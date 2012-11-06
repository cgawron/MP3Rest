package de.cgawron.mp3.server.upnp.model;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "DIDL-Lite", namespace = "urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/")
@XmlType(name = "root.type")
public class DIDLLite
{
   Container container = null;
   Item item = null;

   List<DIDLObject> children = null;

   public DIDLLite()
   {
   }

   public DIDLLite(DIDLObject content)
   {
	  if (content instanceof Container)
		 this.container = (Container) content;
	  else
		 this.item = (Item) content;
   }

   @XmlElement(name = "container")
   public Container getContainer() {
	  return container;
   }

   public void setContainer(Container content) {
	  this.container = content;
   }

   @XmlElement(name = "item", namespace = "urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/")
   public Item getItem() {
	  return item;
   }

   public void setItem(Item item) {
	  this.item = item;
   }

   @XmlElementRef
   public List<DIDLObject> getChildren() {
	  return children;
   }

   public void setChildren(List<DIDLObject> children) {
	  this.children = children;
   }

}
