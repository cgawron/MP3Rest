package de.cgawron.mp3.server.upnp.model;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;

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

   public Res(Path path, String mimeType)
   {
	  this.uri = path.toUri();
	  this.protocolInfo = protocolInfo(mimeType);
   }

   private String protocolInfo(String mimeType) {
	  return String.format("http-get:*:%s:*", mimeType);
   }

   @Column(name = "uri", length = 512)
   public String getUriAsString() {
	  if (uri != null)
		 return uri.toASCIIString();
	  else
		 return null;
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
