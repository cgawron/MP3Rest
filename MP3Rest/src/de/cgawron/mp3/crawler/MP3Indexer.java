/*******************************************************************************
 * Copyright (c) 2012 Christian Gawron.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package de.cgawron.mp3.crawler;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.EntityManager;

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
import org.jaudiotagger.tag.id3.AbstractTagFrame;
import org.jaudiotagger.tag.id3.AbstractTagFrameBody;
import org.jaudiotagger.tag.id3.framebody.AbstractFrameBodyTextInfo;
import org.jaudiotagger.tag.id3.framebody.FrameBodyAPIC;
import org.jaudiotagger.tag.id3.framebody.FrameBodyUFID;

import de.cgawron.didl.model.ArtistWithRole;
import de.cgawron.didl.model.BlobRes;
import de.cgawron.didl.model.Container;
import de.cgawron.didl.model.Container.SpecialContainer;
import de.cgawron.didl.model.ExternalRes;
import de.cgawron.didl.model.FileRes;
import de.cgawron.didl.model.Item;
import de.cgawron.didl.model.MusicAlbum;
import de.cgawron.didl.model.MusicTrack;
import de.cgawron.didl.model.Res;
import de.cgawron.mp3.server.upnp.ContentDirectory;

public class MP3Indexer implements Indexer
{

   private static Logger logger = Logger.getLogger(MP3Indexer.class.toString());
   protected EntityManager em;
   private final Pattern NUMERIC_GENRE;

   private String[] ID3V1_GENRES = {
	                                "Blues",
	                                "Classic Rock",
	                                "Country",
	                                "Dance",
	                                "Disco",
	                                "Funk",
	                                "Grunge",
	                                "Hip-Hop",
	                                "Jazz",
	                                "Metal",
	                                "New Age",
	                                "Oldies",
	                                "Other",
	                                "Pop",
	                                "R&B",
	                                "Rap",
	                                "Reggae",
	                                "Rock",
	                                "Techno",
	                                "Industrial",
	                                "Alternative",
	                                "Ska",
	                                "Death Metal",
	                                "Pranks",
	                                "Soundtrack",
	                                "Euro-Techno",
	                                "Ambient",
	                                "Trip-Hop",
	                                "Vocal",
	                                "Jazz+Funk",
	                                "Fusion",
	                                "Trance",
	                                "Classical",
	                                "Instrumental",
	                                "Acid",
	                                "House",
	                                "Game",
	                                "Sound Clip",
	                                "Gospel",
	                                "Noise",
	                                "Alternative Rock",
	                                "Bass",
	                                "Soul",
	                                "Punk",
	                                "Space",
	                                "Meditative",
	                                "Instrumental Pop",
	                                "Instrumental Rock",
	                                "Ethnic",
	                                "Gothic",
	                                "Darkwave",
	                                "Techno-Industrial",
	                                "Electronic",
	                                "Pop-Folk",
	                                "Eurodance",
	                                "Dream",
	                                "Southern Rock",
	                                "Comedy",
	                                "Cult",
	                                "Gangsta",
	                                "Top 40",
	                                "Christian Rap",
	                                "Pop/Funk",
	                                "Jungle",
	                                "Native US",
	                                "Cabaret",
	                                "New Wave",
	                                "Psychadelic",
	                                "Rave",
	                                "Showtunes",
	                                "Trailer",
	                                "Lo-Fi",
	                                "Tribal",
	                                "Acid Punk",
	                                "Acid Jazz",
	                                "Polka",
	                                "Retro",
	                                "Musical",
	                                "Rock & Roll",
	                                "Hard Rock",
	                                "Folk",
	                                "Folk-Rock",
	                                "National Folk",
	                                "Swing",
	                                "Fast Fusion",
	                                "Bebob",
	                                "Latin",
	                                "Revival",
	                                "Celtic",
	                                "Bluegrass",
	                                "Avantgarde",
	                                "Gothic Rock",
	                                "Progressive Rock",
	                                "Psychedelic Rock",
	                                "Symphonic Rock",
	                                "Slow Rock",
	                                "Big Band",
	                                "Chorus",
	                                "Easy Listening",
	                                "Acoustic",
	                                "Humour",
	                                "Speech",
	                                "Chanson",
	                                "Opera",
	                                "Chamber Music",
	                                "Sonata",
	                                "Symphony",
	                                "Booty Bass",
	                                "Primus",
	                                "Porn Groove",
	                                "Satire",
	                                "Slow Jam",
	                                "Club",
	                                "Tango",
	                                "Samba",
	                                "Folklore",
	                                "Ballad",
	                                "Power Ballad",
	                                "Rhytmic Soul",
	                                "Freestyle",
	                                "Duet",
	                                "Punk Rock",
	                                "Drum Solo",
	                                "Acapella",
	                                "Euro-House",
	                                "Dance Hall",
	                                "Goa",
	                                "Drum & Bass",
	                                "Club-House",
	                                "Hardcore",
	                                "Terror",
	                                "Indie",
	                                "BritPop",
	                                "Negerpunk",
	                                "Polsk Punk",
	                                "Beat",
	                                "Christian Gangsta",
	                                "Heavy Metal",
	                                "Black Metal",
	                                "Crossover",
	                                "Contemporary C",
	                                "Christian Rock",
	                                "Merengue",
	                                "Salsa",
	                                "Thrash Metal",
	                                "Anime",
	                                "JPop",
	                                "SynthPop"
   };

   public MP3Indexer()
   {
	  super();
	  NUMERIC_GENRE = Pattern.compile("\\(?(\\d+)\\)?");
   }

   @Override
   public List<String> mimeTypesSupported() {
	  return Arrays.asList("audio/mpeg");
   }

   @Override
   public Item indexFile(EntityManager em, Path path, String mimeType) throws IOException, URISyntaxException, CannotReadException,
   TagException, ReadOnlyFileException, InvalidAudioFrameException {
	  this.em = em;
	  MusicTrack item = new MusicTrack(null, path, mimeType);
	  AudioFile f = AudioFileIO.read(path.toFile());
	  item.setId(getTrackId(f));
	  URI albumArt = createAlbumArtURI(f);
	  Res res = new FileRes(path, item.getId(), mimeType);
	  res.setSize(path.toFile().length());
	  res.setDuration(f.getAudioHeader().getTrackLength());
	  item.addResource(res);

	  MusicAlbum container = Container.createMusicAlbumContainer(getAlbumId(f), item.getAlbum());
	  item.setParent(container);
	  item.setAlbumArtURI(albumArt);
	  container.setAlbumArtURI(albumArt);
	  container.setParent(Container.getSpecialContainer(em, SpecialContainer.ALBUMS));
	  container.setAlbumArtURI(albumArt);
	  URI playcontainer = ContentDirectory.getContainerURI(container.getId());
	  container.addResource(new ExternalRes(playcontainer, Res.ProtocolInfo.parse("dlna-playcontainer:*:application/xml:*")));
	  setMetadata(item, container, f);

	  logger.info("container created: " + container);

	  for (String genre : item.getGenres()) {
		 container.addGenre(genre);
	  }
	  for (ArtistWithRole artist : item.getArtists()) {
		 container.addArtist(artist.getArtist(), artist.getRole());
	  }
	  return item;
   }

   private String getTrackId(AudioFile f) throws IOException {
	  String id = UUID.nameUUIDFromBytes(f.getFile().getCanonicalPath().getBytes()).toString();
	  Tag tag = f.getTag();
	  // TODO: Handle more than one UFID
	  for (TagField field : tag.getFields("UFID")) {
		 AbstractTagFrameBody body = ((AbstractTagFrame) field).getBody();
		 if (body instanceof FrameBodyUFID) {
			FrameBodyUFID ufid = (FrameBodyUFID) body;
			id = new String(ufid.getUniqueIdentifier());
			logger.info("UFID: owner=" + ufid.getOwner() + ", id=" + id);
		 }
	  }
	  return id;
   }

   private String getAlbumId(AudioFile f) throws IOException {
	  String id = f.getFile().getParentFile().getCanonicalPath();
	  Tag tag = f.getTag();
	  TagField field = tag.getFirstField(FieldKey.MUSICBRAINZ_RELEASEID);
	  if (field != null)
	  {
		 TagTextField albumId = (TagTextField) field;
		 id = albumId.getContent();
		 logger.info("AlbumID: " + id);

	  }
	  return id;
   }

   private URI createAlbumArtURI(AudioFile f) throws URISyntaxException {
	  Tag tag = f.getTag();
	  TagField field = tag.getFirstField(FieldKey.COVER_ART);
	  if (field != null) {
		 AbstractTagFrameBody body = ((AbstractTagFrame) field).getBody();
		 if (body instanceof FrameBodyAPIC) {
			FrameBodyAPIC apic = (FrameBodyAPIC) body;
			if (apic.isImageUrl()) {
			   throw new RuntimeException("not handled");
			}
			else {
			   String mimeType = apic.getMimeType();
			   byte[] content = apic.getImageData();
			   UUID id = UUID.nameUUIDFromBytes(content);
			   Res imageRes = new BlobRes(id, mimeType, content);
			   imageRes = em.merge(imageRes);
			   logger.info("albumArtURI=" + imageRes.getUri());
			   return imageRes.getUri();
			}
		 }
	  }

	  return null;
   }

   private void setMetadata(MusicTrack item, MusicAlbum container, AudioFile f) {
	  // TODO: Sort order for artists
	  try {
		 Tag tag = f.getTag();
		 item.setAlbum(tag.getFirst(FieldKey.ALBUM));
		 container.setTitle(tag.getFirst(FieldKey.ALBUM));

		 for (TagField field : tag.getFields(FieldKey.ALBUM_ARTIST)) {
			AbstractTagFrameBody body = ((AbstractTagFrame) field).getBody();
			if (body instanceof AbstractFrameBodyTextInfo) {
			   AbstractFrameBodyTextInfo textInfo = (AbstractFrameBodyTextInfo) body;
			   for (int i = 0; i < textInfo.getNumberOfValues(); i++) {
				  String text = normalizeGenre(textInfo.getValueAtIndex(i));
				  logger.info("Album Artist: " + text);
				  container.addArtist(text, ArtistWithRole.Role.Unspecified);
			   }
			}
		 }

		 item.setOriginalTrackNumber(Integer.parseInt(tag.getFirst(FieldKey.TRACK)));
		 item.setOriginalDiscNumber(Integer.parseInt(tag.getFirst(FieldKey.DISC_NO)));
		 item.setTitle(tag.getFirst(FieldKey.TITLE));
		 for (TagField field : tag.getFields(FieldKey.ARTIST)) {
			AbstractTagFrameBody body = ((AbstractTagFrame) field).getBody();
			if (body instanceof AbstractFrameBodyTextInfo) {
			   AbstractFrameBodyTextInfo textInfo = (AbstractFrameBodyTextInfo) body;
			   for (int i = 0; i < textInfo.getNumberOfValues(); i++) {
				  String text = normalizeGenre(textInfo.getValueAtIndex(i));
				  logger.info("Artist: " + text);
				  item.addArtist(text, ArtistWithRole.Role.Unspecified);
			   }
			}
		 }
		 for (TagField field : tag.getFields(FieldKey.COMPOSER)) {
			AbstractTagFrameBody body = ((AbstractTagFrame) field).getBody();
			if (body instanceof AbstractFrameBodyTextInfo) {
			   AbstractFrameBodyTextInfo textInfo = (AbstractFrameBodyTextInfo) body;
			   for (int i = 0; i < textInfo.getNumberOfValues(); i++) {
				  String text = normalizeGenre(textInfo.getValueAtIndex(i));
				  logger.info("Composer: " + text);
				  item.addArtist(text, ArtistWithRole.Role.Composer);
			   }
			}
		 }
		 for (TagField field : tag.getFields(FieldKey.CONDUCTOR)) {
			AbstractTagFrameBody body = ((AbstractTagFrame) field).getBody();
			if (body instanceof AbstractFrameBodyTextInfo) {
			   AbstractFrameBodyTextInfo textInfo = (AbstractFrameBodyTextInfo) body;
			   for (int i = 0; i < textInfo.getNumberOfValues(); i++) {
				  String text = normalizeGenre(textInfo.getValueAtIndex(i));
				  logger.info("Conductor: " + text);
				  item.addArtist(text, ArtistWithRole.Role.Conductor);
			   }
			}
		 }

		 for (TagField field : tag.getFields(FieldKey.GENRE)) {
			AbstractTagFrameBody body = ((AbstractTagFrame) field).getBody();
			if (body instanceof AbstractFrameBodyTextInfo) {
			   AbstractFrameBodyTextInfo textInfo = (AbstractFrameBodyTextInfo) body;
			   for (int i = 0; i < textInfo.getNumberOfValues(); i++) {
				  String genre = normalizeGenre(textInfo.getValueAtIndex(i));
				  logger.info("Genre: " + genre);
				  item.addGenre(genre);
			   }
			}
		 }

		 Iterator<TagField> tags = tag.getFields();
		 while (tags.hasNext()) {
			TagField field = tags.next();
			logger.info("tag field: " + field.getId() + ": " + field.toString());
		 }
	  } catch (Exception ex) {
		 logger.log(Level.SEVERE, "error setting metadata for " + f.toString(), ex);
	  }

   }

   private String normalizeGenre(String genre) {
	  Matcher matcher = NUMERIC_GENRE.matcher(genre);
	  if (matcher.matches()) {
		 return ID3V1_GENRES[Integer.parseInt(matcher.group(1))];
	  }
	  return genre;
   }
}
