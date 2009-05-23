/*
 * Coordinates.java - Copyright 2004-2005, Don Branson
 *
 * Created on Sep 6, 2004
 *
 */
package com.moneybender.snarf;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 
 * @author Don Branson
 *
 */
public class HurricaneCenter {
	private float latitude = 0;		// N
	private float longitude = 0;	// E
	Date date = null;
	int windSpeed = 0;

	private HurricaneCenter(){}
	
	public HurricaneCenter(Date date, float latitude, float longitude, int windSpeed){
		this.date = date;
		this.latitude = latitude;
		this.longitude = longitude;
		this.windSpeed = windSpeed;
	}
	
	/**
	 * @return Returns the latitude.
	 */
	public float getLatitude() {
		return latitude;
	}
	/**
	 * @return Returns the longitude.
	 */
	public float getLongitude() {
		return longitude;
	}

	/**
	 * @return Returns the date.
	 */
	public Date getDate() {
		return date;
	}

	public String toString(){
		SimpleDateFormat outFormat = new SimpleDateFormat("yyyy/MM/dd HHmm");
		return outFormat.format(date) + "Z"
			+ latitude + " degrees north; "
			+ longitude + " degrees east";
	}
	/**
	 * @return Returns the windSpeed.
	 */
	public int getWindSpeed() {
		return windSpeed;
	}
}
