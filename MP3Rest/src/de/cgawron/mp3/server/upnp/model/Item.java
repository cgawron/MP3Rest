package de.cgawron.mp3.server.upnp.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "item.type")
@XmlRootElement
@Entity
@DiscriminatorValue(DIDLObject.ITEM)
public class Item extends DIDLObject
{
   public Item()
   {
	  setClazz(ITEM);
   }

   public Item(String id, Container parent)
   {
	  super(id, parent);
	  setClazz(ITEM);
   }

   private String refID;

   // @XmlAttribute(namespace = "urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/")
   @XmlTransient
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
