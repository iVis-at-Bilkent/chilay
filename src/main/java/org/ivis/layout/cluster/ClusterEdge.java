package org.ivis.layout.cluster;

import org.ivis.layout.Cluster;
import org.ivis.layout.LNode;
import org.ivis.layout.cose.CoSEEdge;
import org.ivis.layout.cose.CoSENode;

/**
 * This class implements Cluster specific data and functionality for edges.
 *
 * @author Can Cagdas Cengiz
 *
 * Copyright: i-Vis Research Group, Bilkent University, 2007 - present
 */
public class ClusterEdge extends CoSEEdge
{
// -----------------------------------------------------------------------------
// Section: Instance variables
// -----------------------------------------------------------------------------
	
	/*
	 * Whether this edge is an inter-cluster one
	 */
	protected boolean isInterCluster;
	
	protected boolean areNodesZoneNeighbors;

// -----------------------------------------------------------------------------
// Section: Constructors and initializations
// -----------------------------------------------------------------------------
	/*
	 * Constructor
	 */
	public ClusterEdge(CoSENode source, CoSENode target, Object vEdge)
	{
		super(source, target, vEdge);
	}

// -----------------------------------------------------------------------------
// Section: Remaining methods
// -----------------------------------------------------------------------------

	/**
	 * This method returns whether or not this edge is an inter-cluster edge.
	 */
	public boolean isInterCluster()
	{
		return this.isInterCluster;
	}
	
	public boolean areNodesZoneNeigbors()
	{
		return this.areNodesZoneNeighbors;
	}
	
	public void setAreNodesZoneNeighbors()
	{
		this.areNodesZoneNeighbors = false;
		
		LNode targetNode = this.target;
		LNode sourceNode = this.source;
		for (Object s: sourceNode.getClusters())
		{
			Cluster sourceCluster = (Cluster) s;
			for (Object t: targetNode.getClusters())
			{
				Cluster targetCluster = (Cluster) t;
				if (targetCluster.equals(sourceCluster))
				{
					this.areNodesZoneNeighbors = true;
					return;
				}
			}			
		}
	}
	
	public void setIsInterCluster()
	{
		if ((this.source.getClusters().size() > 0 ) && 
			(this.target.getClusters().size() > 0 ))
		{
		
			// assuming that a node belongs to only one cluster
			Object sourceCluster = this.source.getClusters().get(0);
			Object targetCluster = this.target.getClusters().get(0);
			
			if (sourceCluster.equals(targetCluster))
			{
				this.isInterCluster = false;			
			}
			else
			{
				this.isInterCluster = true;
			}
		}
		else
		{
			// TODO: Perhaps we will set this to false/modify when 
			// both are unclustered.
			this.isInterCluster = true;
		}			
	}
}