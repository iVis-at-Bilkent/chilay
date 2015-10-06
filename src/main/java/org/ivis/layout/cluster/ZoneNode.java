package org.ivis.layout.cluster;

import java.util.ArrayList;
import java.awt.Point;
import java.awt.Dimension;

import org.ivis.layout.*;
import org.ivis.layout.cose.CoSENode;
import org.ivis.util.IGeometry;
import org.ivis.util.PointD;

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
	public PointD center;
// -----------------------------------------------------------------------------
// Section: Constructors and initialization
// -----------------------------------------------------------------------------
	/*
	 * Constructor
	 */
	public ZoneNode(LGraphManager gm, Object vNode)
	{
		super(gm, vNode);
		this.rect = null;
	}

	/**
	 * Alternative constructor
	 */
	public ZoneNode(LGraphManager gm, Point loc, Dimension size, Object vNode)
	{
		super(gm, loc, size, vNode);
		this.rect = null;
	}

// -----------------------------------------------------------------------------
// Section: Remaining methods
// -----------------------------------------------------------------------------

	public boolean calcOverlap(LNode nodeB, double [] overlapAmount)
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
				//overlap[0]:  overlap amount
				//overlap[1]:  overlap direction
					
				PointD temp;
				temp = IGeometry.getXYProjection(((double) overlap[0]),
						((PointD) overlap[1]));
					
				overlapAmount[0] = temp.x * ClusterConstants.DEFAULT_CLUSTER_SEPARATION; // overlap in x						
				overlapAmount[1] = temp.y * ClusterConstants.DEFAULT_CLUSTER_SEPARATION; // overlap in y
			//	System.out.println("Zone " + this.label + " and " + nodeB.label);
			//	System.out.println("Zone Overlap amount x:" + temp.x + " ,y:" + temp.y);	// test
				
				return true;
			}
		}
		return false;
	}
	
	public void calcIntersection(LNode nodeB, double[] clipPoints)
	{		
		IGeometry.getPolygonIntersection(this.polygon, ((ZoneNode) nodeB).polygon, clipPoints);
	}
	
	
	public void calcCenter()
	{
		this.center = IGeometry.getPolygonCenter(this.polygon);
	}
	
	public void move() 
	{
		this.calcCenter();
		
		this.springForceX = 0.0;
		this.springForceY = 0.0;
		this.repulsionForceX = 0.0;
		this.repulsionForceY = 0.0;
		this.gravitationForceX = 0.0;
		this.gravitationForceY = 0.0;
		this.displacementX = 0.0;
		this.displacementY = 0.0;		
	}	
}