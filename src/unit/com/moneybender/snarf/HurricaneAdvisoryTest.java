/*
 * HurricaneCenterPageTest.java - Copyright 2005, Donald A. Branson, Jr.
 *
 * Created on Jan 30, 2005
 */
package com.moneybender.snarf;

import java.text.ParseException;
import java.util.Date;

import junit.framework.TestCase;

import org.apache.log4j.Logger;

/**
 * @author Don Branson
 *
 */
public class HurricaneAdvisoryTest extends TestCase {
	static Logger log = Logger.getLogger(HurricaneAdvisoryTest.class);

	public void testParseDateLine() {
		log.info("testParseDateLine()");
		try {
			String sampleLine = "0900Z SAT AUG 14 2004";
			Date sampleDate = HurricaneForecastAdvisory.parseDateLine(sampleLine);
		} catch (ParseException e) {
			fail("Parse exception parsing valid sample date:" + e.getMessage());
		}
		
		try {
			String sampleLine = "0900Z SAT ABC 14 2004";
			Date sampleDate = HurricaneForecastAdvisory.parseDateLine(sampleLine);
			fail("No parse exception parsing malformatted sample date:");
		} catch (ParseException ignored) {}

	}
	
	public void testParseLocationLine() {
		log.info("testParseLocationLine()");
		String sampleLine = "TROPICAL DEPRESSION CENTER LOCATED NEAR  9.8N  49.8W AT 14/0900Z";
		Location sampleLocation = HurricaneForecastAdvisory.parseLocationLine(sampleLine);
		assertEquals("Latitude parsed incorrectly:", "9.8", sampleLocation.getLatitude());
		assertEquals("Longitude parsed incorrectly:", "49.8", sampleLocation.getLongitude());
		assertEquals("Latitude parsed incorrectly:", 1, sampleLocation.getLatitudeSign());
		assertEquals("Longitude parsed incorrectly:", -1, sampleLocation.getLongitudeSign());
	}
	
	public void testParseWindspeedLine() {
		log.info("testParseWindspeedLine");
		String sampleLine = "MAX SUSTAINED WINDS  30 KT WITH GUSTS TO  40 KT.";
		int sampleWindspeed = HurricaneForecastAdvisory.parseWindspeedLine(sampleLine);
		assertEquals("Wind speed not parsed correctly:", 34, sampleWindspeed);
	}
	
}
