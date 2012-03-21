/*
 * FetchNOAA.java - Copyright 2004-2005, Don Branson
 *
 * Created on Sep 6, 2004
 *
 */
package com.moneybender.noaa;

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.moneybender.snarf.Storm;
import com.moneybender.snarf.WebPage;

/**
 * 
 * @author Don Branson
 *
 */
public class NOAAHurricaneData {

	static Logger log = Logger.getLogger(NOAAHurricaneData.class);
	private String indexContent = null;
	private LinkedHashMap<String, String> storms = null;
	String urlPrefix = null;

	public NOAAHurricaneData(String urlPrefix, String indexURL)
		throws MalformedURLException, IOException
	{
		if(log.isDebugEnabled())
			log.debug("Fetch index from " + indexURL);
		
		this.urlPrefix = urlPrefix;
		indexContent = new WebPage(indexURL).getContents();

		if(log.isDebugEnabled())
			log.debug("Get Storms...");
		if(storms == null){
			storms = new LinkedHashMap<String, String>();
			StringTokenizer st = new StringTokenizer(indexContent, "\n");
			while(st.hasMoreTokens()){
				String line = st.nextToken();
				if(line.matches(".*<a href=\".*.shtml.\">.*</a><br>.*")){
					if(log.isDebugEnabled())
						log.debug("line:" + line);
					String storm = line.replaceAll("^.*<a href=\".*.shtml.\">", "");
					storm = storm.replaceAll("</a><br>.*$", "").replaceAll("^.*  *", "").replaceAll("[*]", "");
					if(log.isDebugEnabled())
						log.debug("storm:" + storm);

					String stormFile = line.replaceAll("^.*<a href=\"", "");
					stormFile = stormFile.replaceAll("[?]\">.*$", "");
					if(log.isDebugEnabled())
						log.debug("storm file:" + stormFile);

					storms.put(storm, stormFile);
				}
			}
		}

	}

	public HashMap getStormFileNames() {
		return storms;
	}

	public LinkedList getStormCenters(String stormName)
		throws MalformedURLException, IOException, ParseException
	{
		String stormFileName = (String)storms.get(stormName);
		return new Storm(urlPrefix, stormFileName).getCentersFromForecastAdvisories();
	}
}
