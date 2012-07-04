package de.cgawron.mp3.server;

import static de.cgawron.mp3.server.Updater.MODIFIED;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.logging.Logger;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.TagField;

public class Track
{
	private static Logger logger = Logger.getLogger(Track.class.toString());

	public static final String TRACKID = "TRACKID"; //$NON-NLS-1$
	public static final String ALBUMID = "ALBUMID"; //$NON-NLS-1$
	public static final String TRACKNO = "TRACKNO"; //$NON-NLS-1$
	public static final String TITLE = "TITLE"; //$NON-NLS-1$
	public static final String PATH = "PATH"; //$NON-NLS-1$

	static FieldKey[] keys = { FieldKey.ALBUM, FieldKey.ARTIST, FieldKey.GENRE, FieldKey.TITLE, FieldKey.TRACK };

	static Persister pers = new Persister();

	Path path;
	private String title;
	String albumTitle;
	int trackId;
	int trackNo;
	long modified;

	private Album album;

	static class Persister
	{
		static Connection con;
		static PreparedStatement queryTrackByPath;
		static PreparedStatement queryTrackById;

		Persister()
		{
			con = Crawler.getConnection();
			try {
				queryTrackByPath = con
				.prepareStatement("SELECT TRACKID, ALBUMID, TRACKNO, TITLE, PATH, MODIFIED FROM TRACK WHERE PATH=? FOR UPDATE",
									ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE, ResultSet.HOLD_CURSORS_OVER_COMMIT);
				queryTrackByPath.setCursorName("TRACK_BY_PATH");
				queryTrackById = con.prepareStatement("SELECT TRACKID, ALBUMID, TRACKNO, TITLE, PATH, MODIFIED FROM TRACK WHERE TRACKID=?");
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		}

		void persist(Track track) throws SQLException {
			queryTrackByPath.setString(1, track.path.toString());
			ResultSet trackSet = queryTrackByPath.executeQuery();
			if (!trackSet.next()) {
				trackSet.moveToInsertRow();
				trackSet.updateInt(TRACKID, track.trackId);
				trackSet.updateInt(ALBUMID, track.album.albumId);
				trackSet.updateInt(TRACKNO, track.trackNo);
				trackSet.updateString(TITLE, track.title);
				trackSet.updateString(PATH, track.path.toString());
				trackSet.updateTimestamp(MODIFIED, new Timestamp(track.modified));
				trackSet.insertRow();
			}
		}

		Track getById(int id) throws SQLException {
			Track track = null;
			queryTrackById.setInt(1, id);
			ResultSet trackSet = queryTrackById.executeQuery();
			if (trackSet.next()) {
				track = new Track(trackSet);
			}
			return track;
		}
	}

	public Track(Path path) throws Exception
	{
		this.path = path;

		// TODO: Use MD5?
		trackId = path.hashCode();

		modified = Files.getLastModifiedTime(path).toMillis();

		AudioFile f = AudioFileIO.read(path.toFile());
		Tag tag = f.getTag();
		albumTitle = tag.getFirst(FieldKey.ALBUM);
		trackNo = Integer.parseInt(tag.getFirst(FieldKey.TRACK));
		setTitle(tag.getFirst(FieldKey.TITLE));
		for (FieldKey key : keys) {
			List<TagField> fields = tag.getFields(key);
			for (TagField field : fields) {
				logger.info(key + " " + field.getId() + ": " + field.toString());
			}
		}

		album = new Album(this);
	}

	public Track(ResultSet trackSet) throws SQLException
	{
		this.path = FileSystems.getDefault().getPath(trackSet.getString(PATH));
		this.trackId = trackSet.getInt(TRACKID);
		this.trackNo = trackSet.getInt(TRACKNO);
		this.setTitle(trackSet.getString(TITLE));
	}

	public Path getPath() {
		return path;
	}

	public String getAlbumTitle() {
		return albumTitle;
	}

	public int getTrackId() {
		return trackId;
	}

	public int getTrackNo() {
		return trackNo;
	}

	public long getModified() {
		return modified;
	}

	public void persist() throws SQLException {
		pers.persist(this);
	}

	public Album getAlbum() {
		return album;
	}

	public static Track getById(int id) throws SQLException {
		if (pers == null)
			pers = new Persister();
		return pers.getById(id);
	}

	public static Track getById(String id) throws SQLException {
		if (pers == null)
			pers = new Persister();
		return pers.getById(Integer.parseInt(id));
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Tag getTag() throws CannotReadException, IOException, TagException, ReadOnlyFileException, InvalidAudioFrameException {
		AudioFile f = AudioFileIO.read(path.toFile());
		Tag tag = f.getTag();
		return tag;
	}
}
