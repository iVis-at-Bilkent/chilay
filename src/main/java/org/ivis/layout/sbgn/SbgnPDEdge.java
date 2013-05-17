package org.ivis.layout.sbgn;

import org.ivis.layout.cose.CoSEEdge;
import org.ivis.layout.cose.CoSENode;

/**
 * This class implements SBGN Process Diagram specific data and functionality for edges.
 *
 * @author Begum Genc
 *
 * Copyright: i-Vis Research Group, Bilkent University, 2007 - present
 */
public class SbgnPDEdge extends CoSEEdge
{

	/**
	 * Constructor
	 */
	public SbgnPDEdge(CoSENode source, CoSENode target, Object vEdge)
	{
		super(source, target, vEdge);
	}

}
