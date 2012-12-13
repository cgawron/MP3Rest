/*******************************************************************************
 * Copyright (c) 2012 Christian Gawron.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package de.cgawron.didl.model;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlTransient;

import de.cgawron.mp3.server.upnp.ContentDirectory;

@Entity
public class FileRes extends Res
{

   private URI internalUri;

   public FileRes()
   {
   }

   public FileRes(Path path, String id, String mimeType)
   {
	  super(id, protocolInfo(mimeType));
	  this.internalUri = path.toUri();

   }

   @Transient
   public URI getUri() throws URISyntaxException {
	  return ContentDirectory.getUriForResource(this);
   }

   @XmlTransient
   @Transient
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

   public void setInternalUri(URI internalUri) {
	  this.internalUri = internalUri;
   }

   public void setInternalUriAsString(String uri) throws URISyntaxException {
	  this.internalUri = new URI(uri);
   }

}
