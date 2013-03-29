package org.ivis.layout.sbgn;

import java.awt.Dimension;
import java.awt.Point;

import org.ivis.layout.LGraphManager;
import org.ivis.layout.cose.CoSENode;

/**
 * This class implements SBGN specific data and functionality for nodes.
 *
 * @author Begum Genc
 *
 * Copyright: i-Vis Research Group, Bilkent University, 2007 - present
 */
public class SbgnPDNode extends CoSENode
{
	/**
	 * 
	 */
	public double relativityConstraintX;
	
	/**
	 * 
	 */
	public double relativityConstraintY;
	
	/**
	 * Constructor
	 */
	public SbgnPDNode(LGraphManager gm, Object vNode, String type)
	{
		super(gm, vNode);
		this.type = type;
	}

	/**
	 * Alternative constructor
	 */
	public SbgnPDNode(LGraphManager gm, Point loc, Dimension size, Object vNode, int type)
	{
		super(gm, loc, size, vNode);
	}
}
