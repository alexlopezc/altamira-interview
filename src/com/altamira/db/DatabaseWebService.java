/**
 * 
 */
package com.altamira.db;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.topografix.gpx._1._1.GpxType;

/**
 * Example of web service based on REST which retrieves information from DB 
 * and marshalls to the web as a service
 * 
 * @author alex
 *
 */
@Path("/xml/db")
public class DatabaseWebService {
	
	
	/*Default empty constructor*/
	public DatabaseWebService() {
	}

	
	@GET
	@Path("/{gpxId}")
	@Produces (MediaType.APPLICATION_XML)
	public GpxType getById(@PathParam("gpxId") String gpxId) throws Exception {	
		return DbUtils.getInstance().getGpxType(Integer.parseInt(gpxId));
	}
	
	
}
