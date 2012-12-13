/*******************************************************************************
 * Copyright (c) 2012 Christian Gawron.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package de.cgawron.didl.model;

import java.util.UUID;

import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlRootElement;

/*
 * artist upnp upnp No
 * producer upnp No
 * albumArtURI upnp No
 * toc upnp No
 * genre upnp No
 */
@XmlRootElement(name = "container")
@Entity
public class MusicGenre extends Container
{
   public MusicGenre()
   {
	  setClazz(DIDLObject.MUSICGENRE);
   }

   public MusicGenre(String id, Container parent, String title, String creator)
   {
	  super(id, parent, title, creator);
	  setClazz(DIDLObject.MUSICGENRE);
   }

   public MusicGenre(String title)
   {
	  super(UUID.nameUUIDFromBytes(title.getBytes()).toString(), null, title, null);
	  setClazz(DIDLObject.MUSICGENRE);
   }

   @Override
   public String toString() {
	  return String.format("Container [%s, albumArtURI=%s]",
		                   super.toString(), getAlbumArtURI());
   }
}
