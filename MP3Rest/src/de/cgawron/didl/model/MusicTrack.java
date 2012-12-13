/*******************************************************************************
 * Copyright (c) 2012 Christian Gawron.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package de.cgawron.didl.model;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import java.util.logging.Logger;

import javax.persistence.Entity;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/*
 *   artist upnp No
 album upnp No
 originalTrackNumber upnp No
 playlist upnp No
 storageMedium upnp No
 contributor dc No
 date dc No
 */

@XmlRootElement(name = "item")
@Entity
public class MusicTrack extends AudioItem
{
   private static Logger logger = Logger.getLogger(MusicTrack.class.toString());

   private String album;

   private int originalTrackNumber;
   private int originalDiscNumber;

   public MusicTrack()
   {
	  super();
	  setClazz(DIDLObject.MUSICTRACK);
   }

   public MusicTrack(Container container, Path path, String mimeType) throws IOException, URISyntaxException
   {
	  super(path.toString(), container);
	  setClazz(DIDLObject.MUSICTRACK);
	  logger.info("calling setMetadata");
   }

   @XmlElement(namespace = NS_UPNP)
   public String getAlbum() {
	  return album;
   }

   public void setAlbum(String album) {
	  this.album = album;
   }

   @XmlElement(namespace = NS_UPNP)
   public int getOriginalTrackNumber() {
	  return originalTrackNumber;
   }

   public void setOriginalTrackNumber(int originalTrackNumber) {
	  this.originalTrackNumber = originalTrackNumber;
   }

   public void setOriginalDiscNumber(int originalDiscNumber)
   {
	  this.originalDiscNumber = originalDiscNumber;
   }

   @XmlElement(namespace = NS_UPNP)
   public int getOriginalDiscNumber()
   {
	  return originalDiscNumber;
   }

   @XmlTransient
   @Transient
   @Override
   public int getIndex()
   {
	  return 1000 * originalDiscNumber + originalTrackNumber;
   }

   public static UUID uuidForPath(Path path) throws IOException
   {
	  byte[] hash;
	  FileChannel fc = FileChannel.open(path, StandardOpenOption.READ);
	  MappedByteBuffer buffer = fc.map(MapMode.READ_ONLY, 0, Files.size(path));

	  try {
		 MessageDigest md = MessageDigest.getInstance("MD5"); //$NON-NLS-1$
		 md.update(buffer);
		 hash = md.digest();
		 fc.close();
	  } catch (NoSuchAlgorithmException e) {
		 throw new AssertionError(e);
	  }

	  long msb = (hash[0] & 0xFFL) << 56;
	  msb |= (hash[1] & 0xFFL) << 48;
	  msb |= (hash[2] & 0xFFL) << 40;
	  msb |= (hash[3] & 0xFFL) << 32;
	  msb |= (hash[4] & 0xFFL) << 24;
	  msb |= (hash[5] & 0xFFL) << 16;
	  msb |= (hash[6] & 0x0FL) << 8;
	  msb |= (0x3L << 12); // set the version to 3
	  msb |= (hash[7] & 0xFFL);

	  long lsb = (hash[8] & 0x3FL) << 56;
	  lsb |= (0x2L << 62); // set the variant to bits 01
	  lsb |= (hash[9] & 0xFFL) << 48;
	  lsb |= (hash[10] & 0xFFL) << 40;
	  lsb |= (hash[11] & 0xFFL) << 32;
	  lsb |= (hash[12] & 0xFFL) << 24;
	  lsb |= (hash[13] & 0xFFL) << 16;
	  lsb |= (hash[14] & 0xFFL) << 8;
	  lsb |= (hash[15] & 0xFFL);
	  return new UUID(msb, lsb);
   }

   @Override
   public String toString() {
	  return "MusicTrack [album=" + album + ", originalTrackNumber=" + originalTrackNumber + ", artists=" + artists + ", toString()=" +
		     super.toString() + "]";
   }

}
