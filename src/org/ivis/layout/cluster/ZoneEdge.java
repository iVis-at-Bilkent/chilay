package org.ivis.layout.cluster;

import org.ivis.layout.cose.CoSEEdge;
import org.ivis.layout.cose.CoSENode;


/**
 * This class implements Zone Graph specific data and functionality for edges.
 *
 * @author Can Cagdas Cengiz
 *
 * Copyright: i-Vis Research Group, Bilkent University, 2007 - present
 */
public class ZoneEdge extends CoSEEdge
{
// -----------------------------------------------------------------------------
// Section: Constructors and initializations
// -----------------------------------------------------------------------------
	/**
	 * Constructor
	 */
	public ZoneEdge(CoSENode source, CoSENode target, Object vEdge)
	{
		super(source, target, vEdge);
	}
	
	public ZoneEdge()
	{
		this(null, null, null);
	}
}