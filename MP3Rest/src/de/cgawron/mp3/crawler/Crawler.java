package de.cgawron.mp3.crawler;

import static java.nio.file.FileVisitResult.CONTINUE;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.sql.DataSource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import de.cgawron.didl.model.Album;
import de.cgawron.didl.model.Container;
import de.cgawron.didl.model.DIDLObject;
import de.cgawron.mp3.server.upnp.ContentDirectory;

@Path("/crawler")
public class Crawler
{
   private static Logger logger = Logger.getLogger(Crawler.class.toString());

   private static java.nio.file.Path ROOT = FileSystems.getDefault().getPath("/opt/mp3");
   // private static String jdbcUrl = "jdbc:db2:Music";
   // private static String jdbcUrl =
   // "jdbc:postgresql://localhost:5433/postgres?user=music&password=mUsIc";

   private static Connection con;

   private static ServiceLoader<Indexer> indexerLoader = ServiceLoader.load(Indexer.class);
   private static Map<String, Indexer> indexerMap = new HashMap<String, Indexer>();

   public Crawler()
   {
	  for (Indexer indexer : indexerLoader) {
		 logger.info("indexer: " + indexer.mimeTypesSupported() + " " + indexer.getClass().getName());
		 registerIndexer(indexer);
	  }
   }

   static class UPNPFileVisitor extends SimpleFileVisitor<java.nio.file.Path>
   {
	  EntityManager em;
	  EntityTransaction albumTransaction;
	  Album album;
	  Container rootContainer;

	  UPNPFileVisitor() throws NamingException
	  {
		 super();
		 Context ic = new InitialContext();
		 EntityManagerFactory entityManagerFactory = (EntityManagerFactory) ic.lookup("java:/MP3Rest");
		 em = entityManagerFactory.createEntityManager();
		 rootContainer = Container.getRootContainer(em);

	  }

	  @Override
	  public FileVisitResult preVisitDirectory(java.nio.file.Path path, BasicFileAttributes attr) throws IOException {
		 logger.info("dir " + path);

		 if (albumTransaction == null) {
			// albumTransaction = em.getTransaction();
			// albumTransaction.begin();
		 }
		 ;
		 try {
			logger.info("directory " + path);
			album = null; // new MusicAlbum(path.getFileName().toString());
		 } catch (Exception ex) {
			logger.log(Level.SEVERE, "error visiting " + path, ex);
			if (albumTransaction.isActive()) {
			   albumTransaction.rollback();
			}
		 }

		 return CONTINUE;
	  }

	  @Override
	  public FileVisitResult postVisitDirectory(java.nio.file.Path path, IOException ex) throws IOException {
		 logger.info("dir " + path);

		 if (albumTransaction != null) {
			try {
			   // album = em.merge(album);
			   // albumTransaction.commit();
			} catch (Exception e) {
			   logger.log(Level.SEVERE, "error visiting " + path, e);
			   if (albumTransaction.isActive()) {
				  // albumTransaction.rollback();
			   }
			} finally {
			   albumTransaction = null;
			}
		 }

		 return CONTINUE;
	  }

	  @Override
	  public FileVisitResult visitFile(java.nio.file.Path path, BasicFileAttributes attr) throws IOException {
		 String mimeType = Files.probeContentType(path);
		 logger.info("file " + path + " " + mimeType);

		 try {
			albumTransaction = em.getTransaction();
			if (albumTransaction.isActive()) {
			   albumTransaction.rollback();
			}
			albumTransaction.begin();
			DIDLObject object;

			if (mimeType != null) {
			   Indexer indexer = indexerMap.get(mimeType);

			   if (indexer != null) {
				  object = indexer.indexFile(em, path, mimeType);
				  if (object != null) {
					 if (object.getParent() == null)
						object.setParent(rootContainer);
					 logger.info("track id=" + object.getId() + ", parent id=" + object.getParentID());
					 if (em.find(DIDLObject.class, object.getId()) == null) {
						// em.persist(track);
						object = em.merge(object);
					 }
					 else {
						object = em.merge(object);
					 }
				  }
			   }

			}
			em.flush();
			albumTransaction.commit();
			ContentDirectory.changeSystemUpdateId();
		 } catch (EntityExistsException ex) {
			logger.log(Level.SEVERE, "entity already exists " + path, ex);

			if (albumTransaction.isActive()) {
			   albumTransaction.rollback();
			}
		 } catch (Exception ex) {
			logger.log(Level.SEVERE, "error visiting " + path, ex);
			if (albumTransaction.isActive()) {
			   albumTransaction.rollback();
			}
		 }

		 return CONTINUE;
	  }
   }

   public static void registerIndexer(Indexer indexer) {
	  for (String mimeType : indexer.mimeTypesSupported()) {
		 indexerMap.put(mimeType, indexer);
	  }
   }

   @Deprecated
   public static Connection getConnection() {
	  if (con == null) {
		 try {
			Context initCtx = new InitialContext();
			Context envCtx = (Context) initCtx.lookup("java:jboss/datasources");
			DataSource ds = (DataSource) envCtx.lookup("musicDB");
			con = ds.getConnection();
			con.setAutoCommit(false);
		 } catch (Exception e1) {
			logger.log(Level.WARNING, "error opening JDBC connection via JNDI", e1);
			try {
			   // con = DriverManager.getConnection(jdbcUrl);
			   con.setAutoCommit(false);
			} catch (Exception e2) {
			   logger.log(Level.SEVERE, "error opening JDBC connection via URL", e2);
			   throw new RuntimeException(e2);
			}
		 }
	  }
	  return con;
   }

   @GET
   @Produces({ MediaType.APPLICATION_XHTML_XML })
   public String crawl(@QueryParam("root") String root) throws NamingException, IOException {
	  FileVisitor<java.nio.file.Path> visitor = new UPNPFileVisitor();
	  java.nio.file.Path rootPath = ROOT;
	  if (root != null) {
		 rootPath = FileSystems.getDefault().getPath(root);
	  }
	  Files.walkFileTree(rootPath, visitor);
	  return "";
   }

   /**
    * @param args
    * @throws IOException
    */
   public static void main(String[] args) throws Exception {
	  // no daemon (yet)
	  // Timer timer = new Timer(false);
	  // TimerTask updater = new Updater();
	  // timer.schedule(updater, 5000, 5000);

	  long millis = System.currentTimeMillis();
	  FileVisitor visitor = new UPNPFileVisitor();
	  Files.walkFileTree(ROOT, visitor);
	  millis = System.currentTimeMillis() - millis;
	  // logger.info(String.format("%d files visited in %d ms",
	  // visitor.numFiles, millis));
   }

}
