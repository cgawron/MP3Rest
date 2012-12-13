/*******************************************************************************
 * Copyright (c) 2012 Christian Gawron.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package de.cgawron.mp3.server.upnp;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

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
import org.teleal.cling.support.model.BrowseFlag;
import org.teleal.cling.support.model.BrowseResult;
import org.teleal.cling.support.model.SortCriterion;
import org.w3c.dom.Node;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;

import de.cgawron.didl.model.BlobRes;
import de.cgawron.didl.model.Container;
import de.cgawron.didl.model.DIDLLite;
import de.cgawron.didl.model.DIDLObject;
import de.cgawron.didl.model.FileRes;
import de.cgawron.didl.model.Res;

public class ContentDirectory extends AbstractContentDirectoryService implements Runnable
{

   public static final String ID_ROOT = "0";
   public static final String ID_ALBUMS = "1";
   public static final String ID_RADIO = "2";
   private static final String ID_DLF = "100";
   private static Logger logger = Logger.getLogger(ContentDirectory.class.toString());
   private static String contextPath;

   private EntityManager entityManager;
   private Marshaller marshaller;
   public static ContentDirectory theContentDirectory;
   private static LocalDevice device;

   public ContentDirectory() throws NamingException, JAXBException
   {
	  super();
	  Context ic = new InitialContext();
	  EntityManagerFactory entityManagerFactory = (EntityManagerFactory) ic.lookup("java:/MP3Rest");
	  entityManager = entityManagerFactory.createEntityManager();

	  JAXBContext jc = JAXBContext.newInstance("de.cgawron.didl.model");
	  marshaller = jc.createMarshaller();
	  marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper",
		                     new NamespacePrefixMapper()
		                     {
			                    @Override
			                    public String getPreferredPrefix(String namespaceUri,
			                                                     String suggestion,
			                                                     boolean requirePrefix) {
			                       switch (namespaceUri) {
								   case "urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/":
									  return "";
								   case "http://purl.org/dc/elements/1.1/":
									  return "dc";
								   case "urn:schemas-upnp-org:metadata-1-0/upnp/":
									  return "upnp";
								   default:
									  return suggestion;
								   }
								}
		                     });
	  theContentDirectory = this;
   }

   @Override
   public BrowseResult browse(String objectID, BrowseFlag browseFlag,
	                          String filter,
	                          long firstResult, long maxResults,
	                          SortCriterion[] orderby) throws ContentDirectoryException {
	  try {
		 logger.info(String.format("browse request: objID=%s, flag=%s, filter=%s, first=%d, max=%d, orderBy=%s",
			                       objectID, browseFlag, filter, firstResult, maxResults, Arrays.asList(orderby)));

		 DIDLObject object = entityManager.find(DIDLObject.class, objectID);
		 if (object == null)
			logger.info("browse: object not found");
		 else {
			entityManager.refresh(object);
			logger.info("object=" + object);
		 }

		 BrowseResult result = createBrowseResult(object, browseFlag, filter, (int) firstResult, (int) maxResults);
		 logger.info("result: " + result.getResult());

		 return result;

	  } catch (Throwable ex) {
		 logger.log(Level.SEVERE, "exception in browse", ex);
		 throw new ContentDirectoryException(ContentDirectoryErrorCode.CANNOT_PROCESS,
			                                 ex.toString());
	  }
   }

   @Override
   public BrowseResult search(String containerId, String
	                          searchCriteria, String filter, long firstResult, long maxResults,
	                          SortCriterion[] orderBy) throws ContentDirectoryException {
	  // You can override this method to implement searching!
	  try {
		 logger.info(String.format(
			                       "search: container=%s, search=%s, filter=%s, first=%d, max=%d, orderBy=%s"
			                       , containerId, searchCriteria, filter, firstResult, maxResults, orderBy));

		 // upnp:class derivedfrom "object.item.audioItem" and @refID exists
		 // false
		 BrowseResult result = createBrowseResult(Container.getRootContainer(entityManager).getChildren(),
			                                      filter, (int) firstResult, (int) maxResults);
		 logger.info("result: " + result.getResult());
		 return result;
	  } catch (Throwable ex) {
		 logger.log(Level.SEVERE, "exception in browse", ex);
		 throw new ContentDirectoryException(ContentDirectoryErrorCode.CANNOT_PROCESS,
			                                 ex.toString());
	  }
   }

   static LocalDevice createDevice(String contextPath)
   throws ValidationException, LocalServiceBindingException, IOException {
	  ContentDirectory.contextPath = contextPath;
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

	  device = new LocalDevice(identity, type, details, contentDirectoryService);
	  return device;

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

   protected BrowseResult createBrowseResult(List<DIDLObject> object, String filter, int firstResult, int maxResults)
   throws Exception
   {
	  DIDLLite didl;
	  int numberReturned = 1;
	  int totalMatches = 1;
	  totalMatches = object.size();
	  List<DIDLObject> children = object.subList(firstResult, totalMatches);
	  ;
	  if (maxResults > 0) {
		 children = object.subList(firstResult,
			                       (firstResult + maxResults) < totalMatches ? firstResult + maxResults : totalMatches);
	  }
	  numberReturned = children.size();
	  didl = new DIDLLite();
	  didl.setChildren(children);
	  DOMResult document = new DOMResult();
	  marshaller.marshal(didl, document);

	  return new BrowseResult(nodeToString(document.getNode(), false), numberReturned, totalMatches, getSystemUpdateID().getValue());
   }

   protected BrowseResult createBrowseResult(DIDLObject object, BrowseFlag browseFlag, String filter, int firstResult, int maxResults)
   throws Exception
   {
	  DIDLLite didl;
	  int numberReturned = 1;
	  int totalMatches = 1;
	  List<DIDLObject> children = null;
	  if (browseFlag == BrowseFlag.DIRECT_CHILDREN && object instanceof Container) {
		 Container container = (Container) object;
		 totalMatches = container.getChildren().size();
		 if (maxResults > 0) {
			children = container.getChildren().subList(firstResult,
			                                           (firstResult + maxResults) < totalMatches ? firstResult + maxResults : totalMatches);
		 }
		 else {
			children = container.getChildren().subList(firstResult, totalMatches);
		 }
		 numberReturned = children.size();
		 didl = new DIDLLite();
		 didl.setChildren(children);
	  } else {
		 didl = new DIDLLite(object);
	  }

	  // TODO: Improve performance (avoid DOM?)
	  logger.info("marshalling ...");
	  StringWriter stringWriter = new StringWriter();
	  XMLEventWriter writer = XMLOutputFactory.newInstance().createXMLEventWriter(stringWriter);
	  marshaller.marshal(didl, writer);
	  logger.info("... done");

	  return new BrowseResult(stringWriter.toString(), numberReturned, totalMatches, getSystemUpdateID().getValue());
   }

   protected static String nodeToString(Node node, boolean omitProlog) throws Exception {
	  logger.info("nodeToString starting ...");
	  TransformerFactory transFactory = TransformerFactory.newInstance();

	  transFactory.setAttribute("indent-number", 4);
	  Transformer transformer = transFactory.newTransformer();

	  if (omitProlog) {
		 // TODO: UPNP VIOLATION: Terratec Noxon Webradio fails when DIDL
		 // content has a prolog
		 // No XML prolog! This is allowed because it is UTF-8 encoded and
		 // required
		 // because broken devices will stumble on SOAP messages that contain
		 // (even
		 // encoded) XML prologs within a message body.
		 transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
	  }
	  transformer.setOutputProperty(OutputKeys.INDENT, "yes");

	  StringWriter out = new StringWriter();
	  transformer.transform(new DOMSource(node), new StreamResult(out));
	  logger.info("... done");
	  return out.toString();
   }

   public static URI getUriForResource(Res res) throws URISyntaxException {
	  if (res instanceof FileRes)
		 return URI.create(contextPath + "/rest/track/" + res.getId() + "/content");
	  else if (res instanceof BlobRes)
		 return URI.create(contextPath + "/rest/blob/" + res.getId() + "/content");
	  else
		 return null;
   }

   // dlna-playcontainer://uuid%3Aa014f495-391b-4f60-8f48-e1c339739476?sid=urn%3Aupnp-org%3AserviceId%3AContentDirectory&cid=Root%2FMy%20Music%2FArtists%2FArtist%2FAllTracks%3AalbumArtistID%3D1818&fid=0&fii=0&sc=%2Bupnp%3Aalbum%2C%2Bupnp%3AoriginalTrackNumber%2C%2Bdc%3Atitle
   // dlna-playcontainer://uuid:fe814e3e-1234-4321-1431-383fb599cc01?sid=urn:upnp-org:serviceId:ContentDirectory&cid=1441&fid=1444&fii=0&sc=&md=0
   public static URI getContainerURI(String id) throws URISyntaxException {
	  UDN udn = theContentDirectory.device.getIdentity().getUdn();
	  String query = String.format("sid=%s&cid=%s", "urn:upnp-org:serviceId:ContentDirectory", id);
	  URI uri = new URI("dlna-playcontainer", "uuid:" + udn.getIdentifierString(), null, query, null);
	  return uri;
   }

   public static void changeSystemUpdateId() {
	  if (theContentDirectory != null) {
		 theContentDirectory.changeSystemUpdateID();
	  }
   }
}
