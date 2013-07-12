package org.ivis.layout.sbgn;

import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Iterator;

import org.ivis.layout.LEdge;
import org.ivis.layout.LGraphManager;
import org.ivis.layout.LNode;
import org.ivis.layout.cose.CoSENode;

/**
 * This class implements SBGN specific data and functionality for nodes.
 * 
 * @author Begum Genc
 * @author Istemi Bahceci
 
 *         Copyright: i-Vis Research Group, Bilkent University, 2007 - present
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
     *
     */
	public double orientationX;

	/**
     *
     */
	public double orientationY;

	/**
	 * This parameter is used in DFS to order complex members.
	 */
	public boolean visited;

	/**
	 * Constructor
	 */
	public SbgnPDNode(LGraphManager gm, Object vNode)
	{
		super(gm, vNode);
		this.visited = false;
	}

	/**
	 * Alternative constructor
	 */
	public SbgnPDNode(LGraphManager gm, Point loc, Dimension size, LNode vNode,
			String type)
	{
		super(gm, loc, size, vNode);
		this.type = type;
		this.visited = false;
		this.label = vNode.label;
	}

	public boolean isComplex()
	{
		return type.equalsIgnoreCase(SbgnPDConstants.COMPLEX);
	}

	protected void updateOrientation()
	{
		this.orientationX = 0;
		this.orientationY = 0;

		Iterator itr = this.edges.iterator();
		while (itr.hasNext())
		{
			double distanceX = 0;
			double distanceY = 0;
			double distance = 0;

			SbgnPDEdge edge = (SbgnPDEdge) itr.next();

			// edge is of type substrate
			if (edge.type.equals(SbgnPDConstants.CONSUMPTION))
			{
				distanceX += this.getCenterX()
						- edge.getOtherEnd(this).getCenterX();
				distanceY += this.getCenterY()
						- edge.getOtherEnd(this).getCenterY();
			} else if (edge.type.equals(SbgnPDConstants.PRODUCTION))
			{
				distanceX += edge.getOtherEnd(this).getCenterX()
						- this.getCenterX();
				distanceY += edge.getOtherEnd(this).getCenterY()
						- this.getCenterY();
			}
			distance = Math.sqrt(distanceX * distanceX + distanceY * distanceY);

			this.orientationX += distanceX / distance;
			this.orientationY += distanceY / distance;
		}
	}


	/**
	 * This method checks if the given node contains any unmarked complex nodes
	 * in its child graph.
	 * 
	 * @return true - if there are unmarked complex nodes false - otherwise
	 */
	public boolean containsUnmarkedComplex(SbgnPDNode comp)
	{
		if (comp.getChild() == null)
			return false;
		else
		{
			for (Object child : comp.getChild().getNodes())
			{
				SbgnPDNode sbgnChild = (SbgnPDNode) child;

				if (sbgnChild.isComplex() && !sbgnChild.visited)
					return true;
			}
			return false;
		}
	}
	

	/**
	 * This method returns the neighbors of a given node. Notice that the graph
	 * is directed. Therefore edges should have the given node as the source
	 * node.
	 */
	public ArrayList<SbgnPDNode> getNeighbors()
	{
		ArrayList<SbgnPDNode> neighbors = new ArrayList<>();

		for (int i = 0; i < this.getEdges().size(); i++)
		{
			LEdge e = (LEdge) this.getEdges().get(i);

			if (e.getSource().equals(this)
					&& !e.getTarget().equals(this))
			{
				SbgnPDNode s = (SbgnPDNode) e.getTarget();
				neighbors.add(s);
			}
		}
		return neighbors;
	}


}
