package org.ivis.layout.cluster;

import java.util.ArrayList;
import java.util.Iterator;
import java.awt.Point;
import java.awt.Dimension;

import org.ivis.layout.*;
import org.ivis.layout.cose.CoSENode;
import org.ivis.layout.fd.FDLayoutNode;
//import org.ivis.layout.fd.FDLayoutNode;
import org.ivis.util.IGeometry;
import org.ivis.util.IMath;
import org.ivis.util.PointD;
import org.ivis.util.RectangleD;

/**
 * This class implements CoSE specific data and functionality for nodes.
 *
 * @author Can Cagdas Cengiz
 *
 * Copyright: i-Vis Research Group, Bilkent University, 2007 - present
 */
public class ZoneNode extends CoSENode
{
// -----------------------------------------------------------------------------
// Section: Instance variables
// -----------------------------------------------------------------------------

	
	/**
	 * Polygon property is needed for a ZoneNode to define its geometry and
	 * calculate its intersections with the other zones
	 * 
	 */
	public ArrayList<PointD> polygon;
	
// -----------------------------------------------------------------------------
// Section: Constructors and initialization
// -----------------------------------------------------------------------------
	/*
	 * Constructor
	 */
	public ZoneNode(LGraphManager gm, Object vNode)
	{
		super(gm, vNode);
	}

	/**
	 * Alternative constructor
	 */
	public ZoneNode(LGraphManager gm, Point loc, Dimension size, Object vNode)
	{
		super(gm, loc, size, vNode);
	}

// -----------------------------------------------------------------------------
// Section: Remaining methods
// -----------------------------------------------------------------------------

	public boolean overlaps(LNode nodeB, double [] overlapAmount)
	{
		ZoneNode b = (ZoneNode) nodeB;
		
		ArrayList <PointD> polygonA = this.polygon;
		ArrayList <PointD> polygonB = b.polygon;
		Object [] overlap;

		
		//System.out.println("In ZoneNode overlaps");
		if ( (polygonA.size() > 3) && (polygonB.size() > 3) )
		{
			overlap = IGeometry.convexPolygonOverlap(polygonA,polygonB);
			if ((double) overlap[0] != 0.0)
			{
				/*System.out.println("The clusters " + c1.clusterID + 
						" and " + c2.clusterID + " overlap."); // test
				*/
				Object [] newOverlap = new Object[2];
					

				//overlap[0]:  overlap amount
				//overlap[1]:  overlap direction
					
				PointD temp;
				temp = IGeometry.getXYProjection(((double) overlap[0]),
						((PointD) overlap[1]));
					
				overlapAmount[0] = temp.x; // overlap in x						
				overlapAmount[1] = temp.y; // overlap in y

				//System.out.println("Zone Repulsion for zone "+ this.label + " and " + nodeB.label + " is " + overlapAmount[0] +","+overlapAmount[1]);
			
				return true;
			}

		}
		return false;
	}
	
	public void calcCenter()
	{
		//PointD center;
		double cx;
		double cy;

		cx = 0;
		cy = 0;
		
		for (Object o: this.polygon)
		{
			PointD pt = (PointD) o;
			cx += pt.x;
			cy += pt.y;
		}
		cx = cx / (this.polygon.size());
		cy = cy / (this.polygon.size());
		this.setCenter(cx, cy);
	}
	
	
	
// -----------------------------------------------------------------------------
// Section: Getters and setters
// -----------------------------------------------------------------------------
	
}