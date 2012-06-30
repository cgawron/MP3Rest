package de.cgawron.mp3.server;

import static java.nio.file.FileVisitResult.CONTINUE;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Crawler
{
	private static Logger logger = Logger.getLogger(Crawler.class.toString());

	private static Path root = FileSystems.getDefault().getPath("/opt/mp3");
	// private static String jdbcUrl = "jdbc:db2:Music";
	private static String jdbcUrl = "jdbc:derby:WEB-INF/music";

	private static Connection con;

	static class MyFileVisitor extends SimpleFileVisitor<Path>
	{
		int numFiles = 0;

		PreparedStatement insertFile;
		PreparedStatement queryFile;
		PreparedStatement updateFile;

		MyFileVisitor() throws SQLException
		{
			con = getConnection();
			insertFile = con.prepareStatement("INSERT INTO CRAWLER (PATH, MODIFIED, STATE) values (?, ?, ?) ");
			queryFile = con.prepareStatement("SELECT MODIFIED, STATE FROM CRAWLER WHERE PATH=? ");
			updateFile = con.prepareStatement("UPDATE CRAWLER SET MODIFIED=?, STATE=? WHERE PATH=? ");
		}

		@Override
		public FileVisitResult preVisitDirectory(Path path, BasicFileAttributes attr) throws IOException {
			logger.info("dir " + path);
			insertPath(path, attr);
			numFiles++;
			return CONTINUE;
		}

		@Override
		public FileVisitResult visitFile(Path path, BasicFileAttributes attr) throws IOException {
			String mimeType = Files.probeContentType(path);
			if (mimeType != null && mimeType.startsWith("audio/")) {
				logger.info("file " + path + " " + Files.probeContentType(path));
				insertPath(path, attr);
				numFiles++;
			}
			return CONTINUE;
		}

		public void insertPath(Path path, BasicFileAttributes attr) {
			try {
				queryFile.setString(1, path.toString());
				if (queryFile.executeQuery().next()) {
					logger.info("Path " + path + " already in DB");
					// TODO: Check for modification time/status
				} else {
					insertFile.setString(1, path.toString());
					insertFile.setTimestamp(2, new Timestamp(attr.lastModifiedTime().toMillis()));
					insertFile.setInt(3, 1);
					insertFile.execute();
					con.commit();
				}
			} catch (SQLException e) {
				logger.log(Level.SEVERE, "error inserting data", e);
				throw new RuntimeException(e);
			}
		}
	}

	public static Connection getConnection() {
		if (con == null) {
			try {
				con = DriverManager.getConnection(jdbcUrl);
				con.setAutoCommit(false);
			} catch (SQLException e) {
				logger.log(Level.SEVERE, "error opening JDBC connection", e);
				throw new RuntimeException(e);
			}
		}
		return con;
	}

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws Exception {
		// no daemon (yet)
		Timer timer = new Timer(false);
		TimerTask updater = new Updater();

		timer.schedule(updater, 5000, 5000);

		long millis = System.currentTimeMillis();
		MyFileVisitor visitor = new MyFileVisitor();
		Files.walkFileTree(root, visitor);
		millis = System.currentTimeMillis() - millis;
		logger.info(String.format("%d files visited in %d ms", visitor.numFiles, millis));
	}

}
