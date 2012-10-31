package de.cgawron.mp3.server.upnp.model;

import javax.persistence.Entity;

/*
 * artist upnp upnp No
 * producer upnp No
 * albumArtURI upnp No
 * toc upnp No
 * genre upnp No
 */

@Entity
public class MusicAlbum extends Album
{
   public MusicAlbum()
   {
	  setClazz(DIDLObject.MUSICALBUM);
   }

   public MusicAlbum(String title)
   {
	  super(title);
	  setClazz(DIDLObject.MUSICALBUM);
   }
}
