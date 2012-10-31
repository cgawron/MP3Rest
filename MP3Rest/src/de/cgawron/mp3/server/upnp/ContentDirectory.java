package de.cgawron.mp3.server.upnp;

import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

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
import org.teleal.cling.support.model.SortCriterion;

import de.cgawron.mp3.server.upnp.model.DIDLObject;

public class ContentDirectory extends AbstractContentDirectoryService implements Runnable
{

   public ContentDirectory() throws NamingException
   {
	  super();
	  Context ic = new InitialContext();
	  EntityManagerFactory entityManagerFactory = (EntityManagerFactory) ic.lookup("java:/MP3Rest");
	  entityManager = entityManagerFactory.createEntityManager();
   }

   public static final String ID_ROOT = "0";
   public static final String ID_ALBUMS = "1";
   public static final String ID_RADIO = "2";
   private static final String ID_DLF = "100";
   private static Logger logger = Logger.getLogger(ContentDirectory.class.toString());
   private static String contextPath;

   private EntityManager entityManager;

   @Override
   public BrowseResult browse(String objectID, BrowseFlag browseFlag,
	                          String filter,
	                          long firstResult, long maxResults,
	                          SortCriterion[] orderby) throws ContentDirectoryException {
	  try {
		 logger.info(String.format("browse: objID=%s, flag=%s, filter=%s, first=%d, max=%d, orderBy=%s",
			                       objectID, browseFlag, filter, firstResult, maxResults, Arrays.asList(orderby)));

		 DIDLContent content = new DIDLContent();
		 DIDLObject object = entityManager.find(DIDLObject.class, objectID);
		 logger.info("browse: object=" + object);
		 /*
		  * int totalMatches = 0; if (browseFlag == BrowseFlag.DIRECT_CHILDREN)
		  * { int i = 0; if (object instanceof Container) { Container container
		  * = (Container) object; for (Container obj :
		  * container.getContainers()) { if (i >= firstResult && i < firstResult
		  * + maxResults) content.addContainer(obj); i++; } for (Item obj :
		  * container.getItems()) { if (i >= firstResult && i < firstResult +
		  * maxResults) content.addItem(obj); i++; } } totalMatches = i; } else
		  * { if (object instanceof Container) {
		  * content.addContainer((Container) object); } else if (object
		  * instanceof Item) { content.addItem((Item) object); } totalMatches =
		  * 1; }
		  */
		 int count = content.getContainers().size() + content.getItems().size();
		 logger.info("returning " + content.getContainers() + " " + content.getItems() + " [" + count + "]");
		 return new BrowseResult(new DIDLParser().generate(content), count, 0);

	  } catch (Exception ex) {
		 throw new ContentDirectoryException(ContentDirectoryErrorCode.CANNOT_PROCESS,
			                                 ex.toString());
	  }
   }

   /*
    * Container allAlbumContainer = null;
    * 
    * private Container getAllAlbumContainer() throws MalformedURLException,
    * Exception { logger.info("getAllAlbumContainer"); if (allAlbumContainer ==
    * null) { allAlbumContainer = new Container(ID_ALBUMS, ID_ROOT, "Albums",
    * null, 0); List<Album> albums = Album.getAll(null, null); for (Album album
    * : albums) { allAlbumContainer.addContainer(getAlbumContainer(album)); } //
    * allAlbumContainer.setSearchable(true);
    * allAlbumContainer.setChildCount(allAlbumContainer.getContainers().size() +
    * allAlbumContainer.getItems().size()); }
    * logger.info("getAllAlbumContainer: childCount=" +
    * allAlbumContainer.getChildCount()); return allAlbumContainer; }
    * 
    * private Container getAlbumContainer(Album album) throws
    * MalformedURLException, SQLException { logger.info("getAlbumContainer(" +
    * album + ")"); String albumId = "album/" + album.getAlbumId().toString();
    * Container didl = new de.cgawron.mp3.server.upnp.model.Album(albumId,
    * ID_ALBUMS, album.getTitle(), "", album.getTrackIDs().size()); MimeType
    * mimeType = new MimeType("audio", "m3u"); Res res = new Res(mimeType, 0L,
    * contextPath + "/rest/" + albumId + "/playList"); didl.addResource(res);
    * for (int i = 0; i < album.getTrackIDs().size(); i++) {
    * didl.addItem(getAlbumTrackContainer(album, i)); } return didl; }
    * 
    * private Item getAlbumTrackContainer(Album album, int trackNo) throws
    * MalformedURLException, SQLException { String albumId = "album/" +
    * album.getAlbumId().toString(); UUID uuid =
    * album.getTrackIDs().get(trackNo); Track track = Track.getById(uuid);
    * MimeType mimeType = new MimeType("audio", "mpeg"); Res res = new
    * Res(track.getFile().length(), contextPath + "/rest/track/" + uuid +
    * "/content", "http-get:*:audio/mpeg:*"); MusicTrack item = new
    * MusicTrack(albumId + "/" + trackNo, albumId, track.getTitle(), "",
    * album.getTitle(), "", res); return item; }
    * 
    * private de.cgawron.mp3.server.upnp.model.Item getDLFItem() throws
    * URISyntaxException { URI dlfURI = new URI(
    * "http://dradio_mp3_dlf_m.akacast.akamaistream.net/7/249/142684/v1/gnl.akacast.akamaistream.net/dradio_mp3_dlf_m"
    * ); de.cgawron.mp3.server.upnp.model.Res dlfRes = new
    * de.cgawron.mp3.server.upnp.model.Res(dlfURI, "http-get:*:audio/mpeg:*");
    * 
    * de.cgawron.mp3.server.upnp.model.Item item = new
    * de.cgawron.mp3.server.upnp.model.AudioBroadcast(ID_DLF, ID_RADIO, "DLF",
    * dlfRes); // item.addProperty(new
    * DIDLObject.Property.UPNP.ALBUM_ART_URI(new //
    * URI("http://dlf.de/papaya-themes/dradio/img/dlf50/dradio-icon.png")));
    * 
    * logger.info("persisting " + item); try { EntityTransaction ta =
    * entityManager.getTransaction(); ta.begin(); entityManager.persist(item);
    * entityManager.flush(); ta.commit(); logger.info("persisting " + item +
    * ": done"); } catch (Exception ex) { logger.log(Level.SEVERE,
    * "faild to persist " + item, ex); } return item; }
    * 
    * private Container getRootContainer() throws MalformedURLException,
    * Exception { Container didl = new Container(ID_ROOT, ID_ROOT, "Root", null,
    * new DIDLObject.Class("object.container"), 2); didl.addContainer(new
    * PlaylistContainer(ID_ALBUMS, ID_ROOT, "Albums", null,
    * getAllAlbumContainer().getChildCount())); didl.addContainer(new
    * PlaylistContainer(ID_RADIO, ID_ROOT, "Radio", null, 1)); //
    * didl.setSearchable(true); return didl; }
    */

   /*
    * @Override public BrowseResult search(String containerId, String
    * searchCriteria, String filter, long firstResult, long maxResults,
    * SortCriterion[] orderBy) throws ContentDirectoryException { // You can
    * override this method to implement searching! logger.info(String.format(
    * "search: container=%s, search=%s, filter=%s, first=%d, max=%d, orderBy=%s"
    * , containerId, searchCriteria, filter, firstResult, maxResults, orderBy));
    * 
    * // upnp:class derivedfrom "object.item.audioItem" and @refID exists false
    * 
    * try { DIDLContent content = new DIDLContent(); Collection<DIDLObject>
    * searchResult = doSearch(searchCriteria);
    * 
    * int i = 0; int count = 0; for (DIDLObject object : searchResult) { if (i
    * >= firstResult) { if (maxResults == 0 || count <= maxResults) { if (object
    * instanceof Item) content.addItem((Item) object); else
    * content.addContainer((Container) object); count++; } } i++; }
    * logger.info("returning " + content.getContainers() + " " +
    * content.getItems() + " [" + count + "]"); return new BrowseResult(new
    * DIDLParser().generate(content), count, searchResult.size()); } catch
    * (Exception ex) { throw new
    * ContentDirectoryException(ContentDirectoryErrorCode.CANNOT_PROCESS,
    * ex.toString()); } }
    * 
    * private Collection<DIDLObject> doSearch(String searchCriteria) throws
    * URISyntaxException { Collection<DIDLObject> result = new
    * ArrayList<DIDLObject>(); result.add(getDLFItem());
    * 
    * return result; }
    */

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
