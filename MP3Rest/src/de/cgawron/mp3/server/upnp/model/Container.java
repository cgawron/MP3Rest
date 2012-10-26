package de.cgawron.mp3.server.upnp.model;

import java.util.Collection;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(DIDLObject.CONTAINER)
public class Container extends DIDLObject
{
   protected Container()
   {
	  setClazz(CONTAINER);
   }

   private int childCount;
   protected Collection<String> createClass;
   protected Collection<String> searchClass;
   private boolean searchable;

   @Override
   public org.teleal.cling.support.model.DIDLObject toClingModel() {
	  // TODO Auto-generated method stub
	  return null;
   }

   public int getChildCount() {
	  return childCount;
   }

   public void setChildCount(int childCount) {
	  this.childCount = childCount;
   }

   public boolean isSearchable() {
	  return searchable;
   }

   public void setSearchable(boolean searchable) {
	  this.searchable = searchable;
   }
}
