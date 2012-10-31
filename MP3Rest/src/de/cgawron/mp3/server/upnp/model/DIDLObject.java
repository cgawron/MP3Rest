package de.cgawron.mp3.server.upnp.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "class")
public abstract class DIDLObject
{
   private static final String OBJECT = "object";

   public static final String ITEM = "object.item";
   public static final String AUDIOITEM = "object.item.audioItem";
   public static final String MUSICTRACK = "object.item.audioItem.musicTrack";
   public static final String AUDIOBROADCAST = "object.item.audioItem.audioBroadcast";
   public static final String CONTAINER = "object.container";
   public static final String ALBUM = "object.container.album";
   public static final String MUSICALBUM = "object.container.album.musicAlbum";

   private String id;

   private Container parent;
   private String title;
   private String creator;
   private String clazz;
   private String restricted;
   private List<Res> resources;

   /**
    * Convert into Cling support model.
    * 
    * @return org.teleal.cling.support.model.DIDLObject corresponding to this
    *         object.
    */
   public abstract org.teleal.cling.support.model.DIDLObject toClingModel();

   protected DIDLObject()
   {
	  setClazz(OBJECT);
   }

   protected DIDLObject(String id, Container parent)
   {
	  this.id = id;
	  setParent(parent);
	  setClazz(OBJECT);
   }

   protected DIDLObject(String id, Container parent, String title, String creator)
   {
	  setId(id);
	  setParent(parent);
	  setTitle(title);
	  setCreator(creator);
   }

   @ManyToOne(cascade = CascadeType.ALL)
   @JoinColumn(name = "parentId")
   public Container getParent() {
	  return parent;
   }

   public void setParent(Container parent) {
	  this.parent = parent;
   }

   public String getTitle() {
	  return title;
   }

   public void setTitle(String title) {
	  this.title = title;
   }

   public String getCreator() {
	  return creator;
   }

   public void setCreator(String creator) {
	  this.creator = creator;
   }

   @Column(name = "class")
   public String getClazz() {
	  return clazz;
   }

   protected void setClazz(String clazz) {
	  this.clazz = clazz;
   }

   public String getRestricted() {
	  return restricted;
   }

   public void setRestricted(String restricted) {
	  this.restricted = restricted;
   }

   @Id
   public String getId() {
	  return id;
   }

   public void setId(String id) {
	  this.id = id;
   }

   public void setId(UUID id) {
	  this.id = id.toString();
   }

   @ElementCollection
   public List<Res> getResources() {
	  return resources;
   }

   public void setResources(List<Res> resources) {
	  this.resources = resources;
   }

   public void addResource(Res res) {
	  if (resources == null)
	  {
		 setResources(new ArrayList<Res>());
	  }
	  resources.add(res);
   }

}
