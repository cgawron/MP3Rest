package de.cgawron.mp3.server;

import static java.nio.file.FileVisitResult.CONTINUE;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.sql.DataSource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import de.cgawron.mp3.server.upnp.model.Album;
import de.cgawron.mp3.server.upnp.model.MusicTrack;

@Path("/crawler")
public class Crawler
{
   private static Logger logger = Logger.getLogger(Crawler.class.toString());

   private static java.nio.file.Path ROOT = FileSystems.getDefault().getPath("/opt/mp3");
   // private static String jdbcUrl = "jdbc:db2:Music";
   private static String jdbcUrl = "jdbc:postgresql://localhost:5433/postgres?user=music&password=mUsIc";

   private static Connection con;

   static class UPNPFileVisitor extends SimpleFileVisitor<java.nio.file.Path>
   {
	  EntityManager em;
	  EntityTransaction albumTransaction;
	  Album album;

	  UPNPFileVisitor() throws NamingException
	  {
		 super();
		 Context ic = new InitialContext();
		 EntityManagerFactory entityManagerFactory = (EntityManagerFactory) ic.lookup("java:/MP3Rest");
		 em = entityManagerFactory.createEntityManager();
	  }

	  @Override
	  public FileVisitResult preVisitDirectory(java.nio.file.Path path, BasicFileAttributes attr) throws IOException {
		 logger.info("dir " + path);

		 if (albumTransaction == null) {
			// albumTransaction = em.getTransaction();
			// albumTransaction.begin();
		 }
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
		 if (mimeType != null && mimeType.startsWith("audio/")) {
			try {
			   albumTransaction = em.getTransaction();
			   albumTransaction.begin();
			   logger.info("file " + path + " " + Files.probeContentType(path));
			   MusicTrack track = new MusicTrack(album, path, mimeType);
			   MusicTrack emTrack = em.find(MusicTrack.class, track.getId());
			   if (emTrack == null) {
				  em.merge(track);
			   }
			   else {
				  em.merge(track);
			   }
			   albumTransaction.commit();
			} catch (Exception ex) {
			   logger.log(Level.SEVERE, "error visiting " + path, ex);
			   if (albumTransaction.isActive()) {
				  albumTransaction.rollback();
			   }
			}
		 }
		 return CONTINUE;
	  }
   }

   static class MyFileVisitor extends SimpleFileVisitor<java.nio.file.Path>
   {
	  int numFiles = 0;

	  PreparedStatement insertFile;
	  PreparedStatement queryFile;
	  PreparedStatement updateFile;

	  MyFileVisitor() throws SQLException
	  {
		 con = getConnection();
		 insertFile = con.prepareStatement("INSERT INTO CRAWLER (PATH, MODIFIED, STATE) values (?, ?, ?) ");
		 queryFile = con.prepareStatement("SELECT MODIFIED, STATE FROM CRAWLER WHERE PATH=? ");
		 updateFile = con.prepareStatement("UPDATE CRAWLER SET MODIFIED=?, STATE=? WHERE PATH=? ");
	  }

	  @Override
	  public FileVisitResult preVisitDirectory(java.nio.file.Path path, BasicFileAttributes attr) throws IOException {
		 logger.info("dir " + path);
		 insertPath(path, attr);
		 numFiles++;
		 return CONTINUE;
	  }

	  @Override
	  public FileVisitResult visitFile(java.nio.file.Path path, BasicFileAttributes attr) throws IOException {
		 String mimeType = Files.probeContentType(path);
		 if (mimeType != null && mimeType.startsWith("audio/")) {
			logger.info("file " + path + " " + Files.probeContentType(path));
			insertPath(path, attr);
			numFiles++;
		 }
		 return CONTINUE;
	  }

	  public void insertPath(java.nio.file.Path path, BasicFileAttributes attr) {
		 try {
			queryFile.setString(1, path.toString());

			if (queryFile.executeQuery().next()) {
			   logger.info("Path " + path + " already in DB");
			   // TODO: Check for modification time/status
			} else {
			   insertFile.setString(1, path.toString());
			   insertFile.setTimestamp(2, new Timestamp(attr.lastModifiedTime().toMillis()));
			   insertFile.setInt(3, 1);
			   insertFile.execute();
			   con.commit();
			}
		 } catch (SQLException e) {
			logger.log(Level.SEVERE, "error inserting data", e);
			throw new RuntimeException(e);
		 }
	  }
   }

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
			   con = DriverManager.getConnection(jdbcUrl);
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
