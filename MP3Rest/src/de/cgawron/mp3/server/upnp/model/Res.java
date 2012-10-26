package de.cgawron.mp3.server.upnp.model;

import java.net.URI;
import java.net.URISyntaxException;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Transient;

@Embeddable
public class Res
{
   private URI uri;

   private String protocolInfo;

   public Res()
   {
   }

   public Res(URI uri, String protocolInfo)
   {
	  this.uri = uri;
	  this.protocolInfo = protocolInfo;
   }

   @Column(name = "uri")
   public String getUriAsString() {
	  return uri.toASCIIString();
   }

   @Transient
   public URI getUri() {
	  return uri;
   }

   public void setUri(URI uri) {
	  this.uri = uri;
   }

   public void setUriAsString(String uri) throws URISyntaxException {
	  this.uri = new URI(uri);
   }

   public String getProtocolInfo() {
	  return protocolInfo;
   }

   public void setProtocolInfo(String protocolInfo) {
	  this.protocolInfo = protocolInfo;
   }
}
