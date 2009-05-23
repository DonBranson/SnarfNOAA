/*
 * HurricaneCenterPage.java - Copyright 2005, Donald A. Branson, Jr.
 *
 * Created on Jan 30, 2005
 */
package com.moneybender.snarf;

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

/**
 * @author Don Branson
 *
 */
public class HurricanePublicAdvisory {
	static Logger log = Logger.getLogger(HurricaneForecastAdvisory.class);
	String advisoryURL;
	
	private HurricanePublicAdvisory() {}
	
	public HurricanePublicAdvisory(String advisoryURL) {
		this.advisoryURL = advisoryURL;
	}
	
	public HurricaneCenter getHurricaneCenter()
		throws MalformedURLException, IOException, ParseException
	{
		if(log.isDebugEnabled())
			log.debug("Fetch advisory " + advisoryURL);
		
		String advisoryContent = null;
		float latitude = 0, longitude = 0;
		Location location = null;
		boolean foundCenter = false;
		Date dateAtLocation = null;
		int windSpeed = 0;
		
		advisoryContent = new WebPage(advisoryURL).getContents();
		StringTokenizer st = new StringTokenizer(advisoryContent, "\n");
		
		if(log.isDebugEnabled())
			log.debug("Extract storm center.");
		String parseLine = "";
		while(st.hasMoreTokens()){
			String line = st.nextToken();
			if(line.trim().length() == 0){
				if(log.isDebugEnabled())
					log.debug("Parsing line:" + parseLine);
				// Grab date
				// Example format: 2 AM EDT WED AUG 24 2005
				if(parseLine.matches("^.*[0-9][0-9]* [AP][M] EDT [A-Z][A-Z][A-Z] [A-Z][A-Z][A-Z] [0-9][0-9] [0-9][0-9][0-9][0-9].*$"))
					dateAtLocation = parseDateLine(parseLine);
				if(parseLine.matches("^.*LATITUDE.*LONGITUDE.*$")){
					foundCenter = true;
					location = parseLocationLine(parseLine);
				}
				if(parseLine.matches("^MAXIMUM SUSTAINED WINDS ARE NEAR.*$"))
					windSpeed = parseWindspeedLine(parseLine);
				
				parseLine = "";
			} else
				parseLine += line;

		}
		if(!foundCenter)
			return null;
		
		try{ latitude = Float.parseFloat(location.getLatitude().trim()); }catch(NumberFormatException ignored){}
		try{ longitude = Float.parseFloat(location.getLongitude().trim()); }catch(NumberFormatException ignored){}
		latitude = latitude * location.getLatitudeSign();
		longitude = longitude * location.getLongitudeSign();
		
		return new HurricaneCenter(dateAtLocation, latitude, longitude, windSpeed);
	}
	
	static protected Date parseDateLine(String line)
		throws ParseException
	{
		if(log.isDebugEnabled())
			log.debug("parseDateLine:" + line);
		SimpleDateFormat inFormat = new SimpleDateFormat("HH EEE MMM dd yyyy");
		String dateString = line.trim().replaceAll("Z", "");
		return inFormat.parse(dateString);
	}
	
	static protected Location parseLocationLine(String line) {
		if(log.isDebugEnabled())
			log.debug("parseLocationLine:" + line);
		String latitude, longitude;
		int latitudeSign = 1, longitudeSign = 1;
		
		if(log.isDebugEnabled())
			log.debug("line:" + line);
		
		latitude = line.replaceAll("^.*LATITUDE ", "");
		latitude = line.replaceAll("^[.][.][.].*", "");
		if(latitude.matches(".*SOUTH.*"))
			latitudeSign = -1;
		latitude = line.replaceAll(".*SOUTH.*", "");
		latitude = line.replaceAll(".*NORTH.*", "");

		longitude = line.replaceAll("^.*LONGITUDE ", "");
		longitude = longitude.replaceAll("^.*OR ABOUT.*", "");
		if(longitude.matches(".*WEST.*"))
			longitudeSign = -1;
		latitude = line.replaceAll(".*WEST.*", "");
		latitude = line.replaceAll(".*EAST.*", "");
		
		return new Location(latitude, longitude, latitudeSign, longitudeSign);
	}
	
	static protected int parseWindspeedLine(String line) {
		if(log.isDebugEnabled())
			log.debug("parseSpeedLine:" + line);
		int windSpeed = 0;
		
		String spd = line.replaceAll("^MAXIMUM SUSTAINED WINDS ARE NEAR ", "");
		spd = spd.replaceAll(" MPH .*", "").trim();
		spd = spd.replaceAll("[^0-9].*", "");
		try {
			windSpeed = (int)(Double.parseDouble(spd));
		} catch (NumberFormatException e1) {
			windSpeed = -1;
		}
		
		return windSpeed;
	}
	
}
