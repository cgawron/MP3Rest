package de.cgawron.mp3.server.upnp.model;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagField;
import org.jaudiotagger.tag.TagTextField;

import de.cgawron.mp3.server.upnp.model.ArtistWithRole.Role;

/*
 *   artist upnp No
 album upnp No
 originalTrackNumber upnp No
 playlist upnp No
 storageMedium upnp No
 contributor dc No
 date dc No
 */

@Entity
public class MusicTrack extends AudioItem
{
   private static Logger logger = Logger.getLogger(MusicTrack.class.toString());

   private String album;

   private int originalTrackNumber;

   private Set<ArtistWithRole> artists;

   public MusicTrack()
   {
	  super();
	  setClazz(DIDLObject.MUSICTRACK);
   }

   public MusicTrack(Container container, Path path, String mimeType) throws IOException, URISyntaxException
   {
	  super(uuidForPath(path).toString(), container);
	  setClazz(DIDLObject.MUSICTRACK);
	  logger.info("calling setMetadata");
	  setMetadata(path);
	  Res res = new Res(path, getId(), mimeType);
	  addResource(res);
	  if (container != null) {
		 container.addItem(this);
	  }
   }

   private void setMetadata(Path path) {
	  try {
		 AudioFile f = AudioFileIO.read(path.toFile());
		 Tag tag = f.getTag();
		 setAlbum(tag.getFirst(FieldKey.ALBUM));
		 setOriginalTrackNumber(Integer.parseInt(tag.getFirst(FieldKey.TRACK)));
		 setTitle(tag.getFirst(FieldKey.TITLE));
		 for (TagField field : tag.getFields(FieldKey.ARTIST)) {
			if (field instanceof TagTextField) {
			   TagTextField text = (TagTextField) field;
			   logger.info("adding artist " + text.getContent());
			   addArtist(text.getContent(), ArtistWithRole.Role.Unspecified);
			}
		 }
		 for (TagField field : tag.getFields(FieldKey.COMPOSER)) {
			if (field instanceof TagTextField) {
			   TagTextField text = (TagTextField) field;
			   logger.info("adding composer " + text.getContent());
			   addArtist(text.getContent(), ArtistWithRole.Role.Composer);
			}
		 }
		 for (TagField field : tag.getFields(FieldKey.CONDUCTOR)) {
			if (field instanceof TagTextField) {
			   TagTextField text = (TagTextField) field;
			   logger.info("adding conductor " + text.getContent());
			   addArtist(text.getContent(), ArtistWithRole.Role.Conductor);
			}
		 }

		 Iterator<TagField> tags = tag.getFields();
		 while (tags.hasNext()) {
			TagField field = tags.next();
			logger.info("tag field: " + field.getId() + ": " + field.toString());
		 }

	  } catch (Exception ex) {
		 logger.log(Level.SEVERE, "error setting metadata for " + path, ex);
	  }

   }

   private void addArtist(String artist, Role role) {
	  if (artists == null) {
		 artists = new HashSet<ArtistWithRole>();
	  }
	  artists.add(new ArtistWithRole(artist, role));
   }

   public String getAlbum() {
	  return album;
   }

   public void setAlbum(String album) {
	  this.album = album;
   }

   @ManyToMany(cascade = CascadeType.ALL)
   public Set<ArtistWithRole> getArtists() {
	  return artists;
   }

   public void setArtists(Set<ArtistWithRole> artists) {
	  this.artists = artists;
   }

   public int getOriginalTrackNumber() {
	  return originalTrackNumber;
   }

   public void setOriginalTrackNumber(int originalTrackNumber) {
	  this.originalTrackNumber = originalTrackNumber;
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
		 fc.close();
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
}
