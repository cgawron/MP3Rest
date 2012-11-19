package de.cgawron.didl.model;

import java.util.List;
import java.util.UUID;

import javax.persistence.DiscriminatorValue;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "item")
@Entity
@DiscriminatorValue(DIDLObject.AUDIOITEM)
public class AudioItem extends Item
{
   List<String> genre;
   String description;
   String longDescription;
   List<String> publisher;
   String language;
   String relation;
   String rights;

   public AudioItem()
   {
	  setClazz(AUDIOITEM);
   }

   public AudioItem(String id, Container parent)
   {
	  super(id, parent);
	  setClazz(AUDIOITEM);
   }

   public AudioItem(UUID id, Container parent)
   {
	  this(id.toString(), parent);
   }

   @ElementCollection
   public List<String> getGenre() {
	  return genre;
   }

   public void setGenre(List<String> genre) {
	  this.genre = genre;
   }

   public String getDescription() {
	  return description;
   }

   public void setDescription(String description) {
	  this.description = description;
   }

   public String getLongDescription() {
	  return longDescription;
   }

   public void setLongDescription(String longDescription) {
	  this.longDescription = longDescription;
   }

   @ElementCollection
   public List<String> getPublisher() {
	  return publisher;
   }

   public void setPublisher(List<String> publisher) {
	  this.publisher = publisher;
   }

   public String getLanguage() {
	  return language;
   }

   public void setLanguage(String language) {
	  this.language = language;
   }

   public String getRelation() {
	  return relation;
   }

   public void setRelation(String relation) {
	  this.relation = relation;
   }

   public String getRights() {
	  return rights;
   }

   public void setRights(String rights) {
	  this.rights = rights;
   }

}
