package org.ivis.layout.cluster;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.ivis.layout.Cluster;
import org.ivis.layout.LGraph;
import org.ivis.layout.LNode;
import org.ivis.layout.Layout;
import org.ivis.layout.LayoutConstants;
import org.ivis.layout.cose.CoSEEdge;
import org.ivis.layout.cose.CoSEGraphManager;
import org.ivis.layout.cose.CoSELayout;
import org.ivis.layout.cose.CoSENode;
import org.ivis.util.IntegerQuickSort;

/**
 * This class implements a graph-manager for Cluster layout specific data and
 * functionality.
 *
 * @author Can Cagdas Cengiz
 *
 * Copyright: i-Vis Research Group, Bilkent University, 2007 - present
 */

public class ClusterGraphManager extends CoSEGraphManager
{	
	HashMap overlappingClusterMap;
	
	// first element is the new cluster
	// second element is the list of clusters
	ArrayList zoneGraphEdgeTable;
	
	int maxClusterId;
	ArrayList zoneGraphEdges;
	ArrayList zoneGraphNodes;
	ZoneGraph zoneGraph;
	
	ArrayList firstLevelClusters;
	// -----------------------------------------------------------------------------
	// Section: Constructors and initialization
	// -----------------------------------------------------------------------------
	public ClusterGraphManager(Layout layout)
	{
		super(layout);
		
		// copy the initial clusters before dividing into zones
		firstLevelClusters = new ArrayList<Cluster>();
		
		overlappingClusterMap = new HashMap();
		zoneGraphEdgeTable = new ArrayList<>();
		zoneGraphNodes = new ArrayList<CoSENode>();
		zoneGraphEdges = new ArrayList<CoSEEdge>();
		zoneGraph = new ZoneGraph(new CoSELayout());
	}
	

	/**
	 * This method finds the cluster zones for each overlap and modifies the nodes and 
	 * edges to be added to the zone graph
	 */
	public void formClusterZones()
	{
		// Mark edges if they are between zone neighbors
		// set areNodesZoneNeighbors property for all edges
		for (Object edge : this.getAllEdges())
		{
			ClusterEdge e = (ClusterEdge) edge;
			e.setAreNodesZoneNeighbors();			
		}
		// Get a copy of the clusters at the beginning. 
		// For possible future usage.
		for (Object o: this.getClusterManager().getClusters())
		{
			Cluster c = (Cluster) o;
			Cluster newCluster = new Cluster(c);
			this.firstLevelClusters.add(newCluster);
		}
		
		this.findZones();
				
		// creating one node for each cluster zone
		this.addClustersToZoneGraph();
		// creating edges in between the related zones
        this.addEdgesToZoneGraph();		
	}
	
	public void updateBounds()
	{
		super.updateBounds();
		ClusterLayout currentLayout = ((ClusterLayout) this.getLayout());
		double coolingFactor = (currentLayout.coolingFactor);
		int iterationFrequency =  (int) ((1 - coolingFactor) * 8) + 1 ;
		if (currentLayout.getTotalIterations() % iterationFrequency == 0)
		{
			for(Object o: this.getClusterManager().getClusters())
			{
				Cluster c = (Cluster) o;
				c.calculatePolygon();
			}
		}
	}
	
	/**
	 * This method traces the zoneGraphEdgeTable to form the edges 
	 * between the cluster zones.
	 */
	private void addEdgesToZoneGraph() 
	{
		for (Object o: this.zoneGraphEdgeTable)
		{
			ArrayList info = (ArrayList) o;
			Cluster source = (Cluster) info.get(0);
			List target = (List) info.get(1);
			
			CoSENode sourceNode = null;
			CoSENode targetNode = null;
			for (Object sn: zoneGraph.getNodes())
			{
				sourceNode = (CoSENode) sn;

				if(sourceNode.label.equals(Integer.toString(source.getClusterID())))
					break;					
			}

			for (Object tn: target)
			{
				Cluster targetN = (Cluster) tn;				
			
				for (Object n : zoneGraph.getNodes())
				{
					targetNode = (CoSENode) n;
				
					if(targetNode.label.equals(Integer.toString(targetN.getClusterID())))
					{							
						zoneGraph.add(new ZoneEdge(), sourceNode, targetNode);
					}			
				}				
			}			
		}		
	}

	/**
	 * This method finds the cluster zones for each overlap and modifies the nodes and 
	 * edges to be added to the zone graph
	 */
	private void findZones() 
	{
		
		maxClusterId = getGreatestClusterId();
		Object [] nodes = this.getAllNodes();
		//System.out.println("TESTFN");		
		for (Object o: nodes)
		{
			List clusters;
			CoSENode node = (CoSENode) o;
			clusters = node.getClusters();
			List<Object> clusterIdList = new ArrayList<Object>();
			
			// if the node belongs to multiple clusters
			if (clusters.size() > 1)
			{
				
				// taking the cluster ids to store in an ArrayList
				for (Object c: clusters)
				{
					Cluster cl = (Cluster) c;
					clusterIdList.add(cl.getClusterID());
				}
				
				// sorting the cluster ids for a node that belongs 
				// to multiple clusters
				IntegerQuickSort intSort = new IntegerQuickSort(clusterIdList);
				intSort.quicksort();
			
				// create overlapping cluster strings				
				String ocString = "";
				
				for (Object c: clusterIdList)
				{
					int cid = (Integer) c;					
					ocString = ocString + cid + "-";
				}
				
				
				// if the cluster is created before
				if (overlappingClusterMap.containsKey(ocString))
				{
					Cluster oc = (Cluster) overlappingClusterMap.get(ocString);
					node.resetClusters();
					node.addCluster(oc);
				}
				else
				{
					this.getClusterManager().createCluster(generateClusterID(), null);
					Cluster newCluster = this.getClusterManager().getClusterByID(maxClusterId);
					overlappingClusterMap.put(ocString, newCluster);
					
					ArrayList temp = new ArrayList();
					temp.add(newCluster);
					temp.add(new ArrayList(node.getClusters()));
					
					zoneGraphEdgeTable.add(temp);
					node.resetClusters();
					node.addCluster(newCluster);
					
				}				
			}
			/*else if ((clusters.size() == 0) ||
					((clusters.size() == 1 ) && (((Cluster) clusters.get(0)).getClusterID() == 0)))
			{
				System.out.println("TEST");
				this.getClusterManager().createCluster(generateClusterID(), null);
				Cluster newCluster = this.getClusterManager().getClusterByID(maxClusterId);
			}*/
		}		
	}
	
	/**
	 * This method traces the clusters in cluster manager to form 
	 * the nodes between the cluster zones.
	 */
	private void addClustersToZoneGraph()
	{
		for (Object o: this.getClusterManager().getClusters())
		{			
			Cluster c = (Cluster) o;
			if (c.getNodes().size() > 0)
			{
				
				ZoneNode newNode = new ZoneNode(this, null);
				
				// set labels as the clusterIDs so that we can find nodes
				newNode.label = Integer.toString(c.getClusterID());
							
				// set ZoneNode polygon as cluster polygon
				// zone polygon simply points to the cluster polygon
				c.calculatePolygon();
				newNode.polygon = c.getPolygon();
				
				zoneGraph.add(newNode);
			}									
		}		
	}
	
	private int generateClusterID()
	{				
		this.maxClusterId++;
		return maxClusterId;		
	}
	
	private int getGreatestClusterId()
	{
		ArrayList<Integer> ids = this.getClusterManager().getClusterIDs();
		return ids.get(ids.size()-1);
	}

}
