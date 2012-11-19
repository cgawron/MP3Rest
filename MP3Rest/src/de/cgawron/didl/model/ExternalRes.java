package de.cgawron.didl.model;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlTransient;

@Entity
public class ExternalRes extends Res
{

   private URI uri;

   public ExternalRes()
   {
   }

   public ExternalRes(URI uri, String mimeType)
   {
	  super(UUID.nameUUIDFromBytes(uri.toASCIIString().getBytes()).toString(), protocolInfo(mimeType));
	  this.uri = uri;

   }

   public ExternalRes(URI uri, ProtocolInfo protocolInfo)
   {
	  super(UUID.nameUUIDFromBytes(uri.toASCIIString().getBytes()).toString(), protocolInfo);
	  this.uri = uri;
   }

   @Transient
   public URI getUri() {
	  return uri;
   }

   @XmlTransient
   @Column(name = "uri", length = 512)
   public String getUriAsString() {
	  if (uri != null)
		 return uri.toASCIIString();
	  else
		 return null;
   }

   public void setUri(URI uri) {
	  this.uri = uri;
   }

   public void setUriAsString(String uri) throws URISyntaxException {
	  this.uri = new URI(uri);
   }
}
