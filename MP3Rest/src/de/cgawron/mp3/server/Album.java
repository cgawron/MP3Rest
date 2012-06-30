package de.cgawron.mp3.server;

import static de.cgawron.mp3.server.Track.ALBUMID;
import static de.cgawron.mp3.server.Track.TITLE;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Album
{
	private static Logger logger = Logger.getLogger(Album.class.toString());

	static Persister pers = null; // new Persister();

	public String title;
	int albumId;

	static class Persister
	{
		static Connection con;
		static PreparedStatement queryAlbum;
		static PreparedStatement queryAllAlbum;

		Persister()
		{
			con = Crawler.getConnection();
			try {
				queryAlbum = con.prepareStatement(	"SELECT ALBUMID, TITLE FROM ALBUM WHERE TITLE=? FOR UPDATE",
													ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE,
													ResultSet.HOLD_CURSORS_OVER_COMMIT);
				queryAllAlbum = con.prepareStatement(	"SELECT ALBUMID, TITLE FROM ALBUM ",
														ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE,
														ResultSet.HOLD_CURSORS_OVER_COMMIT);
				queryAlbum.setCursorName("ALBUM");
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		}

		void persist(Album album) throws SQLException {
			queryAlbum.setString(1, album.title);
			ResultSet albumSet = queryAlbum.executeQuery();
			if (!albumSet.next()) {
				albumSet.moveToInsertRow();
				albumSet.updateInt(ALBUMID, album.albumId);
				albumSet.updateString(TITLE, album.title);
				albumSet.insertRow();
			}
		}

		List<Album> getAll() throws SQLException {
			List<Album> albums = new ArrayList<Album>();
			ResultSet albumSet = queryAllAlbum.executeQuery();
			while (albumSet.next()) {
				albums.add(new Album(albumSet.getString(TITLE)));
			}
			return albums;
		}
	}

	public Album()
	{
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

	public static List<Album> getAll() throws SQLException {
		if (pers == null)
			pers = new Persister();
		return Album.pers.getAll();
	}

}
