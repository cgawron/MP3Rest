package de.cgawron.mp3.rest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.sql.SQLException;
import java.util.logging.Logger;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;

import de.cgawron.didl.model.BlobRes;
import de.cgawron.didl.model.FileRes;

@Path("/")
public class StreamResource
{
   private static Logger logger = Logger.getLogger(StreamResource.class.toString());

   @Context
   UriInfo uriInfo;

   @GET
   @Path("track/{id}/content")
   public Response getFile(@PathParam("id") String id, @HeaderParam("Range") String range) throws SQLException, NamingException,
   MalformedURLException, IOException {
	  ResponseBuilder response;
	  InitialContext ic = new InitialContext();
	  EntityManagerFactory entityManagerFactory = (EntityManagerFactory) ic.lookup("java:/MP3Rest");
	  EntityManager em = entityManagerFactory.createEntityManager();

	  logger.info("id=" + id + ", range=" + range);

	  // Query query = em.createNativeQuery("select * from res where uri = '" +
	  // key + "'", Res.class);

	  FileRes res = em.find(FileRes.class, id);
	  logger.info("res=" + res);

	  if (res == null) {
		 response = Response.noContent();
	  }
	  else {
		 URI uri = res.getInternalUri();
		 InputStream stream = uri.toURL().openConnection().getInputStream();
		 if (range != null && range.length() > 0) {
			int offset = range.indexOf('-');
			int skip = Integer.parseInt(range.substring("bytes=".length(), offset));
			stream.skip(skip);
		 }
		 response = Response.ok(stream);
		 response.type("audio/mpeg");
		 response.header("Accept-Ranges", "bytes");
	  }

	  return response.build();
   }

   @GET
   @Path("blob/{id}/content")
   public Response getBlob(@PathParam("id") String id, @HeaderParam("Range") String range) throws SQLException, NamingException,
   MalformedURLException, IOException {
	  ResponseBuilder response;
	  InitialContext ic = new InitialContext();
	  EntityManagerFactory entityManagerFactory = (EntityManagerFactory) ic.lookup("java:/MP3Rest");
	  EntityManager em = entityManagerFactory.createEntityManager();

	  logger.info("id=" + id + ", range=" + range);
	  EntityTransaction ta = em.getTransaction();
	  ta.begin();
	  BlobRes res = em.find(BlobRes.class, id);
	  logger.info("res=" + res);

	  if (res == null) {
		 response = Response.noContent();
	  }
	  else {
		 byte[] content = res.getBlob();
		 InputStream stream = new ByteArrayInputStream(content);
		 if (range != null && range.length() > 0) {
			int offset = range.indexOf('-');
			int skip = Integer.parseInt(range.substring("bytes=".length(), offset));
			stream.skip(skip);
		 }
		 response = Response.ok(stream);
		 response.type(res.getMimeType());
		 response.header("Accept-Ranges", "bytes");
	  }
	  ta.commit();
	  return response.build();
   }
}
