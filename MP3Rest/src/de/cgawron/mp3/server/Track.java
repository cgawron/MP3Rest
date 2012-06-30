package de.cgawron.mp3.server;

import static de.cgawron.mp3.server.Updater.MODIFIED;
import static de.cgawron.mp3.server.Updater.PATH;

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
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagField;

public class Track {
	private static Logger logger = Logger.getLogger(Track.class.toString());

	public static final String TRACKID = "TRACKID"; //$NON-NLS-1$
	public static final String ALBUMID = "ALBUMID"; //$NON-NLS-1$
	public static final String TITLE = "TITLE"; //$NON-NLS-1$

	static FieldKey[] keys = { FieldKey.ALBUM, FieldKey.ARTIST, FieldKey.GENRE,
			FieldKey.TITLE, FieldKey.TRACK };

	static Persister pers = new Persister();

	Path path;
	String title;
	String albumTitle;
	int trackId;
	long modified;

	private Album album;

	static class Persister {
		static Connection con;
		static PreparedStatement queryTrack;

		Persister() {
			con = Crawler.getConnection();
			try {
				queryTrack = con
						.prepareStatement(
								"SELECT TRACKID, ALBUMID, TITLE, PATH, MODIFIED FROM TRACK WHERE PATH=? FOR UPDATE",
								ResultSet.TYPE_SCROLL_SENSITIVE,
								ResultSet.CONCUR_UPDATABLE,
								ResultSet.HOLD_CURSORS_OVER_COMMIT);
				queryTrack.setCursorName("TRACK");
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		}

		void persist(Track track) throws SQLException {
			queryTrack.setString(1, track.path.toString());
			ResultSet trackSet = queryTrack.executeQuery();
			if (!trackSet.next()) {
				trackSet.moveToInsertRow();
				trackSet.updateInt(TRACKID, track.trackId);
				trackSet.updateInt(ALBUMID, track.album.albumId);
				trackSet.updateString(TITLE, track.title);
				trackSet.updateString(PATH, track.path.toString());
				trackSet.updateTimestamp(MODIFIED,
						new Timestamp(track.modified));
				trackSet.insertRow();
			}
		}
	}

	public Track(Path path) throws Exception {
		this.path = path;

		// TODO: Use MD5?
		trackId = path.hashCode();

		modified = Files.getLastModifiedTime(path).toMillis();

		AudioFile f = AudioFileIO.read(path.toFile());
		Tag tag = f.getTag();
		albumTitle = tag.getFirst(FieldKey.ALBUM);
		title = tag.getFirst(FieldKey.TITLE);
		for (FieldKey key : keys) {
			List<TagField> fields = tag.getFields(key);
			for (TagField field : fields) {
				logger.info(key + " " + field.getId() + ": " + field.toString());
			}
		}

		album = new Album(this);
	}

	public void persist() throws SQLException {
		pers.persist(this);
	}

	public Album getAlbum() {
		return album;
	}
}
