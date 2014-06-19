package org.ivis.layout.sbgn;

import org.ivis.layout.cose.CoSEEdge;

/**
 * This class implements SBGN Process Diagram specific data and functionality
 * for edges.
 * 
 * @author Begum Genc
 * 
 *         Copyright: i-Vis Research Group, Bilkent University, 2007 - present
 */
public class SbgnPDEdge extends CoSEEdge
{
	public int correspondingAngle;
	public boolean isProperlyOriented;

	/**
	 * Constructor
	 */
	public SbgnPDEdge(SbgnPDNode source, SbgnPDNode target, Object vEdge)
	{
		super(source, target, vEdge);
		correspondingAngle = 0;
		isProperlyOriented = false;
	}

	public SbgnPDEdge(SbgnPDNode source, SbgnPDNode target, Object vEdge, String type)
	{
		super(source, target, vEdge);
		this.type = type;
		correspondingAngle = 0;
		isProperlyOriented = false;
	}
	
}
