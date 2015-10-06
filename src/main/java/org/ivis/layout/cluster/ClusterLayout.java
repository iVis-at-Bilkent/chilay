package org.ivis.layout.cluster;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.ivis.layout.LEdge;
import org.ivis.layout.Cluster;
import org.ivis.layout.LGraph;
import org.ivis.layout.LGraphManager;
import org.ivis.layout.LNode;
import org.ivis.layout.LayoutConstants;
import org.ivis.layout.cluster.ClusterGraphManager;
import org.ivis.layout.cluster.ClusterEdge;
import org.ivis.layout.cluster.ClusterConstants;
import org.ivis.layout.cose.*;
import org.ivis.util.IGeometry;
import org.ivis.util.PointD;

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

	//public static final boolean areConsoleAndNewFrameTestsOn = false;
	
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
	 * This method creates a new node associated with the input view node.
	 */
	public LNode newNode(Object vNode)
	{
		return new ClusterNode(this.graphManager, vNode);
	}
	/**
	 * Override. This method modifies the gravitation constants for 
	 * this layout.
	 */
	public void initParameters()
	{
		super.initParameters();
		
		this.gravityRangeFactor = super.gravityRangeFactor * 1.5;
		this.gravityConstant = super.gravityConstant * 1.5;
	}
	/**
	 * Override. This method adds all nodes in the graph to the 
	 * list of nodes that gravitation is applied to
	 */
	public void calculateNodesToApplyGravitationTo()
	{
		LinkedList nodeList = new LinkedList();
		LGraph graph;

		for (Object obj : this.graphManager.getGraphs())
		{
			graph = (LGraph) obj;
			nodeList.addAll(graph.getNodes());			
		}

		this.graphManager.setAllNodesToApplyGravitation(nodeList);

	}
	/**
	 * Override. This method introduces one more repulsion force to separate 
	 * overlapping clusters. If polygons of two clusters overlap they are 
	 * pushed apart in the direction that makes the repulsion force minimum.
	 */	
	public void calcRepulsionForces()
	{	
		super.calcRepulsionForces();
		if (this.totalIterations % 10 == 0)
			this.calcZoneGraphRepulsionForces();
	}
	
	/**
	 * Override method for spring forces.
	 */
	public void calcSpringForces()
	{
		super.calcSpringForces();
		if (this.totalIterations % 10 == 0)
			this.calcZoneGraphSpringForces();					
	}
	
	/**
	 * Override method for layout purposes. isInterCluster property for 
	 * all edges is set.
	 */
	public boolean layout() 
	{	
		// form zones from the clusters
		if (this.graphManager.getClusterManager().getClusters().size() > 1)
		{	
			((ClusterGraphManager) (this.graphManager)).formClusterZones();						
		}
		
		this.setIsInterClusterPropertiesOfAllEdges();		
		super.layout();	
		
		if (LayoutConstants.TESTS_ACTIVE)
		{
			this.showZonesInAFrame(); //test
			//this.testEdgeLengths(); //test
			//this.testPostProcess(); //test
		}
		return true;
	}
	
	// TODO: TEST METHOD to be deleted
	public void printPolygons()
	{
		for (Object o: this.graphManager.getClusterManager().getClusters())
		{
			Cluster c = (Cluster) o;
			System.out.println("Cluster ID "+c.getClusterID());
			System.out.println("There are " + c.getNodes().size() + " nodes");
			System.out.println("Polygon Points: " + c.getPolygon());
			for (Object p: c.getPolygon())
			{
				PointD pt = (PointD) p;
				System.out.println("Pt x:" + pt.x + " , y:" + pt.y);
			}
		}

		for (Object o: this.graphManager.getClusterManager().getClusters())
		{
			Cluster c = (Cluster) o;
			System.out.println("Nodes are");
			for (Object n : c.getNodes())
			{
				ClusterNode node = (ClusterNode) n;
				System.out.println(" " + node.label);
			}
			System.out.println("Pg for cluster "+c.getClusterID()+ " is: " + c.getPolygon());
		}
		
		for (Object o: ((ClusterGraphManager) (this.graphManager)).zoneGraph.getNodes())
		{
			ZoneNode z = (ZoneNode) o;
			System.out.println("Zone " +z.label + " clusters are: "+z.polygon);
		}		
	}
	
	// TODO: Test method to be deleted
	public void polygonOverlapTest()
	{
		ArrayList <Cluster> clusters = this.graphManager.getClusterManager().getClusters();
		Object [] overlap;
		for (int i =  0; i < clusters.size() - 1; i++)
		{
			ArrayList<PointD> polygonI = clusters.get(i).getPolygon();
			
			for (int j = i + 1; j < clusters.size(); j++) 
			{
				System.out.println("Checking clusters "+clusters.get(i).getClusterID() + ", "+clusters.get(j).getClusterID());
				ArrayList<PointD> polygonJ = clusters.get(j).getPolygon();
				overlap = IGeometry.convexPolygonOverlap(polygonI,polygonJ);
				if ((double) overlap[0] != 0.0)
				{
					//overlap[0]:  overlap amount
					//overlap[1]:  overlap direction
						
					PointD temp;
					temp = IGeometry.getXYProjection(((double) overlap[0]),
							((PointD) overlap[1]));
								
					System.out.println("Overlap. x:" + temp.x + ", y:" + temp.y);
				}
				System.out.println("No overlap");
			}			
		}
	}
	
	/**
	 * Override. This method changes the ideal edge length according to the edge 
	 * type. The edge is short if it is between two nodes of the same zone. The 
	 * edge is long between nodes of two different clusters. If the nodes are 
	 * zone neighbors the edge is longer. The edge between two not-neighboring 
	 * zones is even longer.  
	 */
	protected void calcIdealEdgeLengths()
	{
		
		ClusterEdge edge;
		
		for (Object obj : this.graphManager.getAllEdges())
		{
			edge = (ClusterEdge) obj;			
			edge.idealLength = super.idealEdgeLength *
					(ClusterConstants.DEFAULT_SAME_CLUSTER_EDGE_LENGTH_FACTOR);
			
			if (edge.isInterCluster())
			{
				if (edge.areNodesZoneNeighbors)
				{
					edge.idealLength = edge.idealLength *
						(ClusterConstants.DEFAULT_ZONE_NEIGHBOR_EDGE_LENGTH_FACTOR); // mid
				}
				else
				{
					edge.idealLength = edge.idealLength * 
						(ClusterConstants.DEFAULT_INTER_ZONE_EDGE_LENGTH_FACTOR);
				}
			}
		}
	}
	
	/**
	 * This method calculates the spring forces between the zones. It is
	 * called by the override method calcSpringForces. 
	 */
	public void calcZoneGraphSpringForces()
	{
		ZoneGraph zoneGraph = ((ClusterGraphManager) (this.graphManager)).zoneGraph;
		List zoneEdges = zoneGraph.getEdges();
		List zoneNodes = zoneGraph.getNodes();
		
		for (int i = 0; i < zoneEdges.size(); i++)
		{
			ZoneEdge edge = (ZoneEdge) zoneEdges.get(i);
			
			assert !this.uniformLeafNodeSizes;
			
			this.calcSpringForce(edge, edge.idealLength);
		}						

		for (Object o :zoneNodes)
		{
			this.applySpringForcesToZoneMembers((ZoneNode) o);
		}
	}
	/**
	 * This method calculates the spring forces between the zones. It is
	 * called by the override method calcRepulsionForces. 
	 */
	public void calcZoneGraphRepulsionForces()
	{		
		List zones = (((ClusterGraphManager) (this.graphManager))).zoneGraph.getNodes();
		
		// Checking all zone node pairs
		for (int i = 0; i < zones.size() - 1; i++)
		{
			ZoneNode zoneA = (ZoneNode) zones.get(i);
			
			for (int j = i + 1; j < zones.size(); j++)
			{
				ZoneNode zoneB = (ZoneNode) zones.get(j);							 
				 
				// calculate repulsion forces for zone graph nodes 
				this.calcRepulsionForce(zoneA, zoneB);
			}
						 
			this.applyRepulsionForcesToZoneMembers(zoneA);
		}
	}
	
	/**
	 * This method applies the spring forces calculated for the zone graph to the
	 * nodes that belong to the zone
	 */
	public void applySpringForcesToZoneMembers(ZoneNode zone)
	{
		// get the id of the zone to match with the cluster
		int clusterID = Integer.parseInt(zone.label);

		// match zone id to cluster id and get the cluster
		Cluster cluster = this.graphManager.getClusterManager().getClusterByID(clusterID);
		
		for (Object o:cluster.getNodes())
		{
			CoSENode node = (CoSENode) o;
			
			node.springForceX +=  
				( (ClusterConstants.DEFAULT_ZONE_SPRING_FACTOR) * zone.springForceX);				
			node.springForceY += 
				( (ClusterConstants.DEFAULT_ZONE_SPRING_FACTOR) * zone.springForceY);
		}
	}
	
	/**
	 * This method applies the repulsion forces calculated for the zone graph to the
	 * nodes that belong to the zone
	 */
	public void applyRepulsionForcesToZoneMembers(ZoneNode zone)
	{
		// get the id of the zone to match with the cluster
		int clusterID = Integer.parseInt(zone.label);
		
		// match zone id to cluster id and get the cluster
		Cluster cluster = this.graphManager.getClusterManager().getClusterByID(clusterID);
		
		for (Object o:cluster.getNodes())
		{
			CoSENode node = (CoSENode) o;
			
			node.repulsionForceX += 
					(ClusterConstants.DEFAULT_ZONE_REPULSION_FACTOR * zone.repulsionForceX);			
			node.repulsionForceY += 
					(ClusterConstants.DEFAULT_ZONE_REPULSION_FACTOR * zone.repulsionForceY);
		}
	}
	
	/**
	 * This method changes the center points of the ZoneNodes. It is called from the
	 * override method moveNodes.
	 */
	protected void moveZoneGraphNodes()
	{
		List zones = (((ClusterGraphManager) (this.graphManager))).zoneGraph.getNodes();

		for (Object o: zones)
		{			
			ZoneNode node = (ZoneNode) o;
			node.move();
		}	
	}
	
	/**
	 * This method checks and sets the isInterCluster property for all edges.
	 */
	public void setIsInterClusterPropertiesOfAllEdges()
	{
		// set isInterCluster property for all edges
		for (Object edge : this.graphManager.getAllEdges())
		{
			ClusterEdge e = (ClusterEdge) edge;
			e.setIsInterCluster();			
		}
	}
	/**
	 * Override. Moves ZoneGraph nodes too.
	 */
	public void moveNodes()
	{
		super.moveNodes();
		this.moveZoneGraphNodes();				
	}
	
	// TODO: Test method to be deleted
	public void printNodeInfo(String place)
	{
		for (Object o: this.graphManager.getAllNodes())
		{
			CoSENode node = (CoSENode) o;
			if (node.label.startsWith("T"))
			{
				System.out.print("Node label:" + node.label + " " + place);
				System.out.println(" x:" + node.getCenterX() + " y:" + node.getCenterY());
				System.out.println("Repulsion forces x:" + node.repulsionForceX+" y:"+node.repulsionForceY);
				System.out.println("");
			}
		}

	}
	
	// TODO: Test method to be deleted
	public void showZonesInAFrame()
	{
		ClusterGraphManager gm = ((ClusterGraphManager) (this.graphManager));
		ArrayList<Cluster> clusters = gm.getClusterManager().getClusters();
		ZoneGraph zoneGraph = ((ClusterGraphManager) (this.graphManager)).zoneGraph;
		ZoneTestFrame testFrame = new ZoneTestFrame(clusters,zoneGraph);
		testFrame.setVisible(true);
		
		LGraph graph = (LGraph) (this.graphManager.getGraphs().get(0));
		graph.updateBounds(true);
	}
	
	// TODO: Test method to be deleted
	public void testEdgeLengths()
	{
		System.out.println("ZONE EDGES");
		List zoneEdges = ((ClusterGraphManager) this.graphManager).zoneGraph.getEdges();
		for (Object o: zoneEdges)
		{
			ZoneEdge e = (ZoneEdge) o;
			System.out.println("Edge " + e.label + " lenght:" + e.getLength());
		}
		
		System.out.println("CLUSTER EDGES");
		Object [] edges = this.getGraphManager().getAllEdges();
		for (Object o: edges)
		{
			ClusterEdge e = (ClusterEdge) o;
			System.out.println("Edge " + e.label + " lenght:" + e.getLength());
		}		
	}
	// TODO: Test method to be deleted
	public void testEdgeTypes()
	{
		for (Object o: this.getGraphManager().getAllEdges())
		{
			ClusterEdge e = (ClusterEdge) o;
			//System.out.print("Edge Source: " + e.getSource().label + " Edge Target: " + e.getTarget().label);
			//System.out.println(" InterCluster: " + e.isInterCluster() + " ZoneNeigbors: " + e.areNodesZoneNeigbors());
		}
	}
	
	/** This method is called after the layout process to see if a node is 
	 *  overlapping with a zone that it does not belong to 
	 */
	public void testPostProcess()
	{
		Object [] nodes = this.getGraphManager().getAllNodes();
		List clusters = this.getGraphManager().getClusterManager().getClusters();
		ArrayList<PointD> zonePolygon;
		Object [] overlap;
		
		for (Object o: nodes) 
		{
			ClusterNode node = (ClusterNode) o;
			List<Cluster> nodeZones = node.getClusters();
			
			// Get the node polygon 
			double left = node.getLeft();
			double right = node.getRight();
			double top = node.getTop();
			double bottom = node.getBottom();
			ArrayList <PointD> nodePolygon = new ArrayList<PointD>(); 
			nodePolygon.add(new PointD(left, top));
			nodePolygon.add(new PointD(right, top));
			nodePolygon.add(new PointD(right, bottom));
			nodePolygon.add(new PointD(left, bottom));
						
			// Check if it overlaps with the zone polygons
			for (Object c : clusters)
			{
				Cluster zone = (Cluster) c;
				zonePolygon = zone.getPolygon();
				
				// Compare two polygons
				overlap = IGeometry.convexPolygonOverlap(nodePolygon, zonePolygon);
				
				if ((double) overlap[0] != 0.0) 
				{
					//System.out.println("Overlap with zone" +zone.getClusterID());
					int flag = 0;
					for (Object nz : nodeZones)
					{
						Cluster zoneCluster = (Cluster) nz;
						if (!zone.equals(zoneCluster))
						{
							flag = zone.getClusterID();
						}
					}
					
					if (flag != 0)
					{
						System.out.println(node.label + " overlaps with zone " + flag);
					}
				}
			}
		}
	}
	/**
	 * 
	 */
	public void calcZoneGraphGravitationalForces()
	{		
		List zones = (((ClusterGraphManager) (this.graphManager))).zoneGraph.getNodes();
		
		for (int i = 0; i < zones.size(); i++)
		{
			ZoneNode zone = (ZoneNode) zones.get(i);
			PointD center = zone.center;
			
			// get the id of the zone to match with the cluster
			int clusterID = Integer.parseInt(zone.label);
			
			// match zone id to cluster id and get the cluster
			Cluster cluster = this.graphManager.getClusterManager().getClusterByID(clusterID);
			
			for (Object o:cluster.getNodes())
			{
				CoSENode node = (CoSENode) o;
				
				double distanceX;
				double distanceY;

				distanceX = node.getCenterX() - center.x;
				distanceY = node.getCenterY() - center.y;

				node.gravitationForceX = -this.gravityConstant * distanceX;
				node.gravitationForceY = -this.gravityConstant * distanceY;		
			}
		}						 		
	}
	public void calcGravitationalForces()
	{
		super.calcGravitationalForces();
		if (this.totalIterations % 10 == 0)
			this.calcZoneGraphGravitationalForces();
	}
}