/*******************************************************************************
 * Copyright (c) 2012 Christian Gawron.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package de.cgawron.mp3.server;

import static de.cgawron.mp3.server.Updater.MODIFIED;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
import org.jaudiotagger.tag.TagTextField;

import de.cgawron.mp3.crawler.Crawler;

public class Track
{
   private static Logger logger = Logger.getLogger(Track.class.toString());

   public static final String TRACKID = "TRACKID"; //$NON-NLS-1$
   public static final String ALBUMID = "ALBUMID"; //$NON-NLS-1$
   public static final String TRACKNO = "TRACKNO"; //$NON-NLS-1$
   public static final String TITLE = "TITLE"; //$NON-NLS-1$
   public static final String PATH = "PATH"; //$NON-NLS-1$

   public static final FieldKey[] FIELD_KEYS = { FieldKey.ALBUM,
	                                            FieldKey.ARTIST,
	                                            FieldKey.ARTISTS,
	                                            FieldKey.ALBUM_ARTIST,
	                                            FieldKey.ACOUSTID_ID,
	                                            FieldKey.COMPOSER,
	                                            FieldKey.CONDUCTOR,
	                                            FieldKey.GENRE,
	                                            FieldKey.TITLE,
	                                            FieldKey.TRACK };

   static Persister pers = new Persister();

   private Path path;

   private String title;

   String albumTitle;

   UUID trackId;

   int trackNo;

   long modified;

   Map<FieldKey, String> tags = new HashMap<FieldKey, String>();

   private Album album;

   static class Persister
   {
	  static Connection con;
	  static PreparedStatement queryTrackByPath;
	  static PreparedStatement queryTrackById;
	  static PreparedStatement updateTags;

	  Persister()
	  {
		 con = Crawler.getConnection();
		 try {
			queryTrackByPath = con
			.prepareStatement("SELECT TRACKID, ALBUMID, TRACKNO, TITLE, PATH, MODIFIED FROM TRACK WHERE PATH=? FOR UPDATE",
			                  ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE, ResultSet.HOLD_CURSORS_OVER_COMMIT);
			// queryTrackByPath.setCursorName("TRACK_BY_PATH");
			queryTrackById = con.prepareStatement("SELECT TRACKID, ALBUMID, TRACKNO, TITLE, PATH, MODIFIED FROM TRACK WHERE TRACKID=?");
			updateTags = con.prepareStatement("SELECT TRACKID, TAG, TEXT FROM TAGS WHERE TRACKID=? AND TAG=? FOR UPDATE",
			                                  ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE,
			                                  ResultSet.HOLD_CURSORS_OVER_COMMIT);
		 } catch (SQLException e) {
			throw new RuntimeException(e);
		 }
	  }

	  void persist(Track track) throws SQLException {
		 queryTrackByPath.setString(1, track.path.toString());
		 ResultSet trackSet = queryTrackByPath.executeQuery();
		 if (!trackSet.next()) {
			trackSet.moveToInsertRow();
			trackSet.updateObject(TRACKID, track.trackId);
			trackSet.updateObject(ALBUMID, track.album.getAlbumId());
			trackSet.updateInt(TRACKNO, track.trackNo);
			trackSet.updateString(TITLE, track.title);
			trackSet.updateString(PATH, track.path.toString());
			trackSet.updateTimestamp(MODIFIED, new Timestamp(track.modified));
			trackSet.insertRow();
		 }

		 for (FieldKey key : track.tags.keySet()) {
			logger.fine(String.format("storing tag %s=%s for %s", key.name(), track.tags.get(key), track.trackId.toString()));
			updateTags.setObject(1, track.trackId);
			updateTags.setString(2, key.name());
			ResultSet tagSet = updateTags.executeQuery();
			if (!tagSet.next()) {
			   tagSet.moveToInsertRow();
			   tagSet.updateObject(1, track.trackId);
			   tagSet.updateString(2, key.name());
			   tagSet.updateString(3, track.tags.get(key));
			   tagSet.insertRow();
			}
			else {
			   tagSet.updateString(3, track.tags.get(key));
			   tagSet.updateRow();
			}
			tagSet.close();
		 }
		 trackSet.close();

	  }

	  Track getById(UUID uuid) throws SQLException {
		 Track track = null;
		 queryTrackById.setObject(1, uuid);
		 ResultSet trackSet = queryTrackById.executeQuery();
		 if (trackSet.next()) {
			track = new Track(trackSet);
		 }
		 trackSet.close();
		 return track;
	  }

	  Track getByURL(URL url) throws SQLException {
		 int id = Integer.parseInt(url.getPath());
		 Track track = null;
		 queryTrackById.setInt(1, id);
		 ResultSet trackSet = queryTrackById.executeQuery();
		 if (trackSet.next()) {
			track = new Track(trackSet);
		 }
		 trackSet.close();
		 return track;
	  }
   }

   public Track(Path path) throws Exception
   {
	  this.path = path;

	  trackId = uuidForPath(path);

	  modified = Files.getLastModifiedTime(path).toMillis();

	  AudioFile f = AudioFileIO.read(path.toFile());
	  Tag tag = f.getTag();
	  albumTitle = tag.getFirst(FieldKey.ALBUM);
	  trackNo = Integer.parseInt(tag.getFirst(FieldKey.TRACK));
	  setTitle(tag.getFirst(FieldKey.TITLE));
	  for (FieldKey key : FIELD_KEYS) {
		 TagField field = tag.getFirstField(key);
		 if (field instanceof TagTextField) {
			TagTextField text = (TagTextField) field;
			// logger.info(key + " " + field.getId() + ": " +
			// text.getContent());
			tags.put(key, text.getContent());
		 }
	  }

	  album = new Album(this);
   }

   public Track(ResultSet trackSet) throws SQLException
   {
	  this.path = FileSystems.getDefault().getPath(trackSet.getString(PATH));
	  this.trackId = (UUID) trackSet.getObject(TRACKID);
	  this.trackNo = trackSet.getInt(TRACKNO);
	  this.setTitle(trackSet.getString(TITLE));
   }

   public Track(Track track)
   {
	  this.path = track.path;
	  this.trackId = track.trackId;
	  this.trackNo = track.trackNo;
	  this.title = track.title;
   }

   Path getPath() {
	  return path;
   }

   public File getFile() {
	  return path.toFile();
   }

   public String getAlbumTitle() {
	  return albumTitle;
   }

   public UUID getTrackId() {
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

   public static Track getByURL(URL id) throws SQLException {
	  if (pers == null)
		 pers = new Persister();
	  return pers.getByURL(id);
   }

   public static Track getById(String id) throws SQLException {
	  if (pers == null)
		 pers = new Persister();
	  return pers.getById(UUID.fromString(id));
   }

   public static Track getById(UUID id) throws SQLException {
	  if (pers == null)
		 pers = new Persister();
	  return pers.getById(id);
   }

   public String getTitle() {
	  return title;
   }

   public void setTitle(String title) {
	  this.title = title;
   }

   Tag getTag() throws CannotReadException, IOException, TagException, ReadOnlyFileException, InvalidAudioFrameException {
	  AudioFile f = AudioFileIO.read(path.toFile());
	  Tag tag = f.getTag();
	  return tag;
   }

   public List<TagField> getFields(FieldKey key) throws Exception {
	  return getTag().getFields(key);
   }

   public static UUID uuidForPath(Path path) throws IOException
   {
	  byte[] hash;
	  FileChannel fc = FileChannel.open(path, StandardOpenOption.READ);
	  MappedByteBuffer buffer = fc.map(MapMode.READ_ONLY, 0, Files.size(path));

	  try {
		 MessageDigest md = MessageDigest.getInstance("MD5"); //$NON-NLS-1$
		 md.update(buffer);
		 hash = md.digest();
	  } catch (NoSuchAlgorithmException e) {
		 throw new AssertionError(e);
	  }

	  long msb = (hash[0] & 0xFFL) << 56;
	  msb |= (hash[1] & 0xFFL) << 48;
	  msb |= (hash[2] & 0xFFL) << 40;
	  msb |= (hash[3] & 0xFFL) << 32;
	  msb |= (hash[4] & 0xFFL) << 24;
	  msb |= (hash[5] & 0xFFL) << 16;
	  msb |= (hash[6] & 0x0FL) << 8;
	  msb |= (0x3L << 12); // set the version to 3
	  msb |= (hash[7] & 0xFFL);

	  long lsb = (hash[8] & 0x3FL) << 56;
	  lsb |= (0x2L << 62); // set the variant to bits 01
	  lsb |= (hash[9] & 0xFFL) << 48;
	  lsb |= (hash[10] & 0xFFL) << 40;
	  lsb |= (hash[11] & 0xFFL) << 32;
	  lsb |= (hash[12] & 0xFFL) << 24;
	  lsb |= (hash[13] & 0xFFL) << 16;
	  lsb |= (hash[14] & 0xFFL) << 8;
	  lsb |= (hash[15] & 0xFFL);
	  return new UUID(msb, lsb);
   }

   /*
    * public static UUID bytesToUUID(byte[] bytes) { long msb = (bytes[0] &
    * 0xFFL) << 56; msb |= (bytes[1] & 0xFFL) << 48; msb |= (bytes[2] & 0xFFL)
    * << 40; msb |= (bytes[3] & 0xFFL) << 32; msb |= (bytes[4] & 0xFFL) << 24;
    * msb |= (bytes[5] & 0xFFL) << 16; msb |= (bytes[6] & 0xFFL) << 8; msb |=
    * (bytes[7] & 0xFFL);
    * 
    * long lsb = (bytes[8] & 0xFFL) << 56; lsb |= (bytes[9] & 0xFFL) << 48; lsb
    * |= (bytes[10] & 0xFFL) << 40; lsb |= (bytes[11] & 0xFFL) << 32; lsb |=
    * (bytes[12] & 0xFFL) << 24; lsb |= (bytes[13] & 0xFFL) << 16; lsb |=
    * (bytes[14] & 0xFFL) << 8; lsb |= (bytes[15] & 0xFFL); return new UUID(msb,
    * lsb); }
    * 
    * public static byte[] uuidToBytes(UUID uuid) { long msb =
    * uuid.getMostSignificantBits(); long lsb = uuid.getLeastSignificantBits();
    * byte[] bytes = new byte[16];
    * 
    * bytes[0] = (byte) ((msb >> 56) & 0xff); bytes[1] = (byte) ((msb >> 48) &
    * 0xff); bytes[2] = (byte) ((msb >> 40) & 0xff); bytes[3] = (byte) ((msb >>
    * 32) & 0xff); bytes[4] = (byte) ((msb >> 24) & 0xff); bytes[5] = (byte)
    * ((msb >> 16) & 0xff); bytes[6] = (byte) ((msb >> 8) & 0xff); bytes[7] =
    * (byte) ((msb) & 0xff);
    * 
    * bytes[8] = (byte) ((lsb >> 56) & 0xff); bytes[9] = (byte) ((lsb >> 48) &
    * 0xff); bytes[10] = (byte) ((lsb >> 40) & 0xff); bytes[11] = (byte) ((lsb
    * >> 32) & 0xff); bytes[12] = (byte) ((lsb >> 24) & 0xff); bytes[13] =
    * (byte) ((lsb >> 16) & 0xff); bytes[14] = (byte) ((lsb >> 8) & 0xff);
    * bytes[15] = (byte) ((lsb) & 0xff);
    * 
    * return bytes; }
    */
}
