package de.cgawron.mp3.server.upnp;

import java.io.IOException;
import java.net.URI;
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
import org.teleal.cling.support.model.PersonWithRole;
import org.teleal.cling.support.model.Res;
import org.teleal.cling.support.model.SortCriterion;
import org.teleal.cling.support.model.item.AudioItem;
import org.teleal.cling.support.model.item.Item;
import org.teleal.cling.support.model.item.MusicTrack;
import org.teleal.common.util.MimeType;

public class ContentDirectory extends AbstractContentDirectoryService implements Runnable
{
   private static Logger logger = Logger.getLogger(ContentDirectory.class.toString());

   @Override
   public BrowseResult browse(String objectID, BrowseFlag browseFlag,
	                          String filter,
	                          long firstResult, long maxResults,
	                          SortCriterion[] orderby) throws ContentDirectoryException {
	  try {

		 // This is just an example... you have to create the DIDL content
		 // dynamically!

		 DIDLContent didl = new DIDLContent();

		 String album = ("Black Gives Way To Blue");
		 String creator = "Alice In Chains"; // Required
		 PersonWithRole artist = new PersonWithRole(creator, "Performer");
		 {
			MimeType mimeType = new MimeType("audio", "mpeg");

			Res dlfRes = new Res(mimeType, 0l,
			                     "http://dradio_mp3_dlf_m.akacast.akamaistream.net/7/249/142684/v1/gnl.akacast.akamaistream.net/dradio_mp3_dlf_m");
			Item item = new AudioItem("100",
			                          "3",
			                          "DLF",
			                          "Deutschlandfunk",
			                          dlfRes);
			item
			.addProperty(new DIDLObject.Property.UPNP.ALBUM_ART_URI(new URI("http://dlf.de/papaya-themes/dradio/img/dlf50/dradio-icon.png")));
			didl.addItem(new MusicTrack(item));
		 }

		 // Create more tracks...

		 // Count and total matches is 2
		 return new BrowseResult(new DIDLParser().generate(didl), 2, 2);

	  } catch (Exception ex) {
		 throw new ContentDirectoryException(
			                                 ContentDirectoryErrorCode.CANNOT_PROCESS,
			                                 ex.toString());
	  }
   }

   @Override
   public BrowseResult search(String containerId,
	                          String searchCriteria, String filter,
	                          long firstResult, long maxResults,
	                          SortCriterion[] orderBy) throws ContentDirectoryException {
	  // You can override this method to implement searching!
	  return super.search(containerId, searchCriteria, filter, firstResult, maxResults, orderBy);
   }

   static LocalDevice createDevice()
   throws ValidationException, LocalServiceBindingException, IOException {

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
		 upnpService.getRegistry().addDevice(createDevice());

	  } catch (Exception ex) {
		 logger.log(Level.SEVERE, "Exception occored", ex);
	  }
   }
}
