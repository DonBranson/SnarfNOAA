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
public class HurricaneForecastAdvisory {
	static Logger log = Logger.getLogger(HurricaneForecastAdvisory.class);
	String advisoryURL;
	
	private HurricaneForecastAdvisory() {}
	
	public HurricaneForecastAdvisory(String advisoryURL) {
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
		while(st.hasMoreTokens()){
			String line = st.nextToken();
			
			// Grab date
			// Example format: 0300Z WED AUG 25 2004
			// New example (as of 2006): 1500 UTC TUE JUL 18 2006
			if(line.matches("^[0-9][0-9][0-9][0-9]Z [A-Z][A-Z][A-Z] [A-Z][A-Z][A-Z] [0-9][0-9] [0-9][0-9][0-9][0-9].*"))
				dateAtLocation = parseDateLine(line);
			else if(line.matches("^[0-9][0-9][0-9][0-9] UTC [A-Z][A-Z][A-Z] [A-Z][A-Z][A-Z] [0-9][0-9] [0-9][0-9][0-9][0-9].*"))
				dateAtLocation = parseDateLine(line);
			if(line.matches("^.*CENTER LOCATED NEAR.*$")){
				foundCenter = true;
				location = parseLocationLine(line);
			}
			if(line.matches("^MAX SUSTAINED WINDS.*$"))
				windSpeed = parseWindspeedLine(line);
			
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
		SimpleDateFormat inFormat = new SimpleDateFormat("HHmm EEE MMM dd yyyy");
		String dateString = line.trim().replaceAll("Z", "").replaceAll(" UTC", "");
		return inFormat.parse(dateString);
	}
	
	static protected Location parseLocationLine(String line) {
		String latitude, longitude;
		int latitudeSign = 1, longitudeSign = 1;
		
		if(log.isDebugEnabled())
			log.debug("line:" + line);
		
		latitude = line.replaceAll("^.*CENTER LOCATED NEAR ", "");
		latitude = latitude.replaceAll(" AT.*$", "");
		if(latitude.matches(".*S.*"))
			latitudeSign = -1;
		longitude = latitude.replaceAll("^.* ", "");
		if(longitude.matches(".*W.*"))
			longitudeSign = -1;
		latitude = latitude.replaceAll("[NS] .*$", "");
		longitude = longitude.replaceAll("[WE].*$", "");
		
		return new Location(latitude, longitude, latitudeSign, longitudeSign);
	}
	
	static protected int parseWindspeedLine(String line) {
		int windSpeed = 0;
		double mphPerKnot = 1.15077945;
		
		String spd = line.replaceAll("^MAX SUSTAINED WINDS", "");
		spd = spd.replaceAll("KT WITH GUSTS TO.*", "").trim();
		spd = spd.replaceAll("[^0-9].*", "");
		try {
			windSpeed = (int)(Double.parseDouble(spd) * mphPerKnot);
		} catch (NumberFormatException e1) {
			windSpeed = -1;
		}
		
		return windSpeed;
	}
	
}
