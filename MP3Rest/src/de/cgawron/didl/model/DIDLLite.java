package de.cgawron.didl.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "DIDL-Lite", namespace = "urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/")
@XmlType(name = "root.type")
public class DIDLLite
{
   List<DIDLObject> children = new ArrayList<DIDLObject>();

   public DIDLLite()
   {
   }

   public DIDLLite(DIDLObject content)
   {
	  addChild(content);
   }

   public void addChild(DIDLObject content) {
	  children.add(content);
   }

   @XmlElementRef
   public List<DIDLObject> getChildren() {
	  return children;
   }

   public void setChildren(List<DIDLObject> children) {
	  this.children = children;
   }

}
