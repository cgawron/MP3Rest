/*******************************************************************************
 * Copyright (c) 2012 Christian Gawron.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package de.cgawron.mp3.server.upnp;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContextEvent;

import org.teleal.cling.DefaultUpnpServiceConfiguration;
import org.teleal.cling.UpnpService;
import org.teleal.cling.UpnpServiceImpl;
import org.teleal.cling.transport.impl.apache.StreamClientConfigurationImpl;
import org.teleal.cling.transport.impl.apache.StreamClientImpl;
import org.teleal.cling.transport.impl.apache.StreamServerConfigurationImpl;
import org.teleal.cling.transport.impl.apache.StreamServerImpl;
import org.teleal.cling.transport.spi.NetworkAddressFactory;
import org.teleal.cling.transport.spi.StreamClient;
import org.teleal.cling.transport.spi.StreamServer;

public class UPNPServletContextListener implements javax.servlet.ServletContextListener
{
   private static Logger logger = Logger.getLogger(UPNPServletContextListener.class.toString());
   UpnpService upnpService;

   public class UpnpServiceConfiguration extends DefaultUpnpServiceConfiguration
   {

	  @Override
	  public StreamClient createStreamClient() {
		 return new StreamClientImpl(new StreamClientConfigurationImpl());
	  }

	  @Override
	  public StreamServer createStreamServer(NetworkAddressFactory networkAddressFactory) {
		 return new StreamServerImpl(
			                         new StreamServerConfigurationImpl(
			                                                           networkAddressFactory.getStreamListenPort()
			                         ));
	  }

   }

   @Override
   public void contextDestroyed(ServletContextEvent event) {
	  try {
		 if (upnpService != null)
		 {
			logger.info("shutting down UPNP service");
			upnpService.shutdown();
			upnpService = null;
		 }
	  } catch (Exception ex) {
		 logger.log(Level.SEVERE, "Exception occored", ex);
	  }
   }

   @Override
   public void contextInitialized(ServletContextEvent event) {
	  try {
		 if (upnpService == null) {
			logger.info("starting UPNP service");
			upnpService = new UpnpServiceImpl(new UpnpServiceConfiguration());
			String fullContextPath = event.getServletContext().getInitParameter("baseURL") + event.getServletContext().getContextPath();
			upnpService.getRegistry().addDevice(ContentDirectory.createDevice(fullContextPath));
		 }
	  } catch (Exception ex) {
		 logger.log(Level.SEVERE, "Exception occored", ex);
	  }
   }

}
