package de.cgawron.mp3.server.upnp;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.teleal.cling.UpnpService;
import org.teleal.cling.UpnpServiceImpl;
import org.teleal.cling.binding.LocalServiceBindingException;
import org.teleal.cling.binding.annotations.AnnotationLocalServiceBinder;
import org.teleal.cling.model.DefaultServiceManager;
import org.teleal.cling.model.ValidationException;
import org.teleal.cling.model.meta.DeviceDetails;
import org.teleal.cling.model.meta.DeviceIdentity;
import org.teleal.cling.model.meta.LocalDevice;
import org.teleal.cling.model.meta.LocalService;
import org.teleal.cling.model.meta.ManufacturerDetails;
import org.teleal.cling.model.meta.ModelDetails;
import org.teleal.cling.model.types.DeviceType;
import org.teleal.cling.model.types.UDADeviceType;
import org.teleal.cling.model.types.UDN;
import org.teleal.cling.support.contentdirectory.AbstractContentDirectoryService;
import org.teleal.cling.support.contentdirectory.ContentDirectoryErrorCode;
import org.teleal.cling.support.contentdirectory.ContentDirectoryException;
import org.teleal.cling.support.contentdirectory.DIDLParser;
import org.teleal.cling.support.model.BrowseFlag;
import org.teleal.cling.support.model.BrowseResult;
import org.teleal.cling.support.model.DIDLContent;
import org.teleal.cling.support.model.DIDLObject;
import org.teleal.cling.support.model.Res;
import org.teleal.cling.support.model.SortCriterion;
import org.teleal.cling.support.model.container.Container;
import org.teleal.cling.support.model.container.PlaylistContainer;
import org.teleal.cling.support.model.item.AudioItem;
import org.teleal.cling.support.model.item.Item;
import org.teleal.cling.support.model.item.MusicTrack;
import org.teleal.common.util.MimeType;

import de.cgawron.mp3.server.Album;
import de.cgawron.mp3.server.Track;

public class ContentDirectory extends AbstractContentDirectoryService implements Runnable
{
   public static final String ID_ROOT = "0";
   public static final String ID_ALBUMS = "1";
   public static final String ID_RADIO = "2";
   private static Logger logger = Logger.getLogger(ContentDirectory.class.toString());
   private static String contextPath;

   @Override
   public BrowseResult browse(String objectID, BrowseFlag browseFlag,
	                          String filter,
	                          long firstResult, long maxResults,
	                          SortCriterion[] orderby) throws ContentDirectoryException {
	  try {
		 logger.info(String.format("browse: objID=%s, flag=%s, filter=%s, first=%d, max=%d, orderBy=%s",
			                       objectID, browseFlag, filter, firstResult, maxResults, Arrays.asList(orderby)));
		 // This is just an example... you have to create the DIDL content
		 // dynamically!

		 DIDLContent content = new DIDLContent();
		 DIDLObject object = null;

		 switch (objectID)
		 {
		 case ID_ROOT:
			object = getRootContainer();
			break;

		 // String album = ("Black Gives Way To Blue");
		 // String creator = "Alice In Chains"; // Required
		 // PersonWithRole artist = new PersonWithRole(creator, "Performer");
		 case ID_RADIO:
			Container container = new PlaylistContainer(ID_RADIO, ID_ROOT, "Radio", "", 1);
			container.addItem(getDLFItem());
			object = container;
			break;

		 case ID_ALBUMS:
			object = getAllAlbumContainer();
			break;

		 case "100":
			Item item = getDLFItem();
			object = item;
			break;

		 default:
			if (objectID.startsWith("album/")) {
			   String uuid = objectID.substring(6, 42);
			   Album album = Album.getById(uuid);
			   if (objectID.length() > 42) {
				  String trackNo = objectID.substring(43);
				  object = getAlbumTrackContainer(album, Integer.parseInt(trackNo));
			   }
			   else {
				  object = getAlbumContainer(album);
			   }
			}
			break;
		 }

		 int totalMatches = 0;
		 if (browseFlag == BrowseFlag.DIRECT_CHILDREN) {
			int i = 0;
			if (object instanceof Container) {
			   Container container = (Container) object;
			   for (Container obj : container.getContainers()) {
				  if (i >= firstResult && i < firstResult + maxResults)
					 content.addContainer(obj);
				  i++;
			   }
			   for (Item obj : container.getItems()) {
				  if (i >= firstResult && i < firstResult + maxResults)
					 content.addItem(obj);
				  i++;
			   }
			}
			totalMatches = i;
		 }
		 else {
			if (object instanceof Container) {
			   content.addContainer((Container) object);
			}
			else if (object instanceof Item) {
			   content.addItem((Item) object);
			}
			totalMatches = 1;
		 }

		 int count = content.getContainers().size() + content.getItems().size();
		 logger.info("returning " + content.getContainers() + " " + content.getItems() + " [" + count + "]");
		 return new BrowseResult(new DIDLParser().generate(content), count, totalMatches);

	  } catch (Exception ex) {
		 throw new ContentDirectoryException(ContentDirectoryErrorCode.CANNOT_PROCESS,
			                                 ex.toString());
	  }
   }

   Container allAlbumContainer = null;

   private Container getAllAlbumContainer() throws MalformedURLException, Exception {
	  logger.info("getAllAlbumContainer");
	  if (allAlbumContainer == null) {
		 allAlbumContainer = new Container(ID_ALBUMS, ID_ROOT, "Albums", null, new DIDLObject.Class("object.container"), 0);
		 List<Album> albums = Album.getAll(null, null);
		 for (Album album : albums) {
			allAlbumContainer.addContainer(getAlbumContainer(album));
		 }
		 allAlbumContainer.setSearchable(true);
		 allAlbumContainer.setChildCount(allAlbumContainer.getContainers().size() + allAlbumContainer.getItems().size());
	  }
	  logger.info("getAllAlbumContainer: childCount=" + allAlbumContainer.getChildCount());
	  return allAlbumContainer;
   }

   private Container getAlbumContainer(Album album) throws MalformedURLException, SQLException {
	  logger.info("getAlbumContainer(" + album + ")");
	  String albumId = "album/" + album.albumId.toString();
	  Container didl = new org.teleal.cling.support.model.container.Album(albumId, ID_ALBUMS, album.title,
		                                                                  "", album.getTrackIDs().size());
	  for (int i = 0; i < album.getTrackIDs().size(); i++)
	  {
		 didl.addItem(getAlbumTrackContainer(album, i));
	  }
	  return didl;
   }

   private Item getAlbumTrackContainer(Album album, int trackNo) throws MalformedURLException, SQLException {
	  String albumId = "album/" + album.albumId.toString();
	  UUID uuid = album.getTrackIDs().get(trackNo);
	  Track track = Track.getById(uuid);
	  MimeType mimeType = new MimeType("audio", "mpeg");
	  Res res = new Res(mimeType, track.getFile().length(), contextPath + "/rest/track/" + uuid + "/content");
	  MusicTrack item = new MusicTrack(albumId + "/" + trackNo, albumId, track.getTitle(), "", album.title, "", res);
	  return item;
   }

   private Item getDLFItem() throws URISyntaxException {
	  MimeType mimeType = new MimeType("audio", "mpeg");

	  Res dlfRes = new Res(mimeType, null,
		                   "http://dradio_mp3_dlf_m.akacast.akamaistream.net/7/249/142684/v1/gnl.akacast.akamaistream.net/dradio_mp3_dlf_m");
	  Item item = new AudioItem("100",
		                        ID_RADIO,
		                        "DLF",
		                        "Deutschlandfunk",
		                        dlfRes);
	  item
	  .addProperty(new DIDLObject.Property.UPNP.ALBUM_ART_URI(new URI("http://dlf.de/papaya-themes/dradio/img/dlf50/dradio-icon.png")));
	  return new MusicTrack(item);
   }

   private Container getRootContainer() throws MalformedURLException, Exception {
	  Container didl = new Container(ID_ROOT, ID_ROOT, "Root", null, new DIDLObject.Class("object.container"), 2);
	  didl.addContainer(new PlaylistContainer(ID_ALBUMS, ID_ROOT, "Albums", null, getAllAlbumContainer().getChildCount()));
	  didl.addContainer(new PlaylistContainer(ID_RADIO, ID_ROOT, "Radio", null, 1));
	  didl.setSearchable(true);
	  return didl;
   }

   @Override
   public BrowseResult search(String containerId,
	                          String searchCriteria, String filter,
	                          long firstResult, long maxResults,
	                          SortCriterion[] orderBy) throws ContentDirectoryException {
	  // You can override this method to implement searching!
	  logger.info(String.format("search: container=%s, search=%s, filter=%s, first=%d, max=%d, orderBy=%s",
		                        containerId, searchCriteria, filter, firstResult, maxResults, orderBy));

	  try {
		 DIDLContent content = new DIDLContent();
		 content.addItem(getDLFItem());
		 int count = content.getContainers().size() + content.getItems().size();
		 logger.info("returning " + content.getContainers() + " " + content.getItems() + " [" + count + "]");
		 return new BrowseResult(new DIDLParser().generate(content), count, count);
	  } catch (Exception ex) {
		 throw new ContentDirectoryException(ContentDirectoryErrorCode.CANNOT_PROCESS,
			                                 ex.toString());
	  }
   }

   static LocalDevice createDevice(String contextPath)
   throws ValidationException, LocalServiceBindingException, IOException {
	  ContentDirectory.contextPath = "http://192.168.10.2:8080" + contextPath;
	  DeviceIdentity identity = new DeviceIdentity(UDN.uniqueSystemIdentifier("Demo ContentDirectory"));

	  DeviceType type = new UDADeviceType("MediaServer", 1);

	  DeviceDetails details =
	  new DeviceDetails("MP3Rest UPNP server",
		                new ManufacturerDetails("Christian Gawron"),
		                new ModelDetails("MP3Rest UPNP Server"));

	  LocalService<ContentDirectory> contentDirectoryService =
	  new AnnotationLocalServiceBinder().read(ContentDirectory.class);

	  contentDirectoryService.setManager(
	  new DefaultServiceManager(contentDirectoryService, ContentDirectory.class)
	  );

	  return new LocalDevice(identity, type, details, contentDirectoryService);

	  /*
	   * Several services can be bound to the same device: return new
	   * LocalDevice( identity, type, details, icon, new LocalService[]
	   * {switchPowerService, myOtherService} );
	   */

   }

   public static void main(String[] args) throws Exception {
	  // Start a user thread that runs the UPnP stack
	  Thread serverThread = new Thread(new ContentDirectory());
	  serverThread.setDaemon(false);
	  serverThread.start();
   }

   public void run() {
	  try {

		 final UpnpService upnpService = new UpnpServiceImpl();

		 Runtime.getRuntime().addShutdownHook(new Thread()
		 {
			@Override
			public void run() {
			   upnpService.shutdown();
			}
		 });

		 // Add the bound local device to the registry
		 upnpService.getRegistry().addDevice(createDevice(""));

	  } catch (Exception ex) {
		 logger.log(Level.SEVERE, "Exception occored", ex);
	  }
   }
}
