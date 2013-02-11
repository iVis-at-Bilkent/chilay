package org.ivis.layout.cluster;

import java.util.ArrayList;

import org.ivis.layout.LEdge;
import org.ivis.layout.Cluster;
import org.ivis.layout.cluster.ClusterEdge;
import org.ivis.layout.cluster.ClusterConstants;
import org.ivis.layout.cose.*;
import org.ivis.layout.fd.FDLayoutNode;

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
		
		super.calcRepulsionForces();
		
		int cid;
		FDLayoutNode node;
		Object[] lNodes = this.getAllNodes();
		ArrayList<Cluster> clusters = this.graphManager.
				getClusterManager().getClusters();
		
		System.out.println("Clusters: ");
		
		for(Cluster c:clusters)
		{
			System.out.println(c.getClusterID()); //test
		}
		
		double [][] offsets = new double[clusters.size()+1][2];
		ArrayList<Object []> overlaps;
		
		// calculate polygons for each cluster
		
		for(Cluster c:clusters)
		{
			c.calculatePolygon();
		}
		
		// take all overlap information
		
		overlaps = this.graphManager.getClusterManager().getOverlapInformation();
		
		// initialize a matrix to hold the repulsion forces in x-y
		// offsets are the repulsion forces to be applied to the
		// clusters.
		
		for(int i = 0; i < clusters.size() + 1; i++)
		{
			offsets[i][0]= 0.0; // repulsion to cluster i, in x
			offsets[i][1]= 0.0; // repulsion to cluster i, in y
			
		}
		
		// map these overlaps to the matrix
		
		for (Object [] o:overlaps)
		{
			int cid1 = (Integer) o[0];
			int cid2 = (Integer) o[1];
			double x = (Double) o[2];
			double y = (Double) o[3];
			
			offsets[cid1][0] -= (x * ClusterConstants.DEFAULT_CLUSTER_REPULSION);
			offsets[cid1][1] -= (y * ClusterConstants.DEFAULT_CLUSTER_REPULSION);
			offsets[cid2][0] += (x * ClusterConstants.DEFAULT_CLUSTER_REPULSION);
			offsets[cid2][1] += (y * ClusterConstants.DEFAULT_CLUSTER_REPULSION);
			
		}
		
		// pass the repulsion forces to the nodes to be moved
		
		for(Object o:lNodes)
		{
			node = (FDLayoutNode) o;
			
			//System.out.println("node's cluster id: "+node.getClusterID());
			
			if (node.getClusterID() != null)
			{
				cid = Integer.parseInt(node.getClusterID());
				node.repulsionForceX += offsets[cid][0];
				node.repulsionForceY += offsets[cid][1];
				
			}
		}
		
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
			
			if(edge.isInterCluster())
			{
				edge.idealLength = edge.idealLength * 
						(ClusterConstants.DEFAULT_INTER_CLUSTER_EDGE_LENGTH_RATIO);
			}

		}
	}
	
	/**
	 * Override. This method checks whether the edges are inter-cluster or
	 * intra-cluster before making the layout.  
	 */
	public boolean layout()
	{
		
		for(Object edge : this.graphManager.getAllEdges())
		{
			ClusterEdge e = ((ClusterEdge) edge);
			e.checkIsInterCluster();
		}
		
		super.layout();
		
		return true;
	}
	
}