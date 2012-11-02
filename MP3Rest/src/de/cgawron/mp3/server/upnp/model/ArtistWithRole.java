package de.cgawron.mp3.server.upnp.model;

import java.util.Set;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.ManyToMany;

@Entity
@Access(AccessType.FIELD)
@IdClass(de.cgawron.mp3.server.upnp.model.ArtistWithRolePK.class)
public class ArtistWithRole
{

   public enum Role {
	  Composer,
	  Soloist,
	  Conductor,
	  Unspecified
   }

   @Id
   String artist;

   @Id
   @Enumerated
   Role role;

   @ManyToMany(cascade = CascadeType.ALL)
   Set<Item> items;

   public ArtistWithRole()
   {
   }

   public ArtistWithRole(String artist, Role role)
   {
	  this.artist = artist;
	  this.role = role;
   }

   // @ManyToOne(cascade = CascadeType.ALL)
   public String getArtist() {
	  return artist;
   }

   public void setArtist(String artist) {
	  this.artist = artist;
   }

   public Role getRole() {
	  return role;
   }

   public void setRole(Role role) {
	  this.role = role;
   }

   public Set<Item> getItems() {
	  return items;
   }

   public void setItems(Set<Item> items) {
	  this.items = items;
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
