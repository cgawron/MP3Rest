package de.cgawron.mp3.rest;

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
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import de.cgawron.mp3.server.Album;
import de.cgawron.mp3.server.Track;

@Path("/album")
public class AlbumResource
{
	private static Logger logger = Logger.getLogger(AlbumResource.class.toString());

	@Provider
	@Produces({ MediaType.TEXT_HTML })
	public static class HtmlWriter implements MessageBodyWriter<Album>
	{

		@Override
		public long getSize(Album album, Class<?> clazz, Type type, Annotation[] annotations, MediaType mediatype) {
			// TODO Auto-generated method stub
			return -1;
		}

		@Override
		public boolean isWriteable(Class<?> clazz, Type type, Annotation[] annotations, MediaType mediatype) {
			logger.info("class: " + clazz.getName() + ", type: " + type.toString());
			// TODO Auto-generated method stub
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
				List<Track> tracks = album.getTracks();
				for (Track track : tracks) {
					writer.append("<p>" + track.getTitle() + "</p>");
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
			// TODO Auto-generated method stub
			return -1;
		}

		@Override
		public boolean isWriteable(Class<?> clazz, Type type, Annotation[] annotations, MediaType mediatype) {
			logger.info("class: " + clazz.getName() + ", type: " + type.toString());
			// TODO Auto-generated method stub
			return true;
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
			writer.append("<ul>");
			for (Album album : albums) {
				writer.append(String.format("<li><a href=\"album/%d\">%s</a></li>", album.albumId, album.title));
			}
			writer.append("</ul>");
			writer.append("</html>");
			writer.close();
		}
	}

	// This method is called if XMLis request
	@GET
	@Path("{id}")
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Album getXML(@PathParam("id") String id) {
		logger.info("id=" + id);
		return new Album();
	}

	// This can be used to test the integration with the browser
	@GET
	@Path("{id}")
	@Produces({ MediaType.TEXT_HTML })
	public Album getHTML(@PathParam("id") String id) throws NumberFormatException, SQLException {
		logger.info("id=" + id);
		return Album.getById(id);
	}

	// This can be used to test the integration with the browser
	@GET
	@Produces({ MediaType.TEXT_HTML })
	public List<Album> listHTML() throws SQLException {
		List<Album> albums = Album.getAll();
		return albums;
	}

}
