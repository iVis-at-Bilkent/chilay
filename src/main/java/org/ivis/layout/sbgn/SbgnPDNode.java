package org.ivis.layout.sbgn;

import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;

import org.ivis.layout.LEdge;
import org.ivis.layout.LGraphManager;
import org.ivis.layout.LNode;
import org.ivis.layout.cose.CoSELayout;
import org.ivis.layout.cose.CoSENode;
import org.ivis.layout.fd.FDLayoutConstants;
import org.ivis.util.IMath;

/**
 * This class implements SBGN specific data and functionality for nodes.
 * 
 * @author Begum Genc
 * @author Istemi Bahceci
 * 
 *         Copyright: i-Vis Research Group, Bilkent University, 2007 - present
 */
public class SbgnPDNode extends CoSENode
{
	// relativity forces applied on the node.
	protected double relativityConstraintX;
	protected double relativityConstraintY;

	/**
	 * This parameter is used in DFS to find ordering of the complex members.
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

	/**
	 * This method checks if the given node contains any unmarked complex nodes
	 * in its child graph.
	 * 
	 * @return true - if there are unmarked complex nodes false - otherwise
	 */
	public boolean containsUnmarkedComplex()
	{
		if (this.getChild() == null)
			return false;
		else
		{
			for (Object child : this.getChild().getNodes())
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
	public ArrayList<SbgnPDNode> getChildrenNeighbors(String edgeType)
	{
		ArrayList<SbgnPDNode> neighbors = new ArrayList<SbgnPDNode>();

		for (int i = 0; i < this.getEdges().size(); i++)
		{
			LEdge e = (LEdge) this.getEdges().get(i);

			if (e.getSource().equals(this) && !e.getTarget().equals(this))
			{
				SbgnPDNode s = (SbgnPDNode) e.getTarget();

				if (edgeType != null && e.equals(edgeType))
				{
					neighbors.add(s);
				}
				if (edgeType == null)
					neighbors.add(s);

			}
		}
		return neighbors;
	}

	public SbgnPortNode getOtherPortNode(SbgnPortNode s)
	{
		if (this.type.equals(SbgnPDConstants.PROCESS))
		{
			String desiredType = "";
			if (s.type.equals(SbgnPDConstants.PRODUCTION_PORT))
				desiredType = SbgnPDConstants.CONSUMPTION_PORT;
			else if (s.type.equals(SbgnPDConstants.CONSUMPTION_PORT))
				desiredType = SbgnPDConstants.PRODUCTION_PORT;

			for (Object o : getEdges())
			{
				SbgnPDEdge rigidEdge = (SbgnPDEdge) o;
				if (rigidEdge.type.equals(SbgnPDConstants.RIGID_EDGE)
						&& rigidEdge.getOtherEnd(this).type.equals(desiredType))
					return (SbgnPortNode) rigidEdge.getOtherEnd(this);
			}
			return s;
		}
		return null;
	}

	@Override
	public void move()
	{
		CoSELayout layout = (CoSELayout) this.graphManager.getLayout();
		this.displacementX = layout.coolingFactor
				* (this.springForceX + this.repulsionForceX
						+ this.gravitationForceX + this.relativityConstraintX);
		this.displacementY = layout.coolingFactor
				* (this.springForceY + this.repulsionForceY
						+ this.gravitationForceY + this.relativityConstraintY);

		if (Math.abs(this.displacementX) > layout.maxNodeDisplacement)
		{
			this.displacementX = layout.maxNodeDisplacement
					* IMath.sign(this.displacementX);
		}

		if (Math.abs(this.displacementY) > layout.maxNodeDisplacement)
		{
			this.displacementY = layout.maxNodeDisplacement
					* IMath.sign(this.displacementY);
		}

		if (this.child == null)
		// a simple node, just move it
		{
			this.moveBy(this.displacementX, this.displacementY);
		}
		else if (this.child.getNodes().size() == 0)
		// an empty compound node, again just move it
		{
			this.moveBy(this.displacementX, this.displacementY);
		}
		// non-empty compound node, propogate movement to children as well
		else
		{
			this.propogateDisplacementToChildren(this.displacementX,
					this.displacementY);
		}

		layout.totalDisplacement += Math.abs(this.displacementX)
				+ Math.abs(this.displacementY);

		this.springForceX = 0;
		this.springForceY = 0;
		this.repulsionForceX = 0;
		this.repulsionForceY = 0;
		this.gravitationForceX = 0;
		this.gravitationForceY = 0;
		this.displacementX = 0;
		this.displacementY = 0;
		this.relativityConstraintX = 0;
		this.relativityConstraintY = 0;
	}

}
