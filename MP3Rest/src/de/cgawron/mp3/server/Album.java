package de.cgawron.mp3.server;

import static de.cgawron.mp3.server.Track.ALBUMID;
import static de.cgawron.mp3.server.Track.TITLE;
import static de.cgawron.mp3.server.Track.TRACKID;

import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement
public class Album
{
   private static Logger logger = Logger.getLogger(Album.class.toString());

   static Persister pers = null; // new Persister();

   public String title;
   public int albumId;

   static class Persister
   {
	  static Connection con;
	  static PreparedStatement queryAlbumByTitle;
	  static PreparedStatement queryAlbumById;
	  static PreparedStatement queryAlbumByIdRO;
	  static PreparedStatement queryAlbumTracks;
	  static PreparedStatement queryAllAlbum;

	  Persister()
	  {
		 logger.info("persister created");
		 con = Crawler.getConnection();
		 try {
			queryAlbumByTitle = con.prepareStatement("SELECT ALBUMID, TITLE FROM ALBUM WHERE TITLE=? FOR UPDATE",
			                                         ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE,
			                                         ResultSet.HOLD_CURSORS_OVER_COMMIT);
			queryAlbumById = con.prepareStatement("SELECT ALBUMID, TITLE FROM ALBUM WHERE ALBUMID=? FOR UPDATE",
			                                      ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE,
			                                      ResultSet.HOLD_CURSORS_OVER_COMMIT);
			queryAlbumByIdRO = con.prepareStatement("SELECT ALBUMID, TITLE FROM ALBUM WHERE ALBUMID=?");
			queryAllAlbum = con.prepareStatement("SELECT ALBUMID, TITLE FROM ALBUM ORDER BY TITLE", ResultSet.TYPE_SCROLL_SENSITIVE,
			                                     ResultSet.CONCUR_UPDATABLE, ResultSet.HOLD_CURSORS_OVER_COMMIT);
			queryAlbumTracks = con
			.prepareStatement("SELECT TRACK.TRACKID, TRACK.TRACKNO FROM TRACK JOIN ALBUM ON TRACK.ALBUMID=ALBUM.ALBUMID "
			                  + "      WHERE ALBUM.ALBUMID=? ORDER BY TRACK.TRACKNO");
			queryAlbumByTitle.setCursorName("ALBUM");
		 } catch (SQLException e) {
			throw new RuntimeException(e);
		 }
	  }

	  void persist(Album album) throws SQLException {
		 queryAlbumByTitle.setString(1, album.title);
		 ResultSet albumSet = queryAlbumByTitle.executeQuery();
		 if (!albumSet.next()) {
			albumSet.moveToInsertRow();
			albumSet.updateInt(ALBUMID, album.albumId);
			albumSet.updateString(TITLE, album.title);
			albumSet.insertRow();
		 }
		 albumSet.close();
	  }

	  List<Album> getAll(List<String> clauses, List<String> args) throws SQLException, MalformedURLException {
		 List<Album> albums = new ArrayList<Album>();
		 ResultSet albumSet = queryAllAlbum.executeQuery();
		 while (albumSet.next()) {
			albums.add(new Album(albumSet.getString(TITLE)));
		 }
		 albumSet.close();
		 return albums;
	  }

	  List<Integer> getTrackIDs(Album album) throws SQLException, MalformedURLException {
		 List<Integer> trackIDs = new ArrayList<Integer>();
		 queryAlbumTracks.setInt(1, album.albumId);
		 ResultSet trackSet = queryAlbumTracks.executeQuery();
		 while (trackSet.next()) {
			int id = trackSet.getInt(TRACKID);
			trackIDs.add(id);
		 }
		 trackSet.close();
		 return trackIDs;
	  }

	  Album getById(int id) throws SQLException, MalformedURLException {
		 Album album = null;
		 queryAlbumByIdRO.setInt(1, id);
		 ResultSet albumSet = queryAlbumByIdRO.executeQuery();
		 if (albumSet.next()) {
			album = new Album(albumSet.getString(TITLE));
		 }
		 albumSet.close();
		 return album;
	  }
   }

   public Album(Album album)
   {
	  this.title = album.title;
	  this.albumId = album.albumId;
   }

   public Album(String title)
   {
	  this.title = title;
	  this.albumId = title.hashCode();
   }

   public Album(Track track)
   {
	  this.title = track.albumTitle;
	  this.albumId = title.hashCode();
   }

   public void persist() throws SQLException {
	  if (pers == null)
		 pers = new Persister();
	  pers.persist(this);
   }

   public static List<Album> getAll(List<String> clauses, List<String> args) throws SQLException,
   MalformedURLException {
	  if (pers == null)
		 pers = new Persister();
	  return Album.pers.getAll(clauses, args);
   }

   public static Album getById(String id) throws NumberFormatException, SQLException, MalformedURLException {
	  if (pers == null)
		 pers = new Persister();
	  return Album.pers.getById(Integer.parseInt(id));
   }

   @XmlTransient
   public List<Integer> getTrackIDs() throws SQLException, MalformedURLException {
	  if (pers == null)
		 pers = new Persister();
	  return Album.pers.getTrackIDs(this);
   }

}
