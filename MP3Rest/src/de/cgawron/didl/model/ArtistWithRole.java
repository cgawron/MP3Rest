/*******************************************************************************
 * Copyright (c) 2012 Christian Gawron.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package de.cgawron.didl.model;

import java.util.Set;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.ManyToMany;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlValue;

@Entity
@Access(AccessType.FIELD)
@IdClass(de.cgawron.didl.model.ArtistWithRolePK.class)
public class ArtistWithRole
{
   @XmlEnum(String.class)
   public enum Role {
	  Composer,
	  Soloist,
	  Conductor,
	  @XmlEnumValue("")
	  Unspecified
   }

   @Id
   String artist;

   @Id
   @Enumerated
   Role role;

   @ManyToMany(cascade = CascadeType.ALL, mappedBy = "artists")
   Set<DIDLObject> objects;

   public ArtistWithRole()
   {
   }

   public ArtistWithRole(String artist, Role role)
   {
	  this.artist = artist;
	  this.role = role;
   }

   @XmlValue
   public String getArtist() {
	  return artist;
   }

   public void setArtist(String artist) {
	  this.artist = artist;
   }

   @XmlAttribute
   public Role getRole() {
	  return role;
   }

   public void setRole(Role role) {
	  this.role = role;
   }

   @XmlTransient
   public Set<DIDLObject> getObjects() {
	  return objects;
   }

   public void setObjects(Set<DIDLObject> items) {
	  this.objects = items;
   }

   @Override
   public int hashCode() {
	  final int prime = 31;
	  int result = 1;
	  result = prime * result + ((artist == null) ? 0 : artist.hashCode());
	  result = prime * result + ((role == null) ? 0 : role.hashCode());
	  return result;
   }

   @Override
   public boolean equals(Object obj) {
	  if (this == obj)
		 return true;
	  if (obj == null)
		 return false;
	  if (getClass() != obj.getClass())
		 return false;
	  ArtistWithRole other = (ArtistWithRole) obj;
	  if (artist == null) {
		 if (other.artist != null)
			return false;
	  } else if (!artist.equals(other.artist))
		 return false;
	  if (role != other.role)
		 return false;
	  return true;
   }

}
