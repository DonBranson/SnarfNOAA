package com.moneybender.snarf.util;

public class WayPoint {

	float latitude = 0;
	float longitude = 0;
	String name = "";
	String description = "";

	private WayPoint(){};
	
	public WayPoint(String name, String description, float latitude, float longitude) {
		super();
		this.name = name;
		this.description = description;
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public String getDescription() {
		return description;
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
}
