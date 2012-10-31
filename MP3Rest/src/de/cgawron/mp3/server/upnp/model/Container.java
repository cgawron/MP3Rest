package de.cgawron.mp3.server.upnp.model;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

@Entity
@DiscriminatorValue(DIDLObject.CONTAINER)
public class Container extends DIDLObject
{
   protected Container()
   {
	  setClazz(CONTAINER);
   }

   public Container(String id, Container parent, String title, String creator)
   {
	  super(id, parent, title, creator);
	  setClazz(CONTAINER);
   }

   public Container(String title)
   {
	  super(UUID.nameUUIDFromBytes(title.getBytes()).toString(), null, title, null);
	  setClazz(CONTAINER);
   }

   private Set<DIDLObject> children;
   protected Collection<String> createClass;
   protected Collection<String> searchClass;
   private boolean searchable;

   @Override
   public org.teleal.cling.support.model.DIDLObject toClingModel() {
	  // TODO Auto-generated method stub
	  return null;
   }

   @Transient
   public int getChildCount() {
	  return 0;
   }

   public boolean isSearchable() {
	  return searchable;
   }

   public void setSearchable(boolean searchable) {
	  this.searchable = searchable;
   }

   public void addItem(Item dlfItem) {
	  // TODO Auto-generated method stub
   }

   @OneToMany(cascade = CascadeType.ALL, mappedBy = "parent")
   public Set<DIDLObject> getChildren() {
	  return children;
   }

   public void setChildren(Set<DIDLObject> children) {
	  this.children = children;
   }

}
