package org.ivis.layout.cluster;

import org.ivis.layout.LGraph;
import org.ivis.layout.LNode;
import org.ivis.layout.Layout;

/**
 * This class holds Cluster Zone specific graph data and implementations
 *
 * @author: Can Cagdas Cengiz
 *
 * Copyright: i-Vis Research Group, Bilkent University, 2007 - present
 */
public class ZoneGraph extends LGraph
{
// -----------------------------------------------------------------------------
// Section: Instance variables
// -----------------------------------------------------------------------------
	/**
	 * During the zone repulsions, 
	 * CoSE nodes of zone graph is created by the help of layout instance 
	 */
	private Layout layout;
	
// -----------------------------------------------------------------------------
// Section: Constructors and initialization
// -----------------------------------------------------------------------------
	/**
	 * Constructor
	 */
	protected ZoneGraph(LNode parent, Layout layout, Object vGraph)
	{
		super(parent, layout, vGraph);
	}
	
	public ZoneGraph(Layout _layout)
	{
		this(null, _layout, null);
		this.layout = _layout;
	}

// -----------------------------------------------------------------------------
// Section: Coarsening
// -----------------------------------------------------------------------------
	/**
	 * This method will do something later (TEST)
	 */
	public void doSomething()
	{
		
	}
	
// -----------------------------------------------------------------------------
// Section: Instance variables
// -----------------------------------------------------------------------------
	public Layout getLayout()
	{
		return layout;
	}

	public void setLayout(Layout layout)
	{
		this.layout = layout;
	}
	
}
