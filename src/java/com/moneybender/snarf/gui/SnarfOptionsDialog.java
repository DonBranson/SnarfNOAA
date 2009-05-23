package com.moneybender.snarf.gui;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.GregorianCalendar;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.moneybender.snarf.util.StormSet;

public class SnarfOptionsDialog extends JDialog {
	private static Logger log = Logger.getLogger(SnarfOptionsDialog.class);

    String baseUrl = "http://www.nhc.noaa.gov";
    String context = "archive";

	public SnarfOptionsDialog(Frame owner, final Properties props) {
		FormLayout layout = new FormLayout(
				"right:pref, 4dlu, 50dlu, 50dlu, 50dlu, 160dlu",				// columns
				"pref, 2dlu, pref, 2dlu, pref, 2dlu, pref, 2dlu, pref, 2dlu");	// rows
		
		layout.setRowGroups(new int[][]{{1, 3, 5, 7}});
		final JLabel outputLabel = new JLabel("");
		
	    ActionListener snarfActionListener = 
	    	new ActionListener() {
		    	public void actionPerformed(ActionEvent event) {
		    		outputLabel.setText("Fetch...");
		    		String urlPrefix = props.getProperty("base.url", "http://www.nhc.noaa.gov");
		    		String indexURIRoot = props.getProperty("index.uri.root", "archive");
		    		String indexYear = props.getProperty("index.year", Integer.toString(new GregorianCalendar().get(GregorianCalendar.YEAR)));
		    		String gpxFileName = props.getProperty("gpx.output.file", "noaaStormCoords.gpx");
		    		String gpxHeaderFileName = props.getProperty("gpx.header.file", "fetchnoaa.header");
		    		String addedCoordinatesFile = props.getProperty("gpx.added.input.coords.file", "config/addedCoordinates.gpx");
		    		int threadCount = Integer.parseInt((props.getProperty("thread.count", "0").trim()));
		    		boolean showHurricanePathsAsTracks = false;
		    		if(props.getProperty("show.hurricane.paths.as.tracks").trim().toLowerCase().equals("true"))
		    			showHurricanePathsAsTracks = true;
		    		String oneStormOnly = null;
		    		if(props.getProperty("one.storm.only") != null)
		    			oneStormOnly = props.getProperty("one.storm.only").trim().toUpperCase();

		    		try {
		    			TextHolder textDisplayer = new TextHolder(){
		    				public void setText(String text) {
		    					outputLabel.setText(text);
		    					outputLabel.validate();
		    				}
		    			};
		    			new StormSet().fetchStormDataFromNOAA(
		    					textDisplayer,
		    					urlPrefix, indexURIRoot, indexYear,
		    					gpxHeaderFileName, gpxFileName,
		    					addedCoordinatesFile, showHurricanePathsAsTracks, oneStormOnly, threadCount);
    					SnarfOptionsDialog.this.setVisible(false);
		    		} catch (Exception e) {
		    			e.printStackTrace();
		    			log.info("Error processing NOAA site data " + e.getMessage());
		    		}
		    	}
	    };
		
		JPanel panel = new JPanel(layout);
	    String[] years = {"2006", "2005", "2004", "2003"};
	    JButton doFetch = new JButton("Fetch");
	    doFetch.addActionListener(snarfActionListener);
	    
		CellConstraints cc = new CellConstraints();
		panel.add(new JLabel("Base URL"), cc.xy (1, 1));
		panel.add(new JLabel(baseUrl), cc.xyw(3, 1, 3));

		panel.add(new JLabel("Context"), cc.xy (1, 3));
		panel.add(new JLabel(context), cc.xyw (3, 3, 1));
		
		panel.add(new JLabel("Year"), cc.xy (1, 5));
	    panel.add(new JComboBox(years), cc.xyw (3, 5, 1));
		
	    panel.add(doFetch, cc.xy (5, 5));
		panel.add(new JLabel("Year"), cc.xy (1, 5));

		panel.add(outputLabel, cc.xyw (3, 7, 4));
	    
	    add(panel);
	    setTitle("Snarf options");
	    setSize(550, 150);
	    locateOnScreen(this);
	    setVisible(true);
	}

    private void locateOnScreen(Dialog dialog) {
        Dimension paneSize   = dialog.getSize();
        Dimension screenSize = dialog.getToolkit().getScreenSize();
        dialog.setLocation(
            (screenSize.width  - paneSize.width)  / 2,
            (screenSize.height - paneSize.height) / 2);
    }

}
