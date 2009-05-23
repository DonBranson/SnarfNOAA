/*
 * Storm.java - Copyright 2005, Donald A. Branson, Jr.
 *
 * Created on Jan 30, 2005
 */
package com.moneybender.snarf;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

/**
 * @author Don Branson
 *
 */
public class Storm {
	static Logger log = Logger.getLogger(Storm.class);
	private LinkedList<HurricaneCenter> centers = null;
	String urlPrefix;
	String stormFileName;

	private Storm() {}

	public Storm(String urlPrefix, String stormFileName) {
		super();
		this.urlPrefix = urlPrefix;
		this.stormFileName = stormFileName;
	}
	
	public LinkedList getCentersFromForecastAdvisories()
		throws MalformedURLException, IOException, ParseException
	{
		if(log.isDebugEnabled())
			log.debug("Get list of forecast advisories from " + urlPrefix + stormFileName);
		String advisoryArchiveContent = null;
		advisoryArchiveContent = new WebPage(urlPrefix + stormFileName).getContents();
		
		LinkedList<String> advisories = new LinkedList<String>();
		StringTokenizer st = new StringTokenizer(advisoryArchiveContent, "\n");
		while(st.hasMoreTokens()){
			String line = st.nextToken();
			if(line.matches("^.*fstadv.*$")){
				if(log.isDebugEnabled())
					log.debug("line:" + line);
				
				String advisoryFile = line.replaceAll("^.*<a href=\"", "");
				advisoryFile = advisoryFile.replaceAll("[?].*$", "");
				String host = urlPrefix.replaceAll("http://", "").replaceAll("/.*$", "");
				String advisoryURL = "http://" + host + advisoryFile;
				if(log.isDebugEnabled())
					log.debug("advisory URL:" + advisoryURL);
				advisories.add(advisoryURL);
			}
		}
		
		if(log.isDebugEnabled())
			log.debug("Get centers from advisories...");
		centers = new LinkedList<HurricaneCenter>();
		Iterator iter = advisories.iterator();
		while(iter.hasNext()){
			String advisoryURL = (String)iter.next();
			try {
				HurricaneCenter center = new HurricaneForecastAdvisory(advisoryURL).getHurricaneCenter();
				if(log.isDebugEnabled())
					log.debug("\tCenter from forecast advisory:" + center);
				if(center != null)
					centers.add(center);
			}catch(FileNotFoundException e) {
				log.error(e.getClass().getSimpleName() + ": processing storm file:" + e.getMessage());
			}
		}
		
		return centers;
	}
		
	public LinkedList getCentersFromPublicAdvisories()
		throws MalformedURLException, IOException, ParseException
	{
		if(log.isDebugEnabled())
			log.debug("Get list of forecast advisories from " + stormFileName);
		String advisoryArchiveContent = null;
		advisoryArchiveContent = new WebPage(urlPrefix + stormFileName).getContents();
		
		LinkedList<String> advisories = new LinkedList<String>();
		StringTokenizer st = new StringTokenizer(advisoryArchiveContent, "\n");
		while(st.hasMoreTokens()){
			String line = st.nextToken();
			if(line.matches("^.*/archive/[0-9][0-9][0-9][0-9]/pub/.*$")){
				if(log.isDebugEnabled())
					log.debug("line:" + line);
				
				String advisoryFile = line.replaceAll("^.*<a href=\"", "");
				advisoryFile = advisoryFile.replaceAll("[?].*$", "");
				String host = urlPrefix.replaceAll("http://", "").replaceAll("/.*$", "");
				String advisoryURL = "http://" + host + advisoryFile;
				if(log.isDebugEnabled())
					log.debug("advisory URL:" + advisoryURL);
				advisories.add(advisoryURL);
			}
		}
		
		if(log.isDebugEnabled())
			log.debug("Get centers from advisories...");
		centers = new LinkedList<HurricaneCenter>();
		Iterator iter = advisories.iterator();
		while(iter.hasNext()){
			String advisoryURL = (String)iter.next();
			HurricaneCenter center = new HurricaneForecastAdvisory(advisoryURL).getHurricaneCenter();
			if(log.isDebugEnabled())
				log.debug("\tCenter from public advisory:" + center);
			if(center != null)
				centers.add(center);
		}
		
		return centers;
	}
	
}
