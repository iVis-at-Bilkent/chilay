package org.ivis.layout.sbgn;

import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;

import org.ivis.layout.LEdge;
import org.ivis.layout.LGraphManager;
import org.ivis.layout.LNode;
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
	private double relativityConstraintX;
	private double relativityConstraintY;

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
		ArrayList<SbgnPDNode> neighbors = new ArrayList<SbgnPDNode>();

		for (int i = 0; i < this.getEdges().size(); i++)
		{
			LEdge e = (LEdge) this.getEdges().get(i);

			if (e.getSource().equals(this) && !e.getTarget().equals(this))
			{
				SbgnPDNode s = (SbgnPDNode) e.getTarget();
				neighbors.add(s);
			}
		}
		return neighbors;
	}

	/**
	 * This method calculates the relativity forces for each node in the graph
	 * by examining the production and consumption neighbors.
	 */
	public void calcRelativityForce()
	{
		SbgnPDLayout layout = (SbgnPDLayout) this.graphManager.getLayout();

		int prodEdgeCount = 0;
		int consEdgeCount = 0;
		
		ArrayList<SbgnPDNode> productions = new ArrayList<SbgnPDNode>();
		ArrayList<SbgnPDNode> consumptions = new ArrayList<SbgnPDNode>();
		ArrayList<SbgnPDNode> favoredList;
		ArrayList<SbgnPDNode> unfavoredList;

		Point targetPoint = new Point();
		Point reverseTargetPoint = new Point();

		// group products and consumptions separately.
		for (Object o : this.getEdges())
		{
			SbgnPDEdge e = (SbgnPDEdge) o;
			if (e.type.equals(SbgnPDConstants.PRODUCTION))
				productions.add((SbgnPDNode) e.getTarget());
			else if (e.type.equals(SbgnPDConstants.CONSUMPTION))
				consumptions.add((SbgnPDNode) e.getSource());
		}

		// find the favored and the unfavored lists 
		// favor in terms of the edge count of the neighboring nodes.
		// more count indicates centrality -> favor.
		for(SbgnPDNode s : productions)
			prodEdgeCount += s.getEdges().size();
		for(SbgnPDNode s : consumptions)
			consEdgeCount += s.getEdges().size();
		
		if (prodEdgeCount > consEdgeCount)
		{
			favoredList = productions;
			unfavoredList = consumptions;
		}
		else
		{
			favoredList = consumptions;
			unfavoredList = productions;
		}		
		
		// calculate targeted points
		targetPoint = calcTargetPoint(favoredList);
		
		// target the unfavored nodes far away to increase the chance of hops. 
		// reverse target pt makes 180-degree angle with the target pt *------| |----*
		// (i.e.increase the distance by default edge length)
		reverseTargetPoint.x = (int) (this.getCenterX() - ((targetPoint.x - this
				.getCenterX()) + Math.signum(targetPoint.x - this.getCenterX()) * 
				layout.coolingFactor * FDLayoutConstants.DEFAULT_EDGE_LENGTH));

		reverseTargetPoint.y = (int) (this.getCenterY() - ((targetPoint.y - this
				.getCenterY()) + Math.signum(targetPoint.y - this.getCenterY()) * 
				layout.coolingFactor * FDLayoutConstants.DEFAULT_EDGE_LENGTH));

		// apply the forces
		applyRelativityForces(favoredList, targetPoint);
		applyRelativityForces(unfavoredList, reverseTargetPoint);
	}

	/**
	 * This methods applies relativity forces to members of the given list
	 * towards the given point
	 */
	private void applyRelativityForces(ArrayList<SbgnPDNode> list, Point pnt)
	{
		SbgnPDLayout layout = (SbgnPDLayout) this.graphManager.getLayout();
		double multiplier = SbgnPDConstants.RELATIVITY_CONSTRAINT_CONSTANT 
				* layout.coolingFactor;
	
		// use cooling factor in calculations (cooling fac. decreases regularly) 
		for (int i = 0; i < list.size(); i++)
		{
			SbgnPDNode s = list.get(i);
			s.relativityConstraintX = (pnt.x - s.getCenterX()) * multiplier;
			s.relativityConstraintY = (pnt.y - s.getCenterY()) * multiplier;
		}
	}

	/**
	 * This method calculates a target point to move the elements of given list.
	 * The target pt is in the middle of the given node list.
	 * The returned point should have a distance of default edge length
	 * (origin is the process node) 
	 */
	private Point calcTargetPoint(ArrayList<SbgnPDNode> list)
	{
		Point targetPnt = new Point();
		double targetVectorX = 0;
		double targetVectorY = 0;

		if (list.size() > 0)
		{
			// calculate center points of the list
			for (int i = 0; i < list.size(); i++)
			{
				targetPnt.x += list.get(i).getCenterX();
				targetPnt.y += list.get(i).getCenterY();
			}
			targetPnt.x /= list.size();
			targetPnt.y /= list.size();

			// calculate the vector pointing to that point, whose origin is the current node.
			targetVectorX = this.getCenterX() - targetPnt.x;
			targetVectorY = this.getCenterY() - targetPnt.y;

			double vectorLength = Math.sqrt(Math.pow(targetVectorX, 2)
					+ Math.pow(targetVectorY, 2));

			// accept the vectors that fall in relativity distance neighborhood of the def edge length.
			if (vectorLength < FDLayoutConstants.DEFAULT_EDGE_LENGTH 
					- SbgnPDConstants.RELATIVITY_DEVATION_DISTANCE)
			{

				targetVectorX = FDLayoutConstants.DEFAULT_EDGE_LENGTH;
				targetVectorY = FDLayoutConstants.DEFAULT_EDGE_LENGTH;
				
				targetPnt.x = (int) (this.getCenterX() - targetVectorX);
				targetPnt.y = (int) (this.getCenterY() - targetVectorY);
			}

			return targetPnt;
		}

		return null;
	}

	/**
	 * This method recalculates the displacement related attributes of this
	 * object. These attributes are calculated at each layout iteration once,
	 * for increasing the speed of the layout.
	 */
	public void move()
	{
		SbgnPDLayout layout = (SbgnPDLayout) this.graphManager.getLayout();
		this.displacementX = layout.coolingFactor
				* (this.springForceX + this.repulsionForceX
						+ relativityConstraintX + this.gravitationForceX);
		this.displacementY = layout.coolingFactor
				* (this.springForceY + this.repulsionForceY
						+ relativityConstraintY + this.gravitationForceY);

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
