// **********************************************************************
// 
// <copyright>
// 
//  BBN Technologies
//  10 Moulton Street
//  Cambridge, MA 02138
//  (617) 873-8000
// 
//  Copyright (C) BBNT Solutions LLC. All rights reserved.
// 
// </copyright>
// **********************************************************************
// 
// $Source$
// $RCSfile$
// $Revision$
// $Date$
// $Author$
// 
// **********************************************************************

package com.moneybender.snarf.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.bbn.openmap.Layer;
import com.bbn.openmap.event.ProjectionEvent;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMLine;
import com.moneybender.snarf.util.RoutePoint;

/**
 * A Layer to display routes.
 * 
 * Modified by Don Branson
 */
@SuppressWarnings("serial")
public class RouteLayer extends Layer {

    /**
     * A list of graphics to be painted on the map.
     */
    private OMGraphicList omgraphics;

    /**
     * Construct a default route layer. Initializes omgraphics to a
     * new OMGraphicList, and invokes createGraphics to create the
     * canned list of routes.
     */
    public RouteLayer(Map<String, List> routes, String selectedRoute) {
        omgraphics = new OMGraphicList();
        createGraphics(omgraphics, routes, selectedRoute);
    }

    /**
     * Creates an OMLine from the given parameters.
     * 
     * @param lat1 The line's starting latitude
     * @param lon1 The line's starting longitude
     * @param lat2 The line's ending latitude
     * @param lon2 The line's ending longitude
     * @param color The line's color
     * 
     * @return An OMLine with the given properties
     */
    public OMLine createLine(float lat1, float lon1, float lat2, float lon2,
                             Color color) {
        OMLine line = new OMLine(lat1, lon1, lat2, lon2, OMGraphic.LINETYPE_GREATCIRCLE);
        line.setLinePaint(color);
        return line;
    }

    /**
     * Clears and then fills the given OMGraphicList based on the routes in the given year. 
     * 
     * @param graphics The OMGraphicList to clear and populate
     * @return the graphics list, after being cleared and filled
     */
    public OMGraphicList createGraphics(OMGraphicList graphics, Map<String, List> routes, String selectedRoute) {

        graphics.clear();
        Stroke stroke = new BasicStroke(2);

        if(routes != null){
            Iterator iterator = routes.keySet().iterator();
            while(iterator.hasNext()){
                String routeName = (String)iterator.next();
                
                Iterator points = ((List)routes.get(routeName)).iterator();
                if(!points.hasNext())
                	return graphics;
                RoutePoint point1 = (RoutePoint)points.next();
//                OMPoint location = new OMPoint(point1.getLatitude(), point1.getLongitude(), 2);
//                graphics.addOMGraphic(location);
                Color trackColor = Color.BLUE;
                if(routeName.equals(selectedRoute))
                	trackColor = Color.RED;
                while(points.hasNext()){
                    RoutePoint point2 = (RoutePoint)points.next();
//                    location = new OMPoint(point2.getLatitude(), point2.getLongitude(), 2);
                    OMLine line =
                    	createLine(point1.getLatitude(), point1.getLongitude(),
                    			point2.getLatitude(), point2.getLongitude(), trackColor);
                    line.setStroke(stroke);
                    graphics.addOMGraphic(line);
//                    graphics.addOMGraphic(location);
                    		
                	point1 = point2;
                }
            }
        }
        
        return graphics;
    }

    //----------------------------------------------------------------------
    // Layer overrides
    //----------------------------------------------------------------------

    /**
     * Renders the graphics list. It is important to make this routine
     * as fast as possible since it is called frequently by Swing, and
     * the User Interface blocks while painting is done.
     */
    public void paint(java.awt.Graphics g) {
        omgraphics.render(g);
    }

    //----------------------------------------------------------------------
    // ProjectionListener interface implementation
    //----------------------------------------------------------------------

    /**
     * Handler for <code>ProjectionEvent</code>s. This function is
     * invoked when the <code>MapBean</code> projection changes. The
     * graphics are reprojected and then the Layer is repainted.
     * <p>
     * 
     * @param e the projection event
     */
    public void projectionChanged(ProjectionEvent e) {
        omgraphics.project(e.getProjection(), true);
        repaint();
    }
}