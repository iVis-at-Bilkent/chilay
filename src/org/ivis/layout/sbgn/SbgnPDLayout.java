package org.ivis.layout.sbgn;

import org.ivis.layout.LNode;
import org.ivis.layout.cose.CoSELayout;

/**
 * This class implements the layout process of SBGN notation.
 * 
 * @author Begum Genc
 * 
 *         Copyright: i-Vis Research Group, Bilkent University, 2007 - present
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
	 * This method creates a new node associated with the input view node.
	 */
	public LNode newNode(Object vNode)
	{
		return new SbgnPDNode(this.graphManager, vNode, "");
	}

	/**
	 * This method performs layout on constructed l-level graph. It returns true
	 * on success, false otherwise.
	 */
	public boolean layout()
	{
		return super.layout();
		
	}
	
	@Override
	public void moveNodes()
	{
		super.moveNodes();
	}
	
	@Override
	public void calcSpringForces()
	{
		super.calcSpringForces();
	}

}
