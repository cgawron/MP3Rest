package de.cgawron.didl.model;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.Transient;
import javax.ws.rs.core.MediaType;

import de.cgawron.mp3.server.upnp.ContentDirectory;

@Entity
public class BlobRes extends Res
{
   byte[] blob;

   public BlobRes()
   {
   }

   public BlobRes(UUID id, String mimeType, byte[] content)
   {
	  this(id.toString(), mimeType, content);
   }

   public BlobRes(String id, String mimeType, byte[] content)
   {
	  super(id, Res.protocolInfo(mimeType));
	  this.blob = content.clone();
   }

   @Override
   @Transient
   public URI getUri() throws URISyntaxException {
	  return ContentDirectory.getUriForResource(this);
   }

   @Lob
   @Basic(fetch = FetchType.LAZY)
   public byte[] getBlob() {
	  return blob;
   }

   public void setBlob(byte[] blob) {
	  this.blob = blob;
   }

   @Transient
   public MediaType getMimeType() {
	  // TODO Store MIME type instead of protocolInfo
	  return MediaType.valueOf("image/jpeg");
   }

}
