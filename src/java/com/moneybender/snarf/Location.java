/*
 * Location.java - Copyright 2005, Donald A. Branson, Jr.
 *
 * Created on Jan 30, 2005
 */
package com.moneybender.snarf;


public class Location{
	private String latitude, longitude;
	private int latitudeSign, longitudeSign;

	private Location() {}

	public Location(String latitude, String longitude, int latitudeSign,
			int longitudeSign) {
		this.latitude = latitude.trim();
		this.longitude = longitude.trim();
		this.latitudeSign = latitudeSign;
		this.longitudeSign = longitudeSign;
	}

	public String getLatitude() {
		return latitude;
	}

	public String getLongitude() {
		return longitude;
	}

	public int getLatitudeSign() {
		return latitudeSign;
	}

	public int getLongitudeSign() {
		return longitudeSign;
	}
}