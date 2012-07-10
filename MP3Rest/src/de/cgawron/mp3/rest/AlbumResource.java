package de.cgawron.mp3.rest;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import de.cgawron.mp3.server.Album;
import de.cgawron.mp3.server.Track;

@Path("/album")
public class AlbumResource
{
   private static final String APPLICATION_XSPF_XML = "application/xspf+xml";
   private static Logger logger = Logger.getLogger(AlbumResource.class.toString());

   @Provider
   @Produces({ MediaType.TEXT_HTML })
   public static class HtmlWriter implements MessageBodyWriter<Album>
   {

	  @Override
	  public long getSize(Album album, Class<?> clazz, Type type, Annotation[] annotations, MediaType mediatype) {
		 return -1;
	  }

	  @Override
	  public boolean isWriteable(Class<?> clazz, Type type, Annotation[] annotations, MediaType mediatype) {
		 logger.info("class: " + clazz.getName() + ", type: " + type.toString());
		 return clazz.equals(Album.class);
	  }

	  @Override
	  public void writeTo(Album album, Class<?> clazz, Type type, Annotation[] annotations, MediaType mediatype,
		                  MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException,
	  WebApplicationException {
		 logger.info("class: " + clazz.getName() + ", type: " + type.toString());
		 OutputStreamWriter writer = new OutputStreamWriter(entityStream);
		 writer.append("<?xml version='1.0' encoding='UTF-8'?>");
		 writer.append("<html>");
		 writer.append("<h1>Album " + album.title + "</h1>");
		 try {
			List<Integer> trackIDs = album.getTracks();
			for (int id : trackIDs) {
			   Track track = Track.getById(id);
			   writer.append(String.format("<p><a href='../track/%d'>%2d %s</a></p>",
				                           track.getTrackId(), track.getTrackNo(), track.getTitle()));
			}
		 } catch (SQLException e) {
			throw new RuntimeException(e);
		 }
		 writer.append("</html>");
		 writer.close();
	  }

   }

   @Provider
   @Produces({ MediaType.TEXT_HTML })
   public static class ListHtmlWriter implements MessageBodyWriter<List<Album>>
   {

	  @Override
	  public long getSize(List<Album> albums, Class<?> clazz, Type type, Annotation[] annotations, MediaType mediatype) {
		 return -1;
	  }

	  @Override
	  public boolean isWriteable(Class<?> clazz, Type type, Annotation[] annotations, MediaType mediatype) {
		 logger.info("class: " + clazz.getName() + ", type: " + type.toString());
		 if (type instanceof ParameterizedType) {
			ParameterizedType gt = (ParameterizedType) type;
			return gt.getActualTypeArguments()[0].equals(Album.class);
		 }
		 else {
			return false;
		 }
	  }

	  @Override
	  public void writeTo(List<Album> albums, Class<?> clazz, Type type, Annotation[] annotations,
		                  MediaType mediatype, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
	  throws IOException, WebApplicationException {
		 logger.info("class: " + clazz.getName() + ", type: " + type.toString());
		 OutputStreamWriter writer = new OutputStreamWriter(entityStream);
		 writer.append("<?xml version='1.0' encoding='UTF-8'?>");
		 writer.append("<html>");
		 writer.append("<h1>Albums</h1>");
		 writer.append("<table>");
		 for (Album album : albums) {
			writer.append(String.format("<tr><td><a href=\"album/%d\">%s</a></td>", album.albumId, album.title));
			writer.append(String.format("<td><a href=\"album/%d/xspf\">xspf</a></td></tr>", album.albumId));
		 }
		 writer.append("</table>");
		 writer.append("</html>");
		 writer.close();
	  }
   }

   @Provider
   @Produces({ APPLICATION_XSPF_XML })
   public static class XSPFWriter implements MessageBodyWriter<Album>
   {
	  @Override
	  public long getSize(Album album, Class<?> clazz, Type type, Annotation[] annotations, MediaType mediatype) {
		 return -1;
	  }

	  @Override
	  public boolean isWriteable(Class<?> clazz, Type type, Annotation[] annotations, MediaType mediatype) {
		 logger.info("class: " + clazz.getName() + ", type: " + type.toString());
		 return clazz.equals(Album.class);
	  }

	  @Override
	  public void writeTo(Album album, Class<?> clazz, Type type, Annotation[] annotations,
		                  MediaType mediatype, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
	  throws IOException, WebApplicationException {
		 logger.info("class: " + clazz.getName() + ", type: " + type.toString());
		 OutputStreamWriter writer = new OutputStreamWriter(entityStream);
		 writer.append("<?xml version='1.0' encoding='UTF-8'?>");
		 writer.append("<playlist version=\"1\" xmlns=\"http://xspf.org/ns/0/\">\n");
		 writer.append("<trackList>");
		 try {
			List<Integer> trackIDs = album.getTracks();
			for (int id : trackIDs) {
			   Track track = Track.getById(id);
			   writer.append(String.format("<track><title>%s</title><location>%s/track/%d/content</location></track>",
				                           track.getTitle(), "http://192.168.10.2:8080/MP3Rest/mp3", track.getTrackId()));
			}
		 } catch (SQLException e) {
			throw new RuntimeException(e);
		 }
		 writer.append("</trackList>");
		 writer.append("</playlist>");
		 writer.close();
	  }
   }

   @GET
   @Path("{id}")
   @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
   public Album getXML(@PathParam("id") String id) {
	  logger.info("id=" + id);
	  return new Album();
   }

   @GET
   @Path("{id}")
   @Produces({ MediaType.TEXT_HTML })
   public Album getHTML(@PathParam("id") String id) throws NumberFormatException, SQLException {
	  logger.info("id=" + id);
	  return Album.getById(id);
   }

   @GET
   @Path("{id}/xspf")
   @Produces({ APPLICATION_XSPF_XML })
   public Response getXSPF(@PathParam("id") String id) throws NumberFormatException, SQLException {
	  logger.info("xspf for id=" + id);
	  Album album = Album.getById(id);

	  ResponseBuilder response = Response.ok(album, APPLICATION_XSPF_XML);
	  response.header("Content-Disposition", String.format("attachment; filename=\"%d.xspf\"", album.albumId));
	  return response.build();
   }

   // This can be used to test the integration with the browser
   @GET
   @Produces({ MediaType.TEXT_HTML, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
   public List<Album> listAlbums(@QueryParam("conductor") String conductor) throws SQLException {
	  logger.info("conductor: " + conductor);
	  List<String> clauses = new ArrayList<String>();
	  List<String> args = new ArrayList<String>();
	  List<Album> albums = Album.getAll(clauses, args);
	  return albums;
   }

}
