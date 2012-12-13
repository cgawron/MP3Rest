/*******************************************************************************
 * Copyright (c) 2012 Christian Gawron.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package de.cgawron.didl.model;

import java.net.URI;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/* 
 * storageMedium upnp No
 * longDescription dc No 
 * description dc No
 * publisher dc No 
 * contributor dc No
 * date dc No
 * relation dc No
 * rights dc No
 */

@XmlRootElement(name = "container")
@Entity
public class Album extends Container
{
   private String description;
   private String longDescription;
   private String publisher;
   private String contributor;
   private Date date;
   private URI relation;
   private String rights;

   public Album()
   {
	  super();
	  setClazz(DIDLObject.ALBUM);
   }

   public Album(String id, Container parent, String title, String creator)
   {
	  super(id, parent, title, creator);
	  setClazz(DIDLObject.ALBUM);
   }

   @XmlElement(namespace = NS_DC)
   public String getDescription() {
	  return description;
   }

   public void setDescription(String description) {
	  this.description = description;
   }

   @XmlElement(namespace = NS_UPNP)
   public String getLongDescription() {
	  return longDescription;
   }

   public void setLongDescription(String longDescription) {
	  this.longDescription = longDescription;
   }

   @XmlElement(namespace = NS_DC)
   public String getPublisher() {
	  return publisher;
   }

   public void setPublisher(String publisher) {
	  this.publisher = publisher;
   }

   @XmlElement(namespace = NS_DC)
   public String getContributor() {
	  return contributor;
   }

   public void setContributor(String contributor) {
	  this.contributor = contributor;
   }

   @XmlElement(namespace = NS_DC)
   @Temporal(TemporalType.DATE)
   public Date getDate() {
	  return date;
   }

   public void setDate(Date date) {
	  this.date = date;
   }

   public String getRelation() {
	  if (relation != null)
		 return relation.toASCIIString();
	  else
		 return null;
   }

   public void setRelation(String relation) {
	  if (relation != null)
		 this.relation = URI.create(relation);
	  else
		 this.relation = null;
   }

   public String getRights() {
	  return rights;
   }

   public void setRights(String rights) {
	  this.rights = rights;
   }

}
