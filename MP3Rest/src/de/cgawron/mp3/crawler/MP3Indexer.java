package de.cgawron.mp3.crawler;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.Query;

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
import org.jaudiotagger.tag.id3.framebody.FrameBodyAPIC;

import de.cgawron.didl.model.ArtistWithRole;
import de.cgawron.didl.model.BlobRes;
import de.cgawron.didl.model.Container;
import de.cgawron.didl.model.ExternalRes;
import de.cgawron.didl.model.FileRes;
import de.cgawron.didl.model.Item;
import de.cgawron.didl.model.MusicAlbum;
import de.cgawron.didl.model.MusicTrack;
import de.cgawron.didl.model.Res;
import de.cgawron.didl.model.Container.SpecialContainer;
import de.cgawron.mp3.server.upnp.ContentDirectory;

public class MP3Indexer implements Indexer
{
   private static Logger logger = Logger.getLogger(MP3Indexer.class.toString());
   protected EntityManager em;

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
	  URI albumArt = createAlbumArtURI(f);
	  Res res = new FileRes(path, item.getId(), mimeType);
	  res.setSize(path.toFile().length());
	  res.setDuration(f.getAudioHeader().getTrackLength());
	  item.addResource(res);
	  item.setAlbumArtURI(albumArt);
	  setMetadata(item, f);
	  Query query = em.createNativeQuery("select * from didlobject where title=?", Container.class);
	  query.setParameter(1, item.getAlbum());
	  List<Container> result = Collections.EMPTY_LIST; // (List<Container>)
		                                               // query.getResultList();
	  if (result.size() == 0) {
		 MusicAlbum container = Container.createMusicAlbumContainer(getAlbumID(f, item), item.getAlbum());
		 container.setParent(Container.getSpecialContainer(em, SpecialContainer.ALBUMS));
		 container.setAlbumArtURI(albumArt);
		 URI playcontainer = ContentDirectory.getContainerURI(container.getId());
		 container.addResource(new ExternalRes(playcontainer, Res.ProtocolInfo.parse("dlna-playcontainer:*:application/xml:*")));
		 logger.info("container created: " + container);
		 item.setParent(container);
	  }
	  else
		 item.setParent(result.get(0));
	  return item;
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

   private String getAlbumID(AudioFile f, MusicTrack item) {
	  // TODO Use musicbrainz id
	  return UUID.nameUUIDFromBytes(item.getAlbum().getBytes()).toString();
   }

   private void setMetadata(MusicTrack item, AudioFile f) {
	  try {
		 Tag tag = f.getTag();
		 item.setAlbum(tag.getFirst(FieldKey.ALBUM));
		 item.setOriginalTrackNumber(Integer.parseInt(tag.getFirst(FieldKey.TRACK)));
		 item.setTitle(tag.getFirst(FieldKey.TITLE));
		 for (TagField field : tag.getFields(FieldKey.ARTIST)) {
			if (field instanceof TagTextField) {
			   TagTextField text = (TagTextField) field;
			   logger.info("adding artist " + text.getContent());
			   item.addArtist(text.getContent(), ArtistWithRole.Role.Unspecified);
			}
		 }
		 for (TagField field : tag.getFields(FieldKey.COMPOSER)) {
			if (field instanceof TagTextField) {
			   TagTextField text = (TagTextField) field;
			   logger.info("adding composer " + text.getContent());
			   item.addArtist(text.getContent(), ArtistWithRole.Role.Composer);
			}
		 }
		 for (TagField field : tag.getFields(FieldKey.CONDUCTOR)) {
			if (field instanceof TagTextField) {
			   TagTextField text = (TagTextField) field;
			   logger.info("adding conductor " + text.getContent());
			   item.addArtist(text.getContent(), ArtistWithRole.Role.Conductor);
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
}
