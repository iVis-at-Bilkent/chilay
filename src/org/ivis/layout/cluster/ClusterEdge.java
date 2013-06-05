package org.ivis.layout.cluster;

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
	 * Whether this edge is an intercluster one
	 */
	protected boolean isInterCluster;

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
	
	
	public void setIsInterCluster()
	{
		if((this.source.getClusters().size() > 0 ) && 
				(this.target.getClusters().size() > 0 )){
		
			// assuming that a node belongs to only one cluster
			
			Object sourceCluster = this.source.getClusters().get(0);
			Object targetCluster = this.target.getClusters().get(0);
			
			if(sourceCluster.equals(targetCluster))
				this.isInterCluster = false;			
			else
				this.isInterCluster = true;
		}
		else
		{
			this.isInterCluster = true;
		}
			
	}

}