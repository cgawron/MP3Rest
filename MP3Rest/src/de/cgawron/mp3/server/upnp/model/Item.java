package de.cgawron.mp3.server.upnp.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(DIDLObject.ITEM)
public class Item extends DIDLObject
{
   public Item()
   {
	  setClazz(ITEM);
   }

   public Item(String id, String parentId)
   {
	  super(id, parentId);
	  setClazz(ITEM);
   }

   private String refID;

   public String getRefID() {
	  return refID;
   }

   public void setRefID(String refID) {
	  this.refID = refID;
   }

   @Override
   public org.teleal.cling.support.model.item.Item toClingModel() {
	  // TODO Auto-generated method stub
	  return null;
   }
}
