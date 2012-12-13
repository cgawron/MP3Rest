/*******************************************************************************
 * Copyright (c) 2012 Christian Gawron.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package de.cgawron.didl.model;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlValue;

import org.teleal.cling.model.types.InvalidValueException;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "type", discriminatorType = DiscriminatorType.STRING, length = 16)
public abstract class Res
{
   protected String id;
   private ProtocolInfo protocolInfo;
   private String duration;
   private long size;
   private String type;

   public static class ProtocolInfo
   {
	  String protocol;
	  String network;
	  String mimeType;
	  String additionalInfo;

	  private static Map<String, ProtocolInfo> map = new HashMap<String, ProtocolInfo>();

	  private ProtocolInfo(String protocol, String network, String mimeType, String additionalInfo)
	  {
		 super();
		 this.protocol = protocol;
		 this.network = network;
		 this.mimeType = mimeType;
		 this.additionalInfo = additionalInfo;
	  }

	  public static ProtocolInfo parse(String protocolInfo) {
		 if (map.containsKey(protocolInfo)) {
			return map.get(protocolInfo);
		 }
		 else {
			protocolInfo = protocolInfo.trim();
			String[] split = protocolInfo.split(":");
			if (split.length != 4) {
			   throw new InvalidValueException("Can't parse ProtocolInfo string: " + protocolInfo);
			}
			ProtocolInfo info = new ProtocolInfo(split[0], split[1], split[2], split[3]);
			map.put(protocolInfo, info);
			return info;
		 }
	  }

	  @Override
	  public String toString()
	  {
		 return protocol + ":" + network + ":" + mimeType + ":" + additionalInfo;
	  }

	  public String getProtocol() {
		 return protocol;
	  }

	  public void setProtocol(String protocol) {
		 this.protocol = protocol;
	  }

	  public String getNetwork() {
		 return network;
	  }

	  public void setNetwork(String network) {
		 this.network = network;
	  }

	  public String getMimeType() {
		 return mimeType;
	  }

	  public void setMimeType(String mimeType) {
		 this.mimeType = mimeType;
	  }

	  public String getAdditionalInfo() {
		 return additionalInfo;
	  }

	  public void setAdditionalInfo(String additionalInfo) {
		 this.additionalInfo = additionalInfo;
	  }

   }

   public Res()
   {
   }

   public Res(String id)
   {
	  this.id = id;
   }

   public Res(String id, ProtocolInfo protocolInfo)
   {
	  this.id = id;
	  this.protocolInfo = protocolInfo;
   }

   public Res(String id, String protocolInfo)
   {
	  this.id = id;
	  this.protocolInfo = ProtocolInfo.parse(protocolInfo);
   }

   protected static String protocolInfo(String mimeType) {
	  return String.format("http-get:*:%s:*", mimeType);
   }

   @XmlTransient
   @Id
   @Column(name = "id")
   public String getId() {
	  return id;
   }

   @XmlValue
   @Transient
   public abstract URI getUri() throws URISyntaxException;

   public void setId(String id) {
	  this.id = id;
   }

   @XmlAttribute(name = "protocolInfo")
   @Column(name = "protocolInfo")
   public String getProtocolInfoAsString() {
	  if (protocolInfo != null)
		 return protocolInfo.toString();
	  else
		 return null;
   }

   @Transient
   @XmlTransient
   public ProtocolInfo getProtocolInfo() {
	  return protocolInfo;
   }

   public void setProtocolInfo(ProtocolInfo protocolInfo) {
	  this.protocolInfo = protocolInfo;
   }

   public void setProtocolInfoAsString(String protocolInfo) {
	  this.protocolInfo = ProtocolInfo.parse(protocolInfo);
   }

   @XmlAttribute
   public String getDuration() {
	  return duration;
   }

   public void setDuration(String duration) {
	  this.duration = duration;
   }

   public void setDuration(int seconds) {
	  this.duration = String.format("%02d:%02d", seconds / 60, seconds % 60);
   }

   @XmlAttribute
   public long getSize() {
	  return size;
   }

   public void setSize(long size) {
	  this.size = size;
   }

   @XmlTransient
   public String getType() {
	  return type;
   }

   public void setType(String type) {
	  this.type = type;
   }
}
