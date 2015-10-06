package org.ivis.layout.cluster;

import org.ivis.layout.fd.FDLayoutConstants;
import org.ivis.layout.cose.CoSEConstants;

/**
 * This class maintains the constants used by Cluster layout.
 *
 * @author: Ugur Dogrusoz
 * @author: Can Cagdas Cengiz
 *
 * Copyright: i-Vis Research Group, Bilkent University, 2007 - present
 */
public class ClusterConstants extends CoSEConstants
{
// -----------------------------------------------------------------------------
// Section: Cluster layout user options
// -----------------------------------------------------------------------------
	/*
	 * Default cluster gravity
	 */
	public static final double DEFAULT_COMPOUND_GRAVITY_STRENGTH = 6.0 *
		FDLayoutConstants.DEFAULT_COMPOUND_GRAVITY_STRENGTH;

	/**
	 * Default margins of the dummy compounds corresponding to clusters;
	 * determines how much the clusters should be separated
	 */
	public static final int DEFAULT_CLUSTER_SEPARATION = 3;


	/**
	 * Determines how much the clusters should be separated
	 */
	//public static final double DEFAULT_CLUSTER_REPULSION = 0.57;
	public static final double DEFAULT_SAME_CLUSTER_EDGE_LENGTH_FACTOR = 1;
	public static final double DEFAULT_ZONE_NEIGHBOR_EDGE_LENGTH_FACTOR = 1.3;

	/**
	 * Determines the ratio of the inter-cluster edge length to intra-cluster
	 * edge length 
	 */
	public static final double DEFAULT_INTER_ZONE_EDGE_LENGTH_FACTOR = 3;
	public static final double DEFAULT_ZONE_REPULSION_FACTOR = 1.5;
	public static final double DEFAULT_ZONE_SPRING_FACTOR = 1.6;

// -----------------------------------------------------------------------------
// Section: Cluster layout remaining contants
// -----------------------------------------------------------------------------
}