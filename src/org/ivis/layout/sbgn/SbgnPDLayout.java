package org.ivis.layout.sbgn;

import org.ivis.layout.cose.CoSELayout;

/**
 * This class implements the layout process of SBGN notation. 
 * 
 * @author Begum Genc
 *
 * Copyright: i-Vis Research Group, Bilkent University, 2007 - present
 */
public class SbgnPDLayout extends CoSELayout
{

	/**
	 * The constructor creates and associates with this layout a new graph
	 * manager as well.
	 */
	public SbgnPDLayout()
	{
		super();
	}
	
	/**
	 * This method performs layout on constructed l-level graph. It returns true
	 * on success, false otherwise.
	 */
	public boolean layout()
	{
		super.layout();
		return false;
	}

}
