package de.cgawron.mp3.server;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.cgawron.mp3.crawler.Crawler;

public class Updater extends TimerTask implements Runnable
{
   private static Logger logger = Logger.getLogger(Updater.class.toString());

   public static final String MODIFIED = "MODIFIED"; //$NON-NLS-1$

   public static final String PATH = "PATH"; //$NON-NLS-1$
   public static final String STATE = "State"; //$NON-NLS-1$

   Connection con;
   PreparedStatement queryFile;
   PreparedStatement queryAlbum;
   PreparedStatement updateFile;

   Album lastAlbum = null;

   public Updater() throws SQLException
   {
	  super();
	  con = Crawler.getConnection();
	  queryFile = con.prepareStatement("SELECT PATH, MODIFIED, STATE FROM CRAWLER WHERE STATE=1 FOR UPDATE",
		                               ResultSet.TYPE_SCROLL_SENSITIVE,
		                               ResultSet.CONCUR_UPDATABLE,
		                               ResultSet.HOLD_CURSORS_OVER_COMMIT);
	  queryFile.setCursorName("CRAWLER");

	  queryAlbum = con.prepareStatement("SELECT ALBUMID, TITLE FROM ALBUM WHERE TITLE=? FOR UPDATE",
		                                ResultSet.TYPE_SCROLL_SENSITIVE,
		                                ResultSet.CONCUR_UPDATABLE,
		                                ResultSet.HOLD_CURSORS_OVER_COMMIT);
	  queryAlbum.setCursorName("Album");
   }

   @Override
   public void run()
   {
	  logger.info("Starting update run");
	  try {
		 processFiles();
	  } catch (Exception e) {
		 logger.log(Level.SEVERE, "exception in processFile()", e);
	  }
	  logger.info("Ending update run");
   }

   private void processFiles() throws Exception
   {
	  ResultSet set = queryFile.executeQuery();

	  while (set.next()) {
		 Path path = FileSystems.getDefault().getPath(set.getString(PATH));
		 logger.info("processing " + path.getFileName().toString());

		 try {
			if (Files.isDirectory(path))
			   processDirectory(path);
			else
			   processFile(path);
		 } catch (Exception e) {
			con.rollback();
			logger.log(Level.SEVERE, "exception in processFile()", e);
			set.updateInt(STATE, -1);
			set.updateRow();
			con.commit();
			continue;
		 }

		 set.updateInt(STATE, 2);
		 set.updateRow();
		 con.commit();
	  }
   }

   private void processDirectory(Path path) throws Exception
   {

   }

   private void processFile(Path path) throws Exception
   {
	  Track track = new Track(path);
	  Album album = track.getAlbum();
	  if (!album.equals(lastAlbum)) {
		 album.persist();
		 lastAlbum = album;
	  }
	  track.persist();
   }
}
