package org.ivis.layout.cluster;

import java.awt.Point;
import java.awt.Dimension;

import org.ivis.layout.LGraphManager;
import org.ivis.layout.cose.CoSENode;


/**
 * This class implements CoSE specific data and functionality for nodes.
 *
 * @author Erhan Giral
 * @author Ugur Dogrusoz
 *
 * Copyright: i-Vis Research Group, Bilkent University, 2007 - present
 */
public class ClusterNode extends CoSENode
{	
	// -----------------------------------------------------------------------------
	// Section: Constructors and initialization
	// -----------------------------------------------------------------------------
	
	/**
	 * Constructor
	 */
	public ClusterNode(LGraphManager gm, Object vNode)
	{
		super(gm, vNode);
	}

	/**
	 * Alternative constructor
	 */
	public ClusterNode(LGraphManager gm, Point loc, Dimension size, Object vNode)
	{
		super(gm, loc, size, vNode);
	}

}