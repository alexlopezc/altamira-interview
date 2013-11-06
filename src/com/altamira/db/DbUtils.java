package com.altamira.db;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.GregorianCalendar;

import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;
import com.topografix.gpx._1._1.GpxType;
import com.topografix.gpx._1._1.LinkType;
import com.topografix.gpx._1._1.MetadataType;


/**
 * Utils for connecting to MySql database using JDBC
 * @author alex
 *
 */
public class DbUtils {
	/**
	 * Global connection for anyone who wants to access to DB.
	 * It should be treated with synchronized, etc
	 */
	private static Connection conn = null;

	/**
	 * SQL query for retrieving all segments for a track
	 */
	private final static String GET_HEADER = 
			"SELECT LINK, TEXT, TIME "+
			"FROM `TRACK` t WHERE t.track_id = %PARAM%";		

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
	
	public DbUtils() {

	}

	/**
	 * This method should be call whenever an object wants to access the db
	 * There is only one, so the calls to any method should be controlled with 
	 * synchronized methods or similar mechanism
	 * @return Connection to mysql DB
	 */
	public static Connection getConnection (){
		//synchronized (conn) {
		if (conn == null) {
			try {
				String url = System.getenv("OPENSHIFT_MYSQL_DB_URL");
				conn = DriverManager.getConnection("jdbc:"+url);

			} catch (SQLException ex) {
				// handle any errors
				System.out.println("SQLException: " + ex.getMessage());
				System.out.println("SQLState: " + ex.getSQLState());
				System.out.println("VendorError: " + ex.getErrorCode());
			}
		}
		//}
		return conn;
	}


	public GpxType getGpxType(int id) {
		GpxType gpxInfo = new GpxType();


		return gpxInfo;
	}

	public MetadataType getMetadata(int id){
		Statement stmt = null;
		ResultSet rs = null;
		MetadataType metadata = new MetadataType();

		try {
			stmt = DbUtils.getConnection().createStatement();
			String query = GET_HEADER.replace("%PARAM%", Integer.toString(id));

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
		return metadata;
	}

	public int[] getTrackSegments(int id){
		Statement stmt = null;
		ResultSet rs = null;
		try {
			stmt = DbUtils.getConnection().createStatement();
			String query = GET_SEGMENTS.replace("%PARAM%", Integer.toString(id));

			if (stmt.execute(query)) {
				rs = stmt.getResultSet();
			}
			// Now do something with the ResultSet ....
		}
		catch (SQLException ex){
			// handle any errors
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		} finally {
			//  release resources	
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
		return null;
	}

	public int[] getTrackPoints(int id){
		Statement stmt = null;
		ResultSet rs = null;
		try {
			stmt = DbUtils.getConnection().createStatement();
			String query = GET_POINTS.replace("%PARAM%", Integer.toString(id));

			if (stmt.execute(query)) {
				rs = stmt.getResultSet();
			}
			// Now do something with the ResultSet ....
		}
		catch (SQLException ex){
			// handle any errors
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		} finally {
			//  release resources	
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
		return null;
	}

}
