package com.altamira.db;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;
import com.topografix.gpx._1._1.GpxType;
import com.topografix.gpx._1._1.LinkType;
import com.topografix.gpx._1._1.MetadataType;
import com.topografix.gpx._1._1.TrkType;
import com.topografix.gpx._1._1.TrksegType;
import com.topografix.gpx._1._1.WptType;


/**
 * Utils for connecting to MySql database using JDBC and retrieving info from DB
 * @author alex
 *
 */
public class DbUtils {
	
	/**
	 * Singleton for anyone who wants to access to DB.
	 * It should be treated with synchronized, etc
	 */
	private static DbUtils db;

	/**
	 * Connection with the info of the source database
	 */
	private Connection conn = null;

	/**
	 * SQL query for retrieving all segments for a track
	 */
	private final static String GET_HEADER = 
			"SELECT LINK, TEXT, TIME "+
			"FROM `GPX` g WHERE g.gpx_id = %PARAM%";		
	
	/**
	 * SQL query for retrieving all segments for a track
	 */
	private final static String GET_TRACKS = 
			"SELECT TRACK_ID, NAME "+
			"FROM `TRACKS` t WHERE t.gpx_id = %PARAM%";		

	/**
	 * SQL query for retrieving all segments for a track
	 */
	private final static String GET_SEGMENTS = 
			"SELECT TRACK_SEGMENT_ID "+
			"FROM TRACK_SEGMENTS ts WHERE ts.track_id = %PARAM%";
	
	/**
	 * SQL query for retrieving all points for a segment
	 */
	private final static String GET_POINTS = 
			"SELECT LATITUDE, LONGITUDE, ELEVATION, TIME "+
			"FROM TRACK_POINTS tP WHERE tP.track_segment_id = 1";
	
	/**
	 * This constructor should retrieve information of the connection from a .properties file
	 * to set the configuration of the object Connection conn
	 */
	public DbUtils() {
		try {
			// The newInstance() call is a work around for some
			// broken Java implementations
			Class.forName("com.mysql.jdbc.Driver").newInstance();

			String host = System.getenv("OPENSHIFT_MYSQL_DB_HOST");
			String port = System.getenv("OPENSHIFT_MYSQL_DB_PORT");
			String user = System.getenv("OPENSHIFT_MYSQL_DB_USERNAME");
			String password = System.getenv("OPENSHIFT_MYSQL_DB_PASSWORD");
			
			String url = "jdbc:mysql://"+host+":"+port+"/altamira";
			System.out.println(url);
			conn = DriverManager.getConnection(url, user, password);

		} catch (Exception ex) {
			// handle any errors
			System.out.println("SQLException: " + ex.getMessage());
		}

	}

	/**
	 * This method should be call whenever an object wants to access the db
	 * There is only one, so the calls to any method should be controlled with 
	 * synchronized methods or similar mechanism
	 * It is a Singleton
	 * @return Connection to mysql DB
	 */
	public static DbUtils getInstance (){
		if (db == null) {
			db = new DbUtils();
		}
		return db;
	}

	/**
	 * Release resources after the call to the DB
	 * @param rs The resultset
	 * @param stmt The statement
	 */
	public void realeaseResources(ResultSet rs, Statement stmt) {
		if (rs != null) {
			try {
				rs.close();
			} catch (SQLException sqlEx) { } // ignore
			rs = null; }
		if (stmt != null) {
			try {
				stmt.close();
			}  catch (SQLException sqlEx) { } // ignore
			stmt = null;
		}
	}

	/**
	 * Return the object with all the information related to the GPX file retrieved from DB
	 * @param id Identifier of the gpx file which will be read from URL
	 * @return JAXB Object representing the track
	 */
	public GpxType getGpxType(int gpxId) {
		GpxType gpxInfo = new GpxType();

		// If the track is empty, return null GPX file
		List<TrkType> list = getTracks(gpxId);
		if (list.size() == 0) {
			gpxInfo = null;
		} else {
			gpxInfo.getTrk().addAll(list);
			gpxInfo.setCreator("Alex");
			gpxInfo.setVersion("1.1");
			gpxInfo.setMetadata(getMetadata(gpxId));
		}
		
		return gpxInfo;
	}
	

	/** Metada info of the gpx file
	 * @param id Identifier of the gpx file which will be read from URL
	 * @return Metadata information 
	 */
	public MetadataType getMetadata(int gpxId){
		Statement stmt = null;
		ResultSet rs = null;
		MetadataType metadata = new MetadataType();

		try {
			stmt = conn.createStatement();
			String query = GET_HEADER.replace("%PARAM%", Integer.toString(gpxId));
			System.out.println(query);
			
			if (stmt.execute(query)) {
				rs = stmt.getResultSet();
				if (rs.next()) {
					LinkType link = new LinkType();
					link.setHref(rs.getString("LINK"));
					link.setText(rs.getString("TEXT"));
					metadata.getLink().add(link);
					GregorianCalendar cal = new GregorianCalendar();
					cal.setTimeInMillis(rs.getTimestamp("TIME").getTime());
					metadata.setTime(new XMLGregorianCalendarImpl(cal));
				}
			}

		}
		catch (SQLException ex){
			// handle any errors
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		} finally {
			//  release resources	
			realeaseResources(rs, stmt);
		}
		return metadata;
	}

	
	/**
	 * Return all the tracks for an defined GPX file
	 * In this example there will be only one TrkType, but it just a matter of fulfill the DB
	 * @param id Identifier of the track which will be read from URL
	 * @return 
	 */
	public List<TrkType> getTracks(int gpxId) {
		Statement stmt = null;
		ResultSet rs = null;

		List<TrkType> trks = new ArrayList<TrkType>();  // list of tracks returned

		try {
			stmt = conn.createStatement();
			String query = GET_TRACKS.replace("%PARAM%", Integer.toString(gpxId));
			System.out.println(query);

			if (stmt.execute(query)) {
				rs = stmt.getResultSet();
				// For each track, retrieve the name and all the segments of the track
				while (rs.next()) {
					TrkType trk = new TrkType();
					trk.setName(rs.getString("NAME"));
					trk.getTrkseg().addAll(getTrackSegments(rs.getInt("TRACK_ID")));
					trks.add(trk);
				}
			}

		}
		catch (SQLException ex){
			// handle any errors
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		} finally {
			//  release resources	
			realeaseResources(rs, stmt);
		}
		return trks;
	}
	
	
	/**
	 * Retrieve segments from db for a defined track
	 * @param trackId Identifier of the track
	 * @return List of Segments for the track
	 */
	public List<TrksegType> getTrackSegments(int trackId){
		Statement stmt = null;
		ResultSet rs = null;
		
		List<TrksegType> segments = new ArrayList<TrksegType>(); // list of segments
		try {
			stmt = conn.createStatement();
			String query = GET_SEGMENTS.replace("%PARAM%", Integer.toString(trackId));
			System.out.println(query);

			if (stmt.execute(query)) {
				rs = stmt.getResultSet();
				// For each segment, retrieve all the points
				while (rs.next()) {
					TrksegType segment = new TrksegType();
					segment.getTrkpt().addAll(
							getTrackPoints(rs.getInt("TRACK_SEGMENT_ID")));
					segments.add(segment);
				}
			}
		}
		catch (SQLException ex){
			// handle any errors
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		} finally {
			//  release resources	
			realeaseResources(rs, stmt);
		}
		return segments;
	}

	
	/**
	 * Get all the points related to a TrackSegment
	 * @param segmentId Identifier for the segment to be retrieved
	 * @return List of points
	 */
	public List<WptType> getTrackPoints(int segmentId){
		Statement stmt = null;
		ResultSet rs = null;
		
		List<WptType> points = new ArrayList<WptType>(); // list of points
		try {
			stmt = conn.createStatement();
			String query = GET_POINTS.replace("%PARAM%", Integer.toString(segmentId));
			System.out.println(query);

			if (stmt.execute(query)) {
				rs = stmt.getResultSet();
				while (rs.next()) {
					WptType point = new WptType();
					point.setEle(rs.getBigDecimal("ELEVATION"));

					GregorianCalendar cal = new GregorianCalendar();
					cal.setTimeInMillis(rs.getTimestamp("TIME").getTime());
					point.setTime(new XMLGregorianCalendarImpl(cal));

					point.setLat(rs.getBigDecimal("LATITUDE"));
					point.setLon(rs.getBigDecimal("LONGITUDE"));
					points.add(point);
				}
			}
		}
		catch (SQLException ex){
			// handle any errors
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		} finally {
			//  release resources	
			realeaseResources(rs, stmt);
		}
		return points;
	}

}
