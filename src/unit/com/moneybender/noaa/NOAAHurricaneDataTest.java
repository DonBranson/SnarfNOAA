/*
 * FetchNOAATest.java - Copyright 2004, Don Branson
 *
 * Created on Sep 6, 2004
 *
 */
package com.moneybender.noaa;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import junit.framework.TestCase;

import org.apache.log4j.Logger;

import com.moneybender.snarf.gui.TextHolder;
import com.moneybender.snarf.util.StormSet;

/**
 * 
 * @author Don Branson
 *
 */
public class NOAAHurricaneDataTest extends TestCase {

	static Logger log = Logger.getLogger(NOAAHurricaneDataTest.class);
	String propertiesFileName = "fetchnoaa.properties";
	Properties props = new Properties();

	public void testFetch(){
		try {
			props.load(new FileInputStream(propertiesFileName));
		} catch (FileNotFoundException e) {
			log.info("Properties file " + propertiesFileName + " not found, using defaults.");
		} catch (IOException e1) {
			log.info("Error reading properties file " + propertiesFileName + ", using defaults.");
		}

		String urlPrefix = props.getProperty("base.url", "http://www.nhc.noaa.gov");
		String indexURIRoot = props.getProperty("index.uri.root", "archive");
		String indexYear = props.getProperty("index.year", "2004");
		String gpxFileName = props.getProperty("gpx.output.file", "noaaStormCoords.gpx");
		String gpxHeaderFileName = props.getProperty("gpx.header.file", "fetchnoaa.header");
		String addedCoordinatesFile = props.getProperty("gpx.added.input.coords.file",
			"config/addedCoordinates.gpx");
		int threadCount = Integer.parseInt((props.getProperty("thread.count", "0").trim()));
		boolean showHurricanePathsAsTracks = false;
		if(props.getProperty("show.hurricane.paths.as.tracks").trim().toLowerCase().equals("true"))
			showHurricanePathsAsTracks = true;
		String oneStormOnly = null;
		if(props.getProperty("one.storm.only") != null)
			oneStormOnly = props.getProperty("one.storm.only").trim().toUpperCase();

		try {
			TextHolder devNull = new TextHolder(){
				public void setText(String text) { }
			};
			new StormSet().fetchStormDataFromNOAA(
					devNull,
					urlPrefix, indexURIRoot, indexYear,
					gpxHeaderFileName, gpxFileName,
					addedCoordinatesFile, showHurricanePathsAsTracks, oneStormOnly, threadCount);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Error processing NOAA site data " + e.getMessage());
		}
	}
}
