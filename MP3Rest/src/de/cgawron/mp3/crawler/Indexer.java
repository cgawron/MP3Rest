/*******************************************************************************
 * Copyright (c) 2012 Christian Gawron.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package de.cgawron.mp3.crawler;

import java.nio.file.Path;
import java.util.List;

import javax.persistence.EntityManager;

import de.cgawron.didl.model.DIDLObject;

public interface Indexer
{
   List<String> mimeTypesSupported();

   DIDLObject indexFile(EntityManager em, Path path, String mimeType) throws Exception;
}
