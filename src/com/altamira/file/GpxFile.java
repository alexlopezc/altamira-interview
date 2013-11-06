package com.altamira.file;

import java.net.URL;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.ValidationEventLocator;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import com.topografix.gpx._1._1.GpxType;

/**
 * @author alex
 *
 */
public class GpxFile {

	private final static String jaxbContextPath = "com.topografix.gpx._1._1";

	private String filename;
	private GpxType data;


	/**
	 * @param filename
	 */
	public GpxFile(String filename) {
		super();
		this.filename = filename+".gpx";
	}


	/**
	 * @return the filename
	 */
	protected String getFilename() {
		return filename;
	}


	/**
	 * @param filename the filename to set
	 */
	protected void setFilename(String filename) {
		this.filename = filename;
	}


	/**
	 * @return the data
	 */
	protected GpxType getData() {
		return data;
	}


	/**
	 * @param data the data to set
	 */
	protected void setData(GpxType data) {
		this.data = data;
	}


	public GpxType readGpxFile () throws Exception {
		Object poe = null;
		try {
			// Create JAXBContext objects to convert file to object
			JAXBContext jc = JAXBContext.newInstance(jaxbContextPath);
			Unmarshaller u = jc.createUnmarshaller();

			SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			try {
				//String schemaTxT = fileSchemaMap.get( new Integer( 1 ) );
				URL url = getClass().getClassLoader().getResource( "gpx.xsd" );
				Schema schema = sf.newSchema( url );
				u.setSchema( schema );
				u.setEventHandler( 
						new ValidationEventHandler() {
							// allow unmarshalling to continue even if there are errors
							public boolean handleEvent( ValidationEvent ve ) {
								// ignore warnings
								if( ve.getSeverity() != ValidationEvent.WARNING ) {
									ValidationEventLocator vel = ve.getLocator();
									System.out.println( "Line:Col[" + vel.getLineNumber() + ":" + vel.getColumnNumber() + "]:" + ve.getMessage() );
									//throw new XmlValidationException( "Line:Col[" + vel.getLineNumber() + ":" + vel.getColumnNumber() + "]:" + ve.getMessage() );
								}
								return true;
							}
						} 
						);

			} catch ( org.xml.sax.SAXException se )	{
				System.out.println( "Unable to validate due to following error." );
				se.printStackTrace();
			}

			// pass validation.
			URL urlFile = getClass().getClassLoader().getResource( this.filename );

			poe = u.unmarshal( urlFile );
			if (poe instanceof JAXBElement<?>){
				if(((JAXBElement<?>) poe).getValue() instanceof GpxType){
					data = (GpxType) ((JAXBElement<?>) poe).getValue();
				} else {
					throw new Exception("Unkown xml type");
				}
				
			}			
			
		} catch ( UnmarshalException ue ) {
			System.out.println( ue.getMessage() );

		} catch ( JAXBException je ) {
			System.out.println( je.getMessage() );
		}

		return data;
	}

}
