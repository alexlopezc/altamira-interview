/**
 * 
 */
package com.altamira.file;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.topografix.gpx._1._1.GpxType;

/**
 * Example of web service based on REST which unmarshall a 
 * file debined by the url and marshalls to the web
 * 
 * @author alex
 *
 */
@Path("/xml/file")
public class FilesWebService {
	
	
	/**
	 * Default empty constructor - toot simple to do any thing
	 */
	public FilesWebService() {
	}

	
	/**
	 * @param filename name of the file being loaded
	 * @return JAXB object representing the information of the file
	 * @throws Exception
	 */
	@GET
	@Path("/{filename}")
	@Produces (MediaType.APPLICATION_XML)
	public GpxType getFileByName(@PathParam("filename") String filename) throws Exception {		
		GpxFile gpxFile = new GpxFile(filename);
		gpxFile.readGpxFile();
		return gpxFile.getData();
	}
	
	
}
