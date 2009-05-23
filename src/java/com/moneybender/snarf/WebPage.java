/*
 * WebPage.java - Copyright 2005, Donald A. Branson, Jr.
 *
 * Created on Jan 30, 2005
 */
package com.moneybender.snarf;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.log4j.Logger;

/**
 * @author Don Branson
 *
 */
public class WebPage {
	static Logger log = Logger.getLogger(WebPage.class);
	
	protected String contents = "";

	private WebPage() {}

	public WebPage(String url)
		throws MalformedURLException, IOException
	{
		if(log.isDebugEnabled())
			log.debug("getWebPageContent - " + url);
		
		int bytesRead = 0;
		int totalBytesRead = 0;
		byte[] buffer = new byte[16384];
		StringBuffer sb = new StringBuffer();
		
		URLConnection connection = new URL(url).openConnection();
		connection.setUseCaches(false);
		InputStream in = connection.getInputStream();
		while((bytesRead = in.read(buffer)) != -1){
			sb.append(new String(buffer, 0, bytesRead));
			totalBytesRead += bytesRead;
		}
		if(log.isDebugEnabled())
			log.debug("total bytes:" + totalBytesRead);
		
		contents = new String(sb);
		
	}
	
	public String getContents() {
		return contents;
	}
}
