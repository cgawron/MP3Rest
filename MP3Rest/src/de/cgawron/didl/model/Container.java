/*******************************************************************************
 * Copyright (c) 2012 Christian Gawron.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package de.cgawron.didl.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
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
   public static final String ROOT_ID = "0";
   public static final String ALL_ALBUMS_ID = "1";

   public static enum SpecialContainer {
	  ROOT(ROOT_ID, null, "Root"),
	  ALBUMS(ALL_ALBUMS_ID, ROOT, "All Albums");

	  String id;
	  String title;
	  SpecialContainer parent;

	  private SpecialContainer(String id, SpecialContainer parent, String title)
	  {
		 this.id = id;
		 this.title = title;
		 this.parent = parent;
	  }
   }

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

   public Container(UUID id)
   {
	  this(id.toString());
   }

   private List<DIDLObject> children;
   protected Collection<String> createClass;
   protected Collection<String> searchClass;
   private boolean searchable;

   @XmlSchemaType(name = "unsignedInt")
   @XmlAttribute
   @Transient
   public long getChildCount() {
	  if (children != null)
		 return children.size();
	  else
		 return 0;
   }

   @XmlAttribute
   public boolean isSearchable() {
	  return searchable;
   }

   public void setSearchable(boolean searchable) {
	  this.searchable = searchable;
   }

   public void addChild(DIDLObject item) {
	  item.setParent(this);
	  if (children == null) {
		 children = new ArrayList<DIDLObject>();
	  }
	  children.add(item);
   }

   @XmlTransient
   @OrderBy("index")
   @OneToMany(cascade = CascadeType.ALL, mappedBy = "parent")
   public List<DIDLObject> getChildren() {
	  return children;
   }

   public void setChildren(List<DIDLObject> children) {
	  this.children = children;
   }

   @Override
   public void setId(String id) {
	  super.setId(id);
	  if (children != null) {
		 for (DIDLObject child : children) {
			child.setParent(this);
		 }
	  }
   }

   @Override
   public String toString() {
	  return String.format("Container [class=%s, id=%s, parent=%s, title=%s, creator=%s, clazz=%s, resources=%s, childCount=%d]",
		                   getClass().getName(), getId(), getParent() != null ? getParent().getId() : "<null>", getTitle(), getCreator(),
		                   getClazz(),
		                   getResources(), getChildCount());
   }

   public static Container getRootContainer(EntityManager em) {
	  return getSpecialContainer(em, SpecialContainer.ROOT);
   }

   public static Container getSpecialContainer(EntityManager em, SpecialContainer specialContainer) {
	  if (specialContainer == null)
		 return null;
	  Container container = em.find(Container.class, specialContainer.id);
	  if (container == null) {
		 container = new Container(specialContainer.id);
		 container.setTitle(specialContainer.title);
		 container.setParent(getSpecialContainer(em, specialContainer.parent));
	  }
	  return container;
   }

   public static MusicAlbum createMusicAlbumContainer(String id, String title) {
	  return new MusicAlbum(id, null, title, "");
   }

   @Override
   public Container clone() throws CloneNotSupportedException {
	  Container clone = (Container) (super.clone());
	  // ToDo Can we avoid using a random uuid?
	  clone.setId(UUID.randomUUID());
	  if (children != null) {
		 clone.children = new ArrayList<DIDLObject>();
		 for (DIDLObject child : children) {
			clone.addChild(child.clone());
		 }
	  }
	  return clone;
   }
}
