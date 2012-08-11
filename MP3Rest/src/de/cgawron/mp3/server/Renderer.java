package de.cgawron.mp3.server;

import java.util.logging.Logger;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.teleal.cling.UpnpService;
import org.teleal.cling.controlpoint.ActionCallback;
import org.teleal.cling.model.action.ActionInvocation;
import org.teleal.cling.model.message.UpnpResponse;
import org.teleal.cling.model.meta.RemoteDevice;
import org.teleal.cling.model.meta.RemoteService;
import org.teleal.cling.model.types.ServiceId;
import org.teleal.cling.support.avtransport.callback.SetAVTransportURI;

@XmlRootElement
public class Renderer
{
   @XmlTransient
   private RemoteDevice device;

   private RemoteService avTransport;
   private UpnpService upnpService;
   private static Logger logger = Logger.getLogger(Renderer.class.getName());

   public void setAVTransportURI(String uri)
   {
	  logger.info("Setting AVTransportURI to " + uri);
	  ActionCallback setAVTransportURI = new SetAVTransportURI(avTransport, uri)
	  {

		 @SuppressWarnings("rawtypes")
		 @Override
		 public void failure(ActionInvocation invocation, UpnpResponse operation, String defaultMsg)
		 {
			logger.severe(defaultMsg);
			throw new RuntimeException(invocation.getFailure());
		 }
	  };
	  upnpService.getControlPoint().execute(setAVTransportURI);
   }

   public Renderer(UpnpService upnpService, RemoteDevice device)
   {
	  this.upnpService = upnpService;
	  this.device = device;
	  this.avTransport = device.findService(ServiceId.valueOf("urn:upnp-org:serviceId:AVTransport"));
   }

   // For JAXB only
   public Renderer()
   {
   }

   @XmlAttribute
   public String getIdentifier()
   {
	  return device.getIdentity().getUdn().getIdentifierString();
   }

   @XmlAttribute
   public String getName()
   {
	  return device.getDetails().getFriendlyName();
   }

}
