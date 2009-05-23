package com.moneybender.snarf.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.moneybender.noaa.NOAAHurricaneData;
import com.moneybender.snarf.HurricaneCenter;
import com.moneybender.snarf.gui.TextHolder;

public class StormSet {
	
	private static Logger log = Logger.getLogger(StormSet.class);

	List<WayPoint> waypoints = new LinkedList<WayPoint>();
	Map<String, List> storms = new LinkedHashMap<String, List>();
	int waypointCount = 0;
	
	public StormSet(){}
	
	public StormSet(File infile)
		throws Exception
	{
		InputStream reader = new FileInputStream(infile);
		ByteArrayOutputStream gpxStream = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int bytesRead = 0;
		while((bytesRead = reader.read(buffer)) > 0)
			gpxStream.write(buffer, 0, bytesRead);
		reader.close();
		gpxStream.close();
		new GPXParser().parseStormData(this, gpxStream.toString());
	}
	
	private class GPXParser
		extends DefaultHandler
	{
		private StringBuffer saxParserBuffer;
		
		String stormName = null;
		String name = "";
		String description = "";
		String time = "";
		float latitude = 0;
		float longitude = 0;
		
		StormSet stormSet = null;
		LinkedList<RoutePoint> route = new LinkedList<RoutePoint>();
		
		protected void parseStormData(StormSet stormSet, String gpx)
			throws Exception
		{
			if(gpx == null)
				throw new Exception("GPX XML is null");
			
			this.stormSet = stormSet;
			
			SAXParserFactory factory = SAXParserFactory.newInstance();
			factory.setValidating(false);
			SAXParser parser = factory.newSAXParser();
			if(parser == null)
				throw new Exception("Failed to create SAX parser to parse GPX data.");
			
			if(parser.getXMLReader() == null)
				throw new Exception("Failed to get XML reader from SAX parser.");
			
			parser.getXMLReader().setContentHandler(this);
			parser.getXMLReader().parse(new InputSource(new StringReader(gpx)));
			parser = null;
			factory = null;

		}
	
		public void characters(char[] ch, int start, int length)
			throws SAXException
		{
			saxParserBuffer.append(ch, start, length);
		}
	
		public void startElement(String namespaceURI, String localName, String elementName, Attributes attributes)
			throws SAXException
		{
			saxParserBuffer = new StringBuffer();
			if(attributes.getValue("lat") != null)
				latitude = Float.parseFloat(attributes.getValue("lat"));
			if(attributes.getValue("lon") != null)
				longitude = Float.parseFloat(attributes.getValue("lon"));
		}
		
		public void endElement(String namespaceURI, String localName, String elementName)
			throws SAXException
		{
			String value = new String(saxParserBuffer);
			if(elementName.equals("name")){
				this.name = value;
				if(stormName == null)
					stormName = name;
			} else if(elementName.equals("desc")){
				this.description = value;
			} else if(elementName.equals("time")){
				this.time = value;
			} else if(elementName.equals("wpt")){
				WayPoint wayPoint = new WayPoint(name, description, latitude, longitude);
				if(log.isDebugEnabled())
					log.debug("Add waypoint " + wayPoint.getName() + ":" + wayPoint.getDescription()
							+ " @ (" + latitude + ", " + longitude + ")");

				stormSet.waypoints.add(wayPoint);
				stormName = null;
				name = null;
				description = null;
				latitude = 0;
				longitude = 0;
			} else if(elementName.equals("rtept")){
				RoutePoint routePoint = new RoutePoint(name, time, latitude, longitude);
				if(log.isDebugEnabled())
					log.debug("Add routepoint " + routePoint.getName() + ":" + routePoint.getTime()
							+ " @ (" + latitude + ", " + longitude + ")");
				
				route.addFirst(routePoint);
				name = null;
				time = null;
				latitude = 0;
				longitude = 0;
			} else if(elementName.equals("rte")){
				if(log.isDebugEnabled())
					log.debug("Add storm " + stormName);
				stormSet.storms.put(stormName, route);
				route = new LinkedList<RoutePoint>();
				stormName = null;
			}
		}
	}

	public Map<String, List> getStorms() {
		return storms;
	}

	public List<WayPoint> getWaypoints() {
		return waypoints;
	}

	public void fetchStormDataFromNOAA(
			TextHolder status,
			String urlPrefix, String indexURIRoot, String indexYear,
			String gpxHeaderFileName, String gpxFileName,
			String addedCoordinatesFile, boolean showHurricanePathsAsTracks,
			String oneStormOnly, int threadCount)
		throws Exception
	{
		long start = System.currentTimeMillis();
		String gpxPostFix = "</gpx>";

		log.info("Get storms from index...");
		String indexURL = urlPrefix + "/" + indexURIRoot + "/" + indexYear + "/index.shtml";
		NOAAHurricaneData noaa = null;
		try {
			noaa = new NOAAHurricaneData(urlPrefix + "/" + indexURIRoot + "/" + indexYear + "/", indexURL);
		} catch (MalformedURLException e2) {
			e2.printStackTrace();
			throw new Exception("Error reading NOAA coordinates:" + e2.getMessage());
		} catch (IOException e2) {
			e2.printStackTrace();
			throw new Exception("Error reading NOAA coordinates:" + e2.getMessage());
		}
		
		if(noaa == null)
			throw new Exception("Error fetching NOAA index page");
		
		final HashMap storms = noaa.getStormFileNames();
		if(log.isDebugEnabled()){
			Iterator iter = storms.keySet().iterator();
			while(iter.hasNext()){
				String storm = (String)iter.next();
				String stormURL = urlPrefix + "/" + indexURIRoot + "/" + indexYear + "/" + storms.get(storm);
					log.debug("storm:" + storm + " at " + stormURL);
			}
		}
		
		// Loop to process all storms
		log.info("Create GPX file " + gpxFileName);
		FileOutputStream out = null;

		String gpxPrefix = "";
		InputStream gpxHeader;
		try {
			gpxHeader = new FileInputStream(gpxHeaderFileName);
			int bytesRead = -1;
			byte[] buffer = new byte[1024];
			StringBuffer sb = new StringBuffer();
			while((bytesRead = gpxHeader.read(buffer)) >= 0)
				sb.append(new String(buffer, 0, bytesRead));
			gpxHeader.close();
			gpxPrefix = new String(sb);
		} catch (FileNotFoundException e) {
			log.error("GPX header file not found, proceeding without it.");
		} catch (IOException e) {
			log.error(e.getMessage());
			log.error("Error reading GPX header file, proceeding without it.");
		}

		ByteArrayOutputStream pathBuffer = new ByteArrayOutputStream();
		try {
			out = new FileOutputStream(gpxFileName);
			out.write(gpxPrefix.getBytes());

			if(out == null)
				throw new Exception("Can't write GPX file:" + gpxFileName);

			try{
				writeAddedWayPoints(out, addedCoordinatesFile);
			}catch(FileNotFoundException fnf){
				log.error("File not found:" + addedCoordinatesFile);
				throw new Exception("File not found:" + addedCoordinatesFile);
			}


			// Loop through storms to write waypoints
			Map<String, LinkedList> stormCoords = new LinkedHashMap<String, LinkedList>();
			ExecutorService executor = Executors.newFixedThreadPool(threadCount);

			Iterator iter = storms.keySet().iterator();
			while(iter.hasNext()){
				String stormName = (String)iter.next();
				if(oneStormOnly != null && !oneStormOnly.equals(stormName))
					continue;
				String stormURL = urlPrefix + "/" + indexURIRoot + "/" + indexYear + "/" + storms.get(stormName);
				executor.execute(new StormChaser(status, stormCoords, noaa, stormName, stormURL));
			}
			executor.shutdown();
			executor.awaitTermination(600, TimeUnit.SECONDS);
			
			log.info("Done retrieving storm data.");

			// 'storms' and not 'stormCoords' so that the order stays the same.
			iter = storms.keySet().iterator();
			while(iter.hasNext()){
				String stormName = (String)iter.next();
				LinkedList centers = (LinkedList)stormCoords.get(stormName);

				writeStormWaypoints(out, centers, stormName);
				writeStormPath(pathBuffer, centers, stormName, showHurricanePathsAsTracks);
			}

			out.write(pathBuffer.toByteArray());
			
			out.write(gpxPostFix.getBytes());
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new Exception("Error reading NOAA coordinates:" + e.getMessage());
		} catch (IOException e1) {
			e1.printStackTrace();
			throw new Exception("Error reading NOAA coordinates:" + e1.getMessage());
		}
		
		long end = System.currentTimeMillis();
		log.info("Retrieved " + waypointCount + " coordinates total");
		log.info("Done in " + (end - start) / 1000 + " seconds.");
		
	}
	
	class StormChaser implements Runnable{
		NOAAHurricaneData noaa = null;
		String stormName = null;
		String stormURL = null;
		Map<String, LinkedList> stormCoords = null;
		TextHolder status = null;

		public StormChaser(TextHolder status, Map<String, LinkedList>stormCoords, NOAAHurricaneData noaa, String stormName, String stormURL){
			this.status = status;
			this.stormCoords = stormCoords;
			this.noaa = noaa;
			this.stormName = stormName;
			this.stormURL = stormURL;
		}

		public void run() {
			if(log.isDebugEnabled())
				log.debug("StormURL:" + stormURL);
			try{
				log.info(stormName);
				status.setText(stormName);
				LinkedList centers = processStorm(noaa, stormName);
				waypointCount += centers.size();
				
				stormCoords.put(stormName, centers);
			}catch(Exception e){
				log.error("Error processing storm " + stormName + ":" + e.getMessage());
			}
		}
		
	}


	private void writeAddedWayPoints(OutputStream out, String addedCoordinatesFile)
		throws IOException
	{
		InputStream addedCoordinates = new FileInputStream(addedCoordinatesFile);
		int bytesRead = -1;
		byte[] buffer = new byte[1024];
		while((bytesRead = addedCoordinates.read(buffer)) >= 0)
			out.write(buffer, 0, bytesRead);
		addedCoordinates.close();
	}
	
	private void writeStormWaypoints(OutputStream out, LinkedList centers, String stormName)
		throws IOException
	{
		SimpleDateFormat outFormat = new SimpleDateFormat("yyyy/MM/dd HHmm");
		
		// Write all waypoints
		String tmp;
		String shortName = makeShortName(stormName);
		Iterator iter = centers.iterator();
		int count = 1;
		while(iter.hasNext()){
			HurricaneCenter center = (HurricaneCenter)iter.next();
			String countString = "000" + count;
			countString = countString.substring(countString.length() - 3);
			tmp = "<wpt lat=\"" + center.getLatitude()
			+ "\" lon=\"" + center.getLongitude() + "\">\n";
			out.write(tmp.getBytes());
			
			String formattedDate = null;
			try{
				formattedDate = outFormat.format(center.getDate());

				out.write("\t<ele>0.000000</ele>\n".getBytes());
				out.write(("\t<name>" + shortName + countString + "</name>\n").getBytes());
				out.write(("\t<cmt>" + formattedDate + "Z; " + center.getWindSpeed() + " MPH</cmt>\n").getBytes());
				out.write(("\t<desc>" + formattedDate + "Z; " + center.getWindSpeed() + " MPH</desc>\n").getBytes());
				out.write("\t<sym>crossed square</sym>\n".getBytes());
				out.write("</wpt>\n".getBytes());

				++count;
			}catch(NullPointerException e) {
				log.error("Error formatting date '" + center.getDate() + "' for storm '" + stormName + "'");
			}
			
		}
	}
	
	private void writeStormPath(OutputStream out, LinkedList centers,
			String stormName, boolean showHurricanePathAsTrack)
		throws IOException
	{
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
		String shortName = makeShortName(stormName);
		if(showHurricanePathAsTrack){
			out.write("<trk>\n".getBytes());
			out.write(("\t<name>" + stormName + "</name>\n").getBytes());
			out.write("<trkseg>\n".getBytes());
		}
		else{
			out.write("<rte>\n".getBytes());
			out.write(("\t<name>" + stormName + "</name>\n").getBytes());
		}
		Iterator iter = centers.iterator();
		int count = 1;
		while(iter.hasNext()){
			HurricaneCenter center = (HurricaneCenter)iter.next();
			String countString = "0000" + count;
			countString = countString.substring(countString.length() - 3);
			if(showHurricanePathAsTrack)
				out.write("\t<trkpt".getBytes());
			else
				out.write("\t<rtept".getBytes());
			out.write((" lat=\"" + center.getLatitude() + "\" lon=\""
					+ center.getLongitude() + "\">\n").getBytes());
			out.write(("\t\t<name>" + shortName + countString + "</name>\n").getBytes());
			out.write(("\t\t<time>" + dateFormat.format(center.getDate())
					+ "T" + timeFormat.format(center.getDate()) + "Z" + "</time>\n").getBytes());
			if(showHurricanePathAsTrack)
				out.write("\t</trkpt>\n".getBytes());
			else
				out.write("\t</rtept>\n".getBytes());
			
			++count;
		}
		if(showHurricanePathAsTrack){
			out.write("</trkseg>\n".getBytes());
			out.write("</trk>\n".getBytes());
		}
		else
			out.write("</rte>\n".getBytes());
	}
	
	private String makeShortName(String stormName){
		int maxLength = 5;
		String shortName = stormName;
		shortName = shortName.replaceAll("[^a-zA-Z]", "");
		if(shortName.length() > maxLength){
			shortName = shortName.substring(1).replaceAll("[aeiouAEIOU]", "");
			shortName = stormName.substring(0, 1) + shortName;
			if(shortName.length() >= maxLength)
				shortName = shortName.substring(0, maxLength);
		}
		
		return shortName;
	}
	
	private LinkedList processStorm(NOAAHurricaneData noaa, String stormName)
		throws Exception
	{
		if(log.isDebugEnabled())
			log.debug("Storm:" + stormName);

		LinkedList centers = null;
		try {
			centers = noaa.getStormCenters(stormName);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			throw new Exception("Error reading NOAA coordinates:" + e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			throw new Exception("Error reading NOAA coordinates:" + e.getMessage());
		} catch(ParseException e) {
			e.printStackTrace();
			throw new Exception("Error reading NOAA coordinates:" + e.getMessage());
		}

		if(log.isDebugEnabled())
			log.debug("Retrieved " + centers.size() + " coordinates for " + stormName);

		if(centers == null){
			log.error("Failed to retrieve coordinates for " + stormName);
			throw new Exception("Failed to retrieve coordinates for " + stormName);
		}
		return centers;
	}
	
}
