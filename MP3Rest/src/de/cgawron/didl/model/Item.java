/*******************************************************************************
 * Copyright (c) 2012 Christian Gawron.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package de.cgawron.didl.model;

import java.util.UUID;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "item.type")
@XmlRootElement
@Entity
@DiscriminatorValue(DIDLObject.ITEM)
public class Item extends DIDLObject
{
   public Item()
   {
	  setClazz(ITEM);
   }

   public Item(String id, Container parent)
   {
	  super(id, parent);
	  setClazz(ITEM);
   }

   private String refID;

   @XmlAttribute
   // @XmlTransient
   public String getRefID() {
	  return refID;
   }

   public void setRefID(String refID) {
	  this.refID = refID;
   }

   @Override
   public Item clone() throws CloneNotSupportedException {
	  Item clone = (Item) (super.clone());
	  clone.setRefID(getId());
	  clone.setId(UUID.randomUUID());
	  return clone;
   }
}
