/*
 * NoaaViewer.java - Copyright 2004, Don Branson
 *
 * Created on Sep 9, 2005
 *
 */
package com.moneybender.snarf.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

import com.bbn.openmap.LatLonPoint;
import com.bbn.openmap.LayerHandler;
import com.bbn.openmap.MapBean;
import com.bbn.openmap.MapHandler;
import com.bbn.openmap.gui.BasicMapPanel;
import com.bbn.openmap.gui.MapPanel;
import com.bbn.openmap.layer.GraticuleLayer;
import com.bbn.openmap.layer.shape.ShapeLayer;
import com.jgoodies.looks.FontSizeHints;
import com.jgoodies.looks.LookUtils;
import com.jgoodies.looks.Options;
import com.moneybender.snarf.util.RoutePoint;
import com.moneybender.snarf.util.StormSet;

public class NoaaViewer {
	private static Logger log = Logger.getLogger(NoaaViewer.class);
	private JFrame parent;
	protected JSplitPane splitPane = null;
	MapPanel mapPanel = null;
	RouteLayer hurricaneLayer = null;
	String propertiesFileName = "fetchnoaa.properties";
	Properties props = new Properties();

	Map<String, Map> years = new TreeMap<String, Map>();
	
	
	/**
	 * Configures the UI, then builds and opens the UI.
	 */
	public static void main(String[] args) {
		NoaaViewer instance = new NoaaViewer();
		instance.loadProperties();
		instance.configureUI();
		instance.buildInterface();
	}
	
	protected void loadProperties(){
		try {
			props.load(new FileInputStream(propertiesFileName));
		} catch (FileNotFoundException e) {
			log.info("Properties file " + propertiesFileName + " not found, using defaults.");
		} catch (IOException e1) {
			log.info("Error reading properties file " + propertiesFileName + ", using defaults.");
		}
	}

	/**
	 * Configures the UI; tries to set the system look on Mac, 
	 * <code>WindowsLookAndFeel</code> on general Windows, and
	 * <code>Plastic3DLookAndFeel</code> on Windows XP and all other OS.<p>
	 * 
	 * The JGoodies Swing Suite's <code>ApplicationStarter</code>,
	 * <code>ExtUIManager</code>, and <code>LookChoiceStrategies</code>
	 * classes provide a much more fine grained algorithm to choose and
	 * restore a look and theme.
	 */
	private void configureUI() {
		UIManager.put(Options.USE_SYSTEM_FONTS_APP_KEY, Boolean.TRUE);
		Options.setGlobalFontSizeHints(FontSizeHints.MIXED);
		Options.setDefaultIconSize(new Dimension(18, 18));
		
		String lafName =
			LookUtils.IS_OS_WINDOWS_XP
			? Options.getCrossPlatformLookAndFeelClassName()
					: Options.getSystemLookAndFeelClassName();
			
			try {
				UIManager.setLookAndFeel(lafName);
			} catch (Exception e) {
				System.err.println("Can't set look & feel:" + e);
			}
	}
	
	/**
	 * Creates and configures a frame, builds the menu bar, builds the
	 * content, locates the frame on the screen, and finally shows the frame.
	 */
	private void buildInterface() {
		JFrame frame = new JFrame();
		frame.setJMenuBar(buildMenuBar());
		frame.setContentPane(buildContentPane());
		frame.setSize(1200, 800);
		locateOnScreen(frame);
		frame.setTitle("MoneyBender - NOAA Viewer");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		this.parent = frame;
	}
	
	/**
	 * Locates the frame on the screen center.
	 */
	private void locateOnScreen(Frame frame) {
		Dimension paneSize = frame.getSize();
		Dimension screenSize = frame.getToolkit().getScreenSize();
		frame.setLocation(
				(screenSize.width - paneSize.width) / 2,
				(screenSize.height - paneSize.height) / 2);
	}
	
	/**
	 * Builds and answers the menu bar.
	 */
	private JMenuBar buildMenuBar() {
		JMenu menu;
		JMenuBar menuBar = new JMenuBar();
		menuBar.putClientProperty(Options.HEADER_STYLE_KEY, Boolean.TRUE);
		
		menu = new JMenu("File");
		menu.add(new JMenuItem("Open..."));
		menu.add(new JMenuItem("Save"));
		menu.add(new JMenuItem("Export..."));
		menu.addSeparator();
		menu.add(new JMenuItem("Exit"));
		menuBar.add(menu);
		
		menu = new JMenu("Data");
		ActionListener snarfActionListener = 
			new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SnarfOptionsDialog frame = new SnarfOptionsDialog(parent, props);
			}
			
		};
		JMenuItem snarfItem = new JMenuItem("Snarf");
		snarfItem.addActionListener(snarfActionListener);
		menu.add(snarfItem);
		menu.add(new JMenuItem("Convert"));
		menuBar.add(menu);
		
		menu = new JMenu("Help");
		JMenuItem aboutItem = new JMenuItem("About...");
		menu.add(aboutItem);
		ActionListener aboutActionListener = 
			new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AboutDialog frame = new AboutDialog(parent);
			}
			
		};
		aboutItem.addActionListener(aboutActionListener);
		menuBar.add(menu);
		
		return menuBar;
	}
	
	/**
	 * Builds and answers the content pane.
	 */
	private JComponent buildContentPane() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(buildToolBar(), BorderLayout.NORTH);
		panel.add(buildSplitPane(), BorderLayout.CENTER);
		return panel;
	}
	
	/**
	 * Builds and answers the tool bar.
	 */
	private Component buildToolBar() {
		JToolBar toolBar = new JToolBar();
		toolBar.putClientProperty(Options.HEADER_STYLE_KEY, Boolean.TRUE);
		
		toolBar.add(createCenteredLabel("Tool Bar"));
		return toolBar;
	}
	
	/**
	 * Builds and answers the split panel.
	 */
	private Component buildSplitPane() {
		Component sideBar = buildSideBar();
		sideBar.setPreferredSize(new Dimension(300, 100));
		
		buildMapPanel("", "");
		Component mapPane = createStrippedScrollPane(mapPanel.getMapBean());

		splitPane =
			new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sideBar, mapPane);
		return splitPane;
	}
	
	/**
	 * Builds and answers the side bar.
	 */
	private Component buildSideBar() {
		JComponent tree = new JTree(createSampleTreeModel());
		
		MouseListener treeListener = 
			new MouseListener() {
			public void mouseClicked(MouseEvent e) {
				JTree tree = (JTree)e.getComponent();
				
				TreePath selectionPath = tree.getSelectionPath();
				if(selectionPath == null)
					return;
				Object[] nodePath = selectionPath.getPath();
				
				if(nodePath.length < 2)
					return;
				
				String year = ((DefaultMutableTreeNode)nodePath[1]).toString();
				String storm = "";
				String point = "";
				if(nodePath.length > 2)
					storm = ((DefaultMutableTreeNode)nodePath[2]).toString();
				if(nodePath.length > 3)
					point = ((DefaultMutableTreeNode)nodePath[3]).toString();
					
				if(log.isDebugEnabled()){
					log.debug("click year=" + year + "; storm= " + storm + "; point=" + point);
					log.debug("storms=" + years.get(year));
				}
				if(splitPane == null)
					log.error("No split pane ready to hold map panel");
				else{
					MapHandler mapHandler = mapPanel.getMapHandler();
					RouteLayer oldHurricaneLayer = hurricaneLayer;
					hurricaneLayer = new RouteLayer(years.get(year), storm);
					mapHandler.add(hurricaneLayer);
					mapHandler.remove(oldHurricaneLayer);
				}
			}

			public void mousePressed(MouseEvent e) {}
			public void mouseReleased(MouseEvent e) {}
			public void mouseEntered(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
		};
		tree.addMouseListener(treeListener);
		
		JComponent sideBar = createStrippedScrollPane(tree);
		
		return sideBar;
	}

	private void buildMapPanel(String year, String selectedStorm) {
		ShapeLayer countryLayer = buildCountryLayer();
		ShapeLayer statesLayer = buildStateLayer();
		hurricaneLayer = new RouteLayer(years.get(year), selectedStorm);

		GraticuleLayer graticuleLayer = new GraticuleLayer();
		
		mapPanel = new BasicMapPanel();
		MapBean mapBean = mapPanel.getMapBean();
		mapBean.setCenter(new LatLonPoint(30.0f, -80.0f));
		mapBean.setScale(51000000f);

		MapHandler mapHandler = mapPanel.getMapHandler();
		mapHandler.add(new LayerHandler());
		mapHandler.add(countryLayer);
		mapHandler.add(statesLayer);
		mapHandler.add(graticuleLayer);
		mapHandler.add(hurricaneLayer);
	}
//	mapHandler.add(new MIFLayer());
//	mapHandler.add(new DayNightLayer());
//	OMToolSet omts = new OMToolSet();
//	ToolPanel toolBar = new ToolPanel();
//	mapHandler.add(omts);
//	mapHandler.add(toolBar);

	private ShapeLayer buildStateLayer() {
		ShapeLayer statesLayer = new ShapeLayer();
		Properties stateLayerProps = new Properties();
		stateLayerProps.put("prettyName", "Political Solid");
		stateLayerProps.put("lineColor", "000000");
		stateLayerProps.put("fillColor", "BDDE83");
		stateLayerProps.put("shapeFile", "data/shape/states/statesp020.shp");
		stateLayerProps.put("spatialIndex", "data/shape/states/statesp020.ssx");
		statesLayer.setProperties(stateLayerProps);
		
		return statesLayer;
	}

	private ShapeLayer buildCountryLayer() {
		ShapeLayer countryLayer = new ShapeLayer();
		Properties countryLayerProps = new Properties();
		countryLayerProps.put("prettyName", "Political Solid");
		countryLayerProps.put("lineColor", "000000");
		countryLayerProps.put("fillColor", "BDDE83");
		countryLayerProps.put("shapeFile", "data/shape/cntry02/cntry02.shp");
		countryLayerProps.put("spatialIndex", "data/shape/cntry02/cntry02.ssx");
		countryLayer.setProperties(countryLayerProps);
		
		return countryLayer;
	}

	/**
	 * Builds and answers the tool bar.
	 */
	private Component buildStatusBar() {
		JPanel statusBar = new JPanel(new BorderLayout());
		statusBar.add(createCenteredLabel("Status Bar"));
		return statusBar;
	}
	
	// Helper Code ********************************************************
	
	/**
	 * Creates and answers a <code>JScrollpane</code> that has no border.
	 */
	private JScrollPane createStrippedScrollPane(Component c) {
		JScrollPane scrollPane = new JScrollPane(
				c,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED
		);
		scrollPane.setBorder(null);
		return scrollPane;
	}
	
	/**
	 * Creates and answers a <code>JLabel</code> that has the text
	 * centered and that is wrapped with an empty border.
	 */
	private Component createCenteredLabel(String text) {
		JLabel label = new JLabel(text);
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label.setBorder(new EmptyBorder(3, 3, 3, 3));
		return label;
	}
	
	protected TreeModel createSampleTreeModel() {
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("Storms");
		
		years.put("2011", addStormsFromYear(root, "2011"));
		years.put("2010", addStormsFromYear(root, "2010"));
		years.put("2009", addStormsFromYear(root, "2009"));
		years.put("2008", addStormsFromYear(root, "2008"));
		years.put("2007", addStormsFromYear(root, "2007"));
		years.put("2006", addStormsFromYear(root, "2006"));
		years.put("2005", addStormsFromYear(root, "2005"));
		years.put("2004", addStormsFromYear(root, "2004"));
		years.put("2003", addStormsFromYear(root, "2003"));
		
		return new DefaultTreeModel(root);
	}
	
	private Map<String, List> addStormsFromYear(DefaultMutableTreeNode root, String year) {
		DefaultMutableTreeNode yearRoot;
		try{
			StormSet stormSet = new StormSet(new File("noaaStormCoords" + year + ".gpx"));
			yearRoot = new DefaultMutableTreeNode(year);
			root.add(yearRoot);
			
			Map<String, List> storms = stormSet.getStorms();
			Iterator stormIterator = storms.keySet().iterator();
			while(stormIterator.hasNext()){
				String stormName = (String)stormIterator.next();
				DefaultMutableTreeNode stormNode = new DefaultMutableTreeNode(stormName);
				yearRoot.add(stormNode);
				
				List<RoutePoint> routePoints = storms.get(stormName);
				Iterator pointIterator = routePoints.iterator();
				while(pointIterator.hasNext()){
					RoutePoint point = (RoutePoint)pointIterator.next();
					stormNode.add(new DefaultMutableTreeNode(point.getTime() 
							+ " (" + point.getLatitude() + ", " + point.getLongitude() + ")"));
				}
			}
		
			return storms;
		}catch(Exception e){
			yearRoot = new DefaultMutableTreeNode("Failed to load year " + year);
			root.add(yearRoot);
			
			return null;
		}
	}
	
	JFrame getParentFrame() {
		return (JFrame) (SwingUtilities.getWindowAncestor(parent));
	}	
	
}
