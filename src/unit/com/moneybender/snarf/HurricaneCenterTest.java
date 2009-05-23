/*
 * CoordinatePairTest.java - Copyright 2005, Donald A. Branson, Jr.
 *
 * Created on Jan 30, 2005
 */
package com.moneybender.snarf;

import java.util.Date;

import junit.framework.TestCase;

import org.apache.log4j.Logger;

/**
 * @author Don Branson
 *
 */
public class HurricaneCenterTest extends TestCase {
	static Logger log = Logger.getLogger(HurricaneCenterTest.class);

	public void testConstructor(){
		Date date = new Date();
		float latitude = 40, longitude = -89;
		int windSpeed = 40;

		HurricaneCenter center = new HurricaneCenter(date, latitude, longitude, windSpeed);
		log.info("pair created: HurricaneCenter(" + center.getDate() + ", " + center.getLatitude() + ", " + center.getLongitude() + ", " + center.getWindSpeed());
		assertEquals("Date not stored correctly:", date, center.getDate());
		assertEquals("Latitude not stored correctly", latitude, center.getLatitude(), 0);
		assertEquals("Longitude not stored correctly", longitude, center.getLongitude(), 0);
		assertEquals("Windspeed not stored correctly:", windSpeed, center.getWindSpeed());
	}
}
