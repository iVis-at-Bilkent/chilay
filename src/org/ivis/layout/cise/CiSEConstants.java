package org.ivis.layout.cise;

import org.ivis.layout.fd.FDLayoutConstants;

/**
 * This class maintains the constants used by CiSE layout.
 *
 * @author: Ugur Dogrusoz
 *
 * Copyright: i-Vis Research Group, Bilkent University, 2007 - present
 */
public class CiSEConstants extends FDLayoutConstants
{
// -----------------------------------------------------------------------------
// Section: CiSE layout user options
// -----------------------------------------------------------------------------
	/**
	 * Amount of separation of nodes on the associated circle
	 */
	public static final int DEFAULT_NODE_SEPARATION =
		FDLayoutConstants.DEFAULT_EDGE_LENGTH;

	/**
	 * Inter-cluster edge length factor (2.0 means inter-cluster edges should be
	 * twice as long as intra-cluster edges)
	 */
	public static final double
		DEFAULT_IDEAL_INTER_CLUSTER_EDGE_LENGTH_COEFF = 2.0;

	/**
	 * Whether to enable pulling nodes inside of the circles
	 */
	public static final boolean DEFAULT_ALLOW_NODES_INSIDE_CIRCLE = true;

	/**
	 * Max percentage of the nodes in a circle that can be inside the circle
	 */
	public static final double DEFAULT_MAX_RATIO_OF_NODES_INSIDE_CIRCLE = 0.2;

// -----------------------------------------------------------------------------
// Section: CiSE layout remaining contants
// -----------------------------------------------------------------------------
	/**
	 * Maximum rotation angle
	 */
	public static final double MAX_ROTATION_ANGLE = Math.PI / 36.0;
	public static final double MIN_ROTATION_ANGLE = -MAX_ROTATION_ANGLE;

	/**
	 * Number of iterations without swap or swap prepartion
	 */
	public static final int SWAP_IDLE_DURATION = 95;

	/**
	 * Number of iterations required for collecting information about swapping
	 */
	public static final int SWAP_PREPERATION_DURATION = 5;

	/**
	 * Number of iterations that should be done in between two swaps.
	 */
	public static final int SWAP_PERIOD =
		SWAP_IDLE_DURATION + SWAP_PREPERATION_DURATION;

	/**
	 * Buffer for swapping
	 */
	public static final int MIN_DISPLACEMENT_FOR_SWAP =
		FDLayoutConstants.DEFAULT_EDGE_LENGTH / 12;
}
