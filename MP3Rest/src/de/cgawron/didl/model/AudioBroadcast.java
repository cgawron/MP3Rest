/*******************************************************************************
 * Copyright (c) 2012 Christian Gawron.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package de.cgawron.didl.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(DIDLObject.AUDIOBROADCAST)
public class AudioBroadcast extends AudioItem
{
   private String region;
   private String radioCallSign;
   private String radioStationID;
   private String radioBand;
   private int channelNr;

   public AudioBroadcast()
   {
	  setClazz(AUDIOBROADCAST);
   }

   public AudioBroadcast(String id, Container parent, String radioCallSign, Res res)
   {
	  super(id, parent);
	  setClazz(AUDIOBROADCAST);
	  this.radioCallSign = radioCallSign;
	  addResource(res);
   }

   public String getRegion() {
	  return region;
   }

   public void setRegion(String region) {
	  this.region = region;
   }

   public String getRadioCallSign() {
	  return radioCallSign;
   }

   public void setRadioCallSign(String radioCallSign) {
	  this.radioCallSign = radioCallSign;
   }

   public String getRadioStationID() {
	  return radioStationID;
   }

   public void setRadioStationID(String radioStationID) {
	  this.radioStationID = radioStationID;
   }

   public String getRadioBand() {
	  return radioBand;
   }

   public void setRadioBand(String radioBand) {
	  this.radioBand = radioBand;
   }

   public int getChannelNr() {
	  return channelNr;
   }

   public void setChannelNr(int channelNr) {
	  this.channelNr = channelNr;
   }

}
