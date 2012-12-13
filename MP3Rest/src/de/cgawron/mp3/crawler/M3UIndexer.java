/*******************************************************************************
 * Copyright (c) 2012 Christian Gawron.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package de.cgawron.mp3.crawler;

import java.net.URI;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;

import de.cgawron.didl.model.AudioItem;
import de.cgawron.didl.model.ExternalRes;
import de.cgawron.didl.model.Item;
import de.cgawron.didl.model.Res;

public class M3UIndexer implements Indexer
{

   @Override
   public List<String> mimeTypesSupported() {
	  return Arrays.asList("audio/x-mpegurl");
   }

   @Override
   public Item indexFile(EntityManager em, Path path, String mimeType) throws Exception {
	  // ToDo
	  String url = "http://dradio_mp3_dlf_m.akacast.akamaistream.net/7/249/142684/v1/gnl.akacast.akamaistream.net/dradio_mp3_dlf_m";
	  UUID id = UUID.nameUUIDFromBytes(url.getBytes());
	  Item track = new AudioItem(id.toString(), null);
	  track.setTitle(path.getFileName().toString());
	  Res res = new ExternalRes(new URI(url), "audio/mpeg");
	  track.addResource(res);
	  return track;
   }

}
