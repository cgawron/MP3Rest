/*******************************************************************************
 * Copyright (c) 2012 Christian Gawron.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
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
import de.cgawron.didl.model.Item;
import de.cgawron.didl.model.MusicGenre;
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
   private EntityManager em;

   public Crawler()
   {
	  for (Indexer indexer : indexerLoader) {
		 logger.info("indexer: " + indexer.mimeTypesSupported() + " " + indexer.getClass().getName());
		 registerIndexer(indexer);
	  }
   }

   class UPNPFileVisitor extends SimpleFileVisitor<java.nio.file.Path>
   {

	  EntityTransaction albumTransaction;
	  Container album;
	  Container rootContainer;
	  int filesVisited;

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
	  public FileVisitResult postVisitDirectory(java.nio.file.Path path, IOException ex) {
		 logger.info("dir " + path);
		 albumTransaction = em.getTransaction();
		 if (albumTransaction.isActive()) {
			albumTransaction.rollback();
		 }
		 albumTransaction.begin();
		 if (album != null) {
			try {
			   album = em.merge(album);
			   addFolders(album);
			   albumTransaction.commit();
			   ContentDirectory.changeSystemUpdateId();
			} catch (Exception e) {
			   if (albumTransaction.isActive())
				  albumTransaction.rollback();
			   logger.log(Level.SEVERE, "exception while adding folders", e);
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
				  if (object instanceof Item) {
					 album = object.getParent();
				  }
				  else {
					 album = (Container) object;
				  }
				  album = em.merge(album);
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
			filesVisited++;
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

	  public String getStatus() {
		 return String.format("\n%d files visited\n", filesVisited);
	  }
   }

   public static void registerIndexer(Indexer indexer) {
	  for (String mimeType : indexer.mimeTypesSupported()) {
		 indexerMap.put(mimeType, indexer);
	  }
   }

   public void addFolders(DIDLObject object) throws CloneNotSupportedException {
	  logger.info("addFolder " + object);
	  if (object instanceof Item)
		 object = object.getParent();

	  if (object instanceof Album) {
		 for (String genre : object.getGenres()) {
			Container genreContainer = new MusicGenre(genre);
			genreContainer = em.merge(genreContainer);
			genreContainer.setParent(Container.getRootContainer(em));
			String id = genreContainer.getId() + "/" + object.getId();
			DIDLObject clone = em.find(Container.class, id);
			if (clone == null) {
			   clone = object.clone();
			   clone.setId(id);
			   clone.setParent(genreContainer);
			   genreContainer.addChild(clone);
			   em.persist(clone);
			   logger.info("Creating additional folder " + clone);
			}
		 }
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
   @Produces({ MediaType.TEXT_PLAIN })
   public String crawl(@QueryParam("root") String root) throws NamingException, IOException {
	  UPNPFileVisitor visitor = new UPNPFileVisitor();
	  java.nio.file.Path rootPath = ROOT;
	  if (root != null) {
		 rootPath = FileSystems.getDefault().getPath(root);
	  }
	  Files.walkFileTree(rootPath, visitor);
	  return visitor.getStatus();
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
	  Crawler crawler = new Crawler();
	  FileVisitor visitor = crawler.new UPNPFileVisitor();
	  Files.walkFileTree(ROOT, visitor);
	  millis = System.currentTimeMillis() - millis;
	  // logger.info(String.format("%d files visited in %d ms",
	  // visitor.numFiles, millis));
   }

}
