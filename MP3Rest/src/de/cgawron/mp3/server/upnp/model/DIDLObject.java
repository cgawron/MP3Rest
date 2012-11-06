package de.cgawron.mp3.server.upnp.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

//@XmlRootElement(namespace = "urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/")
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
   private String title = "";
   private String creator = "";
   private String clazz;
   private boolean restricted;
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

   @XmlTransient
   @ManyToOne(cascade = CascadeType.ALL)
   @JoinColumn(name = "parentId")
   public Container getParent() {
	  return parent;
   }

   public void setParent(Container parent) {
	  this.parent = parent;
   }

   @XmlAttribute
   @Transient
   public String getParentID()
   {
	  if (parent == null)
		 return "-1";
	  else
		 return parent.getId();
   }

   @XmlElement(required = true, defaultValue = "", namespace = "http://purl.org/dc/elements/1.1/")
   public String getTitle() {
	  return title;
   }

   public void setTitle(String title) {
	  this.title = title;
   }

   @XmlElement(namespace = "http://purl.org/dc/elements/1.1/")
   public String getCreator() {
	  return creator;
   }

   public void setCreator(String creator) {
	  this.creator = creator;
   }

   @Column(name = "class")
   @XmlElement(name = "class", required = true, namespace = "urn:schemas-upnp-org:metadata-1-0/upnp/")
   public String getClazz() {
	  return clazz;
   }

   protected void setClazz(String clazz) {
	  this.clazz = clazz;
   }

   @XmlAttribute
   public boolean isRestricted() {
	  return restricted;
   }

   public void setRestricted(boolean restricted) {
	  this.restricted = restricted;
   }

   @XmlAttribute
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

   @XmlElement(name = "res")
   @ManyToMany(cascade = CascadeType.ALL)
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

   @Override
   public String toString() {
	  return String.format("DIDLObject [id=%s, parent=%s, title=%s, creator=%s, clazz=%s, resources=%s]",
		                   id, parent.getId(), title, creator, clazz, resources);
   }

}
