package de.cgawron.mp3.rest;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagField;
import org.jaudiotagger.tag.TagTextField;

import de.cgawron.mp3.server.Track;

@Path("/track")
public class TrackResource
{
   private static Logger logger = Logger.getLogger(TrackResource.class.toString());

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
			Tag tag = track.getTag();
			writer.append(String.format("%d (%d) tags</br>", tag.getFieldCount(), tag.getFieldCountIncludingSubValues()));
			for (FieldKey key : Track.FIELD_KEYS) {
			   List<TagField> fields = tag.getFields(key);
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
	  return Track.getById(id);
   }

   // This can be used to test the integration with the browser
   @GET
   @Path("{id}")
   @Produces({ MediaType.TEXT_HTML })
   public Track getHTML(@PathParam("id") String id) throws NumberFormatException, SQLException {
	  logger.info("id=" + id);
	  return Track.getById(id);
   }

   @GET
   @Path("{id}/content")
   public Response getFile(@PathParam("id") String id) throws SQLException {
	  Track track = Track.getById(id);

	  File f = track.getPath().toFile();

	  ResponseBuilder response = Response.ok((Object) f);
	  response.type("audio/mpeg");
	  return response.build();
   }
}
