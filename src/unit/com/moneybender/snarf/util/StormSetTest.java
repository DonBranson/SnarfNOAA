package com.moneybender.snarf.util;

import java.io.File;

import junit.framework.TestCase;

import org.apache.log4j.Logger;

public class StormSetTest
	extends TestCase
{
	private static Logger log = Logger.getLogger(StormSetTest.class);
			
	public void testBasic()
	{
		try{
			new StormSet(new File("noaaStormCoords2003.gpx"));
			log.info("Loaded");
		}catch(Exception e){
			fail(e.getMessage());
		}
	}
}
