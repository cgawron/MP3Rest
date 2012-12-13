/*******************************************************************************
 * Copyright (c) 2012 Christian Gawron.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package de.cgawron.didl.model;


// @Entity
public class Artist
{
   String name;

   public Artist()
   {
   }

   public Artist(String name)
   {
	  this.name = name;
   }

   // @Id
   public String getName() {
	  return name;
   }

   public void setName(String name) {
	  this.name = name;
   }

   @Override
   public int hashCode() {
	  final int prime = 31;
	  int result = 1;
	  result = prime * result + ((name == null) ? 0 : name.hashCode());
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
	  Artist other = (Artist) obj;
	  if (name == null) {
		 if (other.name != null)
			return false;
	  } else if (!name.equals(other.name))
		 return false;
	  return true;
   }

}
