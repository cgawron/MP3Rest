package de.cgawron.mp3.server.upnp.model;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlValue;

import de.cgawron.mp3.server.upnp.ContentDirectory;

@Entity
public class Res
{
   private URI internalUri;
   private String id;
   private String protocolInfo;

   public Res()
   {
   }

   public Res(URI internalUri, String id, String protocolInfo)
   {
	  this.internalUri = internalUri;
	  this.id = id;
	  this.protocolInfo = protocolInfo;
   }

   public Res(Path path, String id, String mimeType)
   {
	  this.id = id;
	  this.internalUri = path.toUri();
	  this.protocolInfo = protocolInfo(mimeType);
   }

   private String protocolInfo(String mimeType) {
	  return String.format("http-get:*:%s:*", mimeType);
   }

   @XmlTransient
   @Id
   @Column(name = "id")
   public String getId() {
	  return id;
   }

   @XmlValue
   @Transient
   public String getUri() {
	  return ContentDirectory.getUriForResource(id);
   }

   public void setId(String id) {
	  this.id = id;
   }

   @XmlAttribute
   public String getProtocolInfo() {
	  return protocolInfo;
   }

   public void setProtocolInfo(String protocolInfo) {
	  this.protocolInfo = protocolInfo;
   }

   @XmlTransient
   @Transient
   @Id
   public URI getInternalUri() {
	  return internalUri;
   }

   @XmlTransient
   @Column(name = "internalUri", length = 512)
   public String getInternalUriAsString() {
	  if (internalUri != null)
		 return internalUri.toASCIIString();
	  else
		 return null;
   }

   public void seInternalUri(URI internalUri) {
	  this.internalUri = internalUri;
   }

   public void setInternalUriAsString(String uri) throws URISyntaxException {
	  this.internalUri = new URI(uri);
   }
}
