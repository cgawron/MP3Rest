package de.cgawron.didl.model;

import java.io.Serializable;

import de.cgawron.didl.model.ArtistWithRole.Role;

public class ArtistWithRolePK implements Serializable
{
   private static final long serialVersionUID = 1L;

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
	  ArtistWithRolePK other = (ArtistWithRolePK) obj;
	  if (artist == null) {
		 if (other.artist != null)
			return false;
	  } else if (!artist.equals(other.artist))
		 return false;
	  if (role != other.role)
		 return false;
	  return true;
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

   String artist;

   Role role;
}
