package de.cgawron.mp3.server.upnp.model;

import java.io.Serializable;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;

@Entity
@IdClass(ArtistWithRole.class)
public class ArtistWithRole implements Serializable
{
   private static final long serialVersionUID = -324688838210861343L;

   public enum Role {
	  Composer,
	  Soloist,
	  Conductor,
	  Unspecified
   }

   Artist artist;

   @Enumerated
   Role role;

   Set<Item> items;

   public ArtistWithRole()
   {
   }

   public ArtistWithRole(String artist, Role role)
   {
	  this.artist = new Artist(artist);
	  this.role = role;
   }

   @Id
   @ManyToOne
   public Artist getArtist() {
	  return artist;
   }

   public void setArtist(Artist artist) {
	  this.artist = artist;
   }

   @Id
   public Role getRole() {
	  return role;
   }

   public void setRole(Role role) {
	  this.role = role;
   }

   @ManyToMany
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
