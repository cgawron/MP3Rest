package de.cgawron.mp3.server.upnp.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "container.type")
@XmlRootElement
@Entity
@DiscriminatorValue(DIDLObject.CONTAINER)
public class Container extends DIDLObject
{
   public static final String ROOTID = "0";

   protected Container()
   {
	  setClazz(CONTAINER);
   }

   public Container(String id, Container parent, String title, String creator)
   {
	  super(id, parent, title, creator);
	  setClazz(CONTAINER);
   }

   public Container(String id)
   {
	  super(id, null, "", "");
	  setClazz(CONTAINER);
   }

   private List<DIDLObject> children;
   protected Collection<String> createClass;
   protected Collection<String> searchClass;
   private boolean searchable;

   @Override
   public org.teleal.cling.support.model.DIDLObject toClingModel() {
	  // TODO Auto-generated method stub
	  return null;
   }

   @XmlSchemaType(name = "unsignedInt")
   @XmlAttribute
   @Transient
   public long getChildCount() {
	  return children.size();
   }

   @XmlAttribute
   public boolean isSearchable() {
	  return searchable;
   }

   public void setSearchable(boolean searchable) {
	  this.searchable = searchable;
   }

   public void addItem(Item item) {
	  if (children == null) {
		 children = new ArrayList<DIDLObject>();
	  }
	  children.add(item);
   }

   @XmlTransient
   @OneToMany(cascade = CascadeType.ALL, mappedBy = "parent")
   public List<DIDLObject> getChildren() {
	  return children;
   }

   public void setChildren(List<DIDLObject> children) {
	  this.children = children;
   }

   @Override
   public String toString() {
	  return String.format("DIDLObject [id=%s, parent=%s, title=%s, creator=%s, clazz=%s, resources=%s, children=%s]",
		                   getId(), getParent() != null ? getParent().getId() : "<null>", getTitle(), getCreator(), getClazz(),
		                   getResources(), children);
   }

}
