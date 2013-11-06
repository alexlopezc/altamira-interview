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
 * Example of web service based on REST which unmarshall froms disk
 * and marshalls to the web
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
	@Path("/{id}")
	@Produces (MediaType.APPLICATION_XML)
	public GpxType getById(@PathParam("id") String id) throws Exception {	
		return null;
	}
	
	
}
