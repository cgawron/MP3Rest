package de.cgawron.mp3.server.upnp.model;

import javax.persistence.Entity;

/* 
 * storageMedium upnp No
 * longDescription dc No 
 * description dc No
 * publisher dc No 
 * contributor dc No
 * date dc No
 * relation dc No
 * rights dc No
 */

@Entity
public class Album extends Container
{
   public Album()
   {
	  super();
	  setClazz(DIDLObject.ALBUM);
   }

   public Album(String title)
   {
	  super(title);
	  setClazz(DIDLObject.ALBUM);
   }
}
