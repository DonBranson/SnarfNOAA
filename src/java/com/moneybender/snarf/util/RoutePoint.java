package com.moneybender.snarf.util;

public class RoutePoint {

	float latitude = 0;
	float longitude = 0;
	String name = "";
	String time = "";
	
	private RoutePoint(){}

	public RoutePoint(String name, String time, float latitude, float longitude) {
		super();
		this.name = name;
		this.time = time;
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public float getLatitude() {
		return latitude;
	}

	public float getLongitude() {
		return longitude;
	}

	public String getName() {
		return name;
	}

	public String getTime() {
		return time;
	}
	
}
