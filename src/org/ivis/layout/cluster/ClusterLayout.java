package org.ivis.layout.cluster;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.HashSet;
import java.util.Random;


import org.ivis.layout.LEdge;
import org.ivis.layout.LGraph;
import org.ivis.layout.Cluster;
import org.ivis.layout.LGraphManager;
import org.ivis.layout.LNode;
import org.ivis.layout.Layout;
import org.ivis.layout.LayoutConstants;
import org.ivis.layout.cluster.ClusterGraphManager;
import org.ivis.layout.cluster.ClusterEdge;
import org.ivis.layout.cluster.ClusterConstants;
import org.ivis.layout.cose.*;
import org.ivis.layout.fd.FDLayoutConstants;
import org.ivis.layout.fd.FDLayoutEdge;
import org.ivis.layout.fd.FDLayoutNode;
import org.ivis.util.IGeometry;
import org.ivis.util.IMath;
import org.ivis.util.IntegerQuickSort;
import org.ivis.util.PointD;
import org.ivis.util.RectangleD;

/**
 * This class implements a layout process based on the CoSE algorithm.
 * In addition to repulsion forces in CoSE, a new repulsion force is 
 * presented based on overlap amounts of the clusters to push them 
 * apart from each other.
 *
 * @author Can Cagdas Cengiz
 * 
 * Copyright: i-Vis Research Group, Bilkent University, 2007 - present
 */

public class ClusterLayout extends CoSELayout
{

	HashMap overlappingClusterMap;
	int maxClusterId;
	
	/**
	 * The constructor creates and associates with this layout a new graph
	 * manager as well. 
	 */
	public ClusterLayout()
	{

		super();
		
	}
	
	/**
	 * This method creates a new graph manager associated with this layout.
	 */
	protected LGraphManager newGraphManager()
	{
		LGraphManager gm = new ClusterGraphManager(this);
		this.graphManager = gm;
		return gm;
	}
	
	/**
	 * This method creates a new edge associated with the input view edge.
	 */
	public LEdge newEdge(Object vEdge)
	{
		
		return new ClusterEdge(null, null, vEdge);
	
	}
	
	/**
	 * Override. This method introduces one more repulsion force to separate 
	 * overlapping clusters. If polygons of two clusters overlap they are 
	 * pushed apart in the direction that makes the repulsion force minimum.
	 */	
	public void calcRepulsionForces()
	{
		
		this.calcZoneGraphRepulsionForces();
		super.calcRepulsionForces();
						
	}
	
	/**
	 * Override method for spring forces.
	 */
	public void calcSpringForces()
	{
		this.calcZoneGraphSpringForces();
		
		super.calcSpringForces();
		
		
		
	}
	
	/**
	 * Override method for layout purposes. isInterCluster property for 
	 * all edges is set.
	 */
	public boolean layout() 
	{
		
		if (this.graphManager.getClusterManager().getClusters().size() > 1)
		{
			
			((ClusterGraphManager) (this.graphManager)).formClusterZones();

			// set isInterCluster property for all edges
			for (Object edge : this.graphManager.getAllEdges())
			{
				ClusterEdge e = (ClusterEdge) edge;
				e.setIsInterCluster();
			}
						
		}
		
		scatterZoneGraphNodes();
		super.layout();
		
		
		return true;
	}
	
	/**
	 * Override. This method changes the spring constant if the edge is
	 * inter-cluster. 
	 */
	protected void calcIdealEdgeLengths()
	{
		
		ClusterEdge edge;
		
		for (Object obj : this.graphManager.getAllEdges())
		{
			edge = (ClusterEdge) obj;			
			edge.idealLength = super.idealEdgeLength;
			
			if (edge.isInterCluster())
			{
				edge.idealLength = edge.idealLength * 
						(ClusterConstants.DEFAULT_INTER_ZONE_EDGE_LENGTH_FACTOR);
			}

		}
	}
	
	/**
	 * This method calculates the spring forces between the zones. It is
	 * called by the override method calcSpringForces. 
	 */
	public void calcZoneGraphSpringForces()
	{
		List zoneEdges = (((ClusterGraphManager) (this.graphManager))).zoneGraph.getEdges();
		
		for (int i = 0; i < zoneEdges.size(); i++)
		{
			
			ZoneEdge edge = (ZoneEdge) zoneEdges.get(i);
			
			this.calcSpringForce(edge, edge.idealLength);
			ZoneNode target = (ZoneNode) edge.getTarget();
			
			applyZoneGraphSpringForcesToCluster(target);
			ZoneNode source = (ZoneNode) edge.getSource();

			applyZoneGraphSpringForcesToCluster(source);

		}
						
	}
	
	/**
	 * This method calculates the spring forces between the zones. It is
	 * called by the override method calcRepulsionForces. 
	 */
	public void calcZoneGraphRepulsionForces()
	{
		this.calcZonePolygons();
		
		List zones = (((ClusterGraphManager) (this.graphManager))).zoneGraph.getNodes();
		 
		 for (int i = 0; i < zones.size() - 1; i++)
		 {
			 ZoneNode zoneA = (ZoneNode) zones.get(i);
			
			 for (int j = i + 1; j < zones.size(); j++)
			 {
				 ZoneNode zoneB = (ZoneNode) zones.get(j);							 
				 
				 // calculate repulsion forces for zone graph nodes 
				 this.calcRepulsionForce(zoneA, zoneB);
			 }
			 
			 // get the id of the zone to match with the cluster
			 int clusterID = Integer.parseInt(zoneA.label);
			 
			 applyZoneGraphRepulsionsToCluster(zoneA);
			 			 
		 }
		
	}
	
	/**
	 * Override. Parameters are changed to ZoneNodes. ZoneNodes have their own 
	 * overlap methods.
	 */
	public void calcRepulsionForce(ZoneNode a, ZoneNode b)
	{
		super.calcRepulsionForce(a, b);
	}
	
	
	/**
	 * This method applies the spring forces calculated for the zone graph to the
	 * nodes that belong to the zone
	 */
	public void applyZoneGraphSpringForcesToCluster(ZoneNode zone)
	{
		
		// get the id of the zone to match with the cluster
		int clusterID = Integer.parseInt(zone.label);

		// match zone id to cluster id and get the cluster
		Cluster cluster = this.graphManager.getClusterManager().getClusterByID(clusterID);
		
		for (Object o:cluster.getNodes())
		{
			CoSENode node = (CoSENode) o;
			
			node.springForceX = node.springForceX + 
					( (ClusterConstants.DEFAULT_ZONE_SPRING_FACTOR) * zone.springForceX);			
			
			node.springForceY = node.springForceY + 
					( (ClusterConstants.DEFAULT_ZONE_SPRING_FACTOR) * zone.springForceY);

		}
	}
	
	/**
	 * This method applies the repulsion forces calculated for the zone graph to the
	 * nodes that belong to the zone
	 */
	public void applyZoneGraphRepulsionsToCluster(ZoneNode zone)
	{
		
		 // get the id of the zone to match with the cluster
		 int clusterID = Integer.parseInt(zone.label);
		
		// match zone id to cluster id and get the cluster
		Cluster cluster = this.graphManager.getClusterManager().getClusterByID(clusterID);
		
		for (Object o:cluster.getNodes())
		{
			CoSENode node = (CoSENode) o;
			
			node.repulsionForceX = node.repulsionForceX + 
					(ClusterConstants.DEFAULT_ZONE_REPULSION_FACTOR * zone.repulsionForceX);			
			node.repulsionForceY = node.repulsionForceY + 
					(ClusterConstants.DEFAULT_ZONE_REPULSION_FACTOR * zone.repulsionForceY);
		}
	}
	
	/**
	 * This method changes the positions of the ZoneNodes. It is called from the
	 * override method moveNodes.
	 */
	public void moveZoneGraphNodes()
	{

		List zones = (((ClusterGraphManager) (this.graphManager))).zoneGraph.getNodes();
		
		for (Object o: zones)
		{
			ZoneNode node = (ZoneNode) o;
			node.move();
		}
	}
	
	/**
	 * Override. Moves ZoneGraph nodes too.
	 */
	public void moveNodes()
	{
		moveZoneGraphNodes();
		super.moveNodes();
		
		
	}
	
	/**
	 * This method calculates the central position of the zone graph nodes.
	 * It is used to position the zone nodes in the beginning of the layout
	 * and also for testing purposes.
	 */
	private void scatterZoneGraphNodes()
	{
		ZoneGraph zoneGraph = ((ClusterGraphManager) this.getGraphManager()).zoneGraph;
		
		for (Object o:zoneGraph.getNodes())
		{
			ZoneNode zone = (ZoneNode) o;			
			zone.calcCenter();
			
		} 
	}

	/**
	 * This method calculates the Zone Polygons. It is based on the clusters in 
	 * ClusterManager.
	 */
	private void calcZonePolygons()
	{
		ArrayList<Cluster> clusters = this.graphManager.
				getClusterManager().getClusters();
		
		// calculate polygons for each cluster
		
		for (Cluster c:clusters)
		{
			c.calculatePolygon();
		}
		
	}

	
	private static Random random = new Random(Layout.RANDOM_SEED);
}