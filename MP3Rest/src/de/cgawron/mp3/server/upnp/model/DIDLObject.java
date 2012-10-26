package de.cgawron.mp3.server.upnp.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "class")
public abstract class DIDLObject
{
   private static final String OBJECT = "object";

   public static final String ITEM = "object.item";
   public static final String AUDIOITEM = "object.item.audioItem";
   public static final String AUDIOBROADCAST = "object.item.audioItem.audioBroadcast";
   public static final String CONTAINER = "object.container";

   private String id;
   private String parentID;
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

   public DIDLObject(String id, String parentId)
   {
	  this.id = id;
	  this.parentID = parentId;
	  setClazz(OBJECT);
   }

   public String getParentID() {
	  return parentID;
   }

   public void setParentID(String parentID) {
	  this.parentID = parentID;
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
