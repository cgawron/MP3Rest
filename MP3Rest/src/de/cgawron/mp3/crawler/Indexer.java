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
