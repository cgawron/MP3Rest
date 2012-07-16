package de.cgawron.mp3.rest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.teleal.cling.DefaultUpnpServiceConfiguration;
import org.teleal.cling.UpnpService;
import org.teleal.cling.UpnpServiceImpl;
import org.teleal.cling.model.message.header.STAllHeader;
import org.teleal.cling.model.meta.LocalDevice;
import org.teleal.cling.model.meta.RemoteDevice;
import org.teleal.cling.model.types.DeviceType;
import org.teleal.cling.registry.Registry;
import org.teleal.cling.registry.RegistryListener;
import org.teleal.cling.transport.impl.apache.StreamClientConfigurationImpl;
import org.teleal.cling.transport.impl.apache.StreamClientImpl;
import org.teleal.cling.transport.impl.apache.StreamServerConfigurationImpl;
import org.teleal.cling.transport.impl.apache.StreamServerImpl;
import org.teleal.cling.transport.spi.NetworkAddressFactory;
import org.teleal.cling.transport.spi.StreamClient;
import org.teleal.cling.transport.spi.StreamServer;

import de.cgawron.mp3.server.Renderer;

@Path("/upnp/renderer")
public class UPnPResource implements RegistryListener, Runnable
{
   private static Logger logger = Logger.getLogger(UPnPResource.class.toString());

   @SuppressWarnings("rawtypes")
   private class MyUpnpServiceConfiguration extends DefaultUpnpServiceConfiguration
   {
	  @Override
	  public StreamClient createStreamClient() {
		 StreamClientConfigurationImpl config = new StreamClientConfigurationImpl();
		 return new StreamClientImpl(config);
	  }

	  @Override
	  public StreamServer createStreamServer(NetworkAddressFactory networkAddressFactory) {
		 return new StreamServerImpl(new StreamServerConfigurationImpl(networkAddressFactory.getStreamListenPort()));
	  }
   }

   MyUpnpServiceConfiguration config = new MyUpnpServiceConfiguration();

   @Context
   UriInfo uriInfo;

   private static UpnpService upnpService;
   private static Collection<Renderer> renderer = new ArrayList<Renderer>();

   public UPnPResource()
   {
	  if (upnpService == null) {
		 upnpService = new UpnpServiceImpl(config, this);

		 Thread clientThread = new Thread(this);
		 clientThread.setDaemon(false);
		 clientThread.start();
	  }
   }

   @Override
   public void run()
   {
	  try {
		 // Add a listener for device registration events
		 upnpService.getRegistry().addListener(this);

		 // Broadcast a search message for all devices
		 upnpService.getControlPoint().search(new STAllHeader());
	  } catch (Exception ex) {
		 logger.log(Level.SEVERE, "an exception occured in the registry listener", ex);
		 throw new RuntimeException("an exception occured in the registry listener", ex);
	  }
   }

   // This method is called if XMLis request
   @GET
   @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
   public Collection<Renderer> getRenderer() {
	  logger.info("devices: " + renderer);
	  return renderer;
   }

   @Override
   public void remoteDeviceDiscoveryStarted(Registry registry, RemoteDevice device) {
	  // TODO Auto-generated method stub

   }

   @Override
   public void remoteDeviceDiscoveryFailed(Registry registry, RemoteDevice device, Exception ex) {
	  // TODO Auto-generated method stub

   }

   @Override
   public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
	  logger.info("Remote device available: " + device.getDisplayString());
	  logger.info("Device type: " + device.getType());
	  if (device.getType().equals(DeviceType.valueOf("urn:schemas-upnp-org:device:MediaRenderer:1"))) {
		 renderer.add(new Renderer(upnpService, device));
	  }
   }

   @Override
   public void remoteDeviceUpdated(Registry registry, RemoteDevice device) {
	  // TODO Auto-generated method stub

   }

   @Override
   public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
	  // TODO Auto-generated method stub

   }

   @Override
   public void localDeviceAdded(Registry registry, LocalDevice device) {
	  // TODO Auto-generated method stub

   }

   @Override
   public void localDeviceRemoved(Registry registry, LocalDevice device) {
	  // TODO Auto-generated method stub

   }

   @Override
   public void beforeShutdown(Registry registry) {
	  // TODO Auto-generated method stub

   }

   @Override
   public void afterShutdown() {
	  // TODO Auto-generated method stub

   }
}
