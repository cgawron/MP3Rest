package de.cgawron.mp3.rest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URI;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.TagField;
import org.jaudiotagger.tag.TagTextField;

import de.cgawron.mp3.server.upnp.model.Res;

@Path("/track")
public class StreamResource
{
   private static Logger logger = Logger.getLogger(StreamResource.class.toString());

   @Context
   UriInfo uriInfo;

   private class Track extends de.cgawron.mp3.server.Track
   {
	  public URI self;

	  Track(de.cgawron.mp3.server.Track track)
	  {
		 super(track);
		 self = uriInfo.getBaseUri().resolve("track/" + getTrackId());
		 logger.info("Track: self=" + self);
	  }
   }

   public Track getById(String id) throws SQLException
   {
	  de.cgawron.mp3.server.Track track = de.cgawron.mp3.server.Track.getById(id);
	  return new Track(track);
   }

   @Provider
   @Produces({ MediaType.TEXT_HTML })
   public static class HtmlWriter implements MessageBodyWriter<Track>
   {

	  @Override
	  public long getSize(Track track, Class<?> clazz, Type type, Annotation[] annotations, MediaType mediatype) {
		 return -1;
	  }

	  @Override
	  public boolean isWriteable(Class<?> clazz, Type type, Annotation[] annotations, MediaType mediatype) {
		 logger.info("class: " + clazz.getName() + ", type: " + type.toString());
		 return clazz.equals(Track.class);
	  }

	  @Override
	  public void writeTo(Track track, Class<?> clazz, Type type, Annotation[] annotations, MediaType mediatype,
		                  MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException,
	  WebApplicationException {
		 logger.info("class: " + clazz.getName() + ", type: " + type.toString());
		 OutputStreamWriter writer = new OutputStreamWriter(entityStream);
		 writer.append("<?xml version='1.0' encoding='UTF-8'?>");
		 writer.append("<html>");
		 writer.append("<h1>Track " + track.getTitle() + "</h1>");
		 try {
			// Tag tag = track.getTag();
			// writer.append(String.format("%d (%d) tags</br>",
			// tag.getFieldCount(), tag.getFieldCountIncludingSubValues()));
			for (FieldKey key : Track.FIELD_KEYS) {
			   List<TagField> fields = track.getFields(key);
			   for (TagField field : fields) {
				  if (field instanceof TagTextField) {
					 TagTextField text = (TagTextField) field;
					 writer.append(String.format("%s: %s -> %s</br>", key, field.getId(), text.getContent()));
				  }
				  else {
					 writer.append(String.format("%s: %s -> %s</br>", key, field.getId(), field.toString()));
				  }
			   }
			}
		 } catch (Exception e) {
			throw new RuntimeException(e);
		 }
		 writer.append("</html>");
		 writer.close();
	  }
   }

   // This method is called if XMLis request
   @GET
   @Path("{id}")
   @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
   public Track getXML(@PathParam("id") String id) throws SQLException {
	  logger.info("id=" + id);
	  return getById(id);
   }

   // This can be used to test the integration with the browser
   @GET
   @Path("{id}")
   @Produces({ MediaType.TEXT_HTML })
   public Track getHTML(@PathParam("id") String id) throws NumberFormatException, SQLException {
	  logger.info("id=" + id);
	  return getById(id);
   }

   @GET
   @Path("{id}/content")
   public Response getFile(@PathParam("id") String id, @HeaderParam("Range") String range) throws SQLException, NamingException,
   MalformedURLException, IOException {
	  ResponseBuilder response;
	  InitialContext ic = new InitialContext();
	  EntityManagerFactory entityManagerFactory = (EntityManagerFactory) ic.lookup("java:/MP3Rest");
	  EntityManager em = entityManagerFactory.createEntityManager();

	  logger.info("id=" + id + ", range=" + range);
	  String key = "http://192.168.10.2:8080/MP3Rest/rest/track/" + id + "/content";
	  logger.info("key=" + key);

	  // Query query = em.createNativeQuery("select * from res where uri = '" +
	  // key + "'", Res.class);
	  Res res = em.find(Res.class, id);
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
}
