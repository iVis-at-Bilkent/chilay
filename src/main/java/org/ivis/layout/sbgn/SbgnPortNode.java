package org.ivis.layout.sbgn;

import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;

import org.ivis.layout.LGraphManager;
import org.ivis.layout.LNode;
import org.ivis.layout.fd.FDLayoutConstants;

/**
 * This class implements two port nodes for a process node of SBGN. The new two
 * nodes can be connected to their parent(process) and neighbors(parents
 * neighbors) using this class methods.
 * 
 * @author Begum Genc
 * 
 */
public class SbgnPortNode extends SbgnPDNode
{
	// relativity forces applied on the node.
	protected double relativityConstraintX;
	protected double relativityConstraintY;

	public int width;
	public int height;
	public SbgnPDNode parent;
	private LGraphManager graphManager;

	public SbgnPortNode(LGraphManager gm, Object vNode)
	{
		super(gm, vNode);
		graphManager = gm;
		this.setWidth(SbgnPDConstants.PORT_NODE_DEFAULT_WIDTH);
		this.setHeight(SbgnPDConstants.PORT_NODE_DEFAULT_HEIGHT);

		// TODO better labeling?
		this.label = "glyph9" + gm.getAllNodes().length;
	}

	public SbgnPortNode(LGraphManager gm, Point loc, Dimension size,
			LNode vNode, String type)
	{
		super(gm, loc, size, vNode, type);
	}

	/**
	 * Connect the port node to its process node (parent) and connect the edges
	 * of neighbor nodes to the port node by taking into account the type
	 * (consumption port or production port)
	 */
	public void connect(String type, SbgnPDNode parent)
	{
		this.type = type;
		this.parent = parent;

		this.parent.getOwner().add(this);
		this.setOwner(this.parent.getOwner());

		// add a rigid edge between the process node and port node
		SbgnPDEdge rigid = new SbgnPDEdge(parent, this, null,
				SbgnPDConstants.RIGID_EDGE);
		rigid.label = "" + (this.graphManager.getAllEdges().length + 1);
		this.parent.getOwner().add(rigid, parent, this);

		if (type.equals(SbgnPDConstants.PRODUCTION_PORT))
		{
			// initial placement. place it on the right of the process node
			this.setCenter(this.parent.getCenterX()
					+ SbgnPDConstants.RIGID_EDGE_LENGTH,
					this.parent.getCenterY());

			// change connections from process node&neighbors to port&neighbors.
			for (int i = 0; i < parent.getEdges().size(); i++)
			{
				SbgnPDEdge sEdge = (SbgnPDEdge) parent.getEdges().get(i);

				if (sEdge.type.equals(SbgnPDConstants.PRODUCTION))
				{
					parent.getEdges().remove(sEdge);
					sEdge.setSource(this);
					this.getEdges().add(sEdge);
					i--;
				}
			}
		}

		else if (type.equals(SbgnPDConstants.CONSUMPTION_PORT))
		{
			// initial placement. place it on the left of the process node
			this.setCenter(this.parent.getCenterX()
					- SbgnPDConstants.RIGID_EDGE_LENGTH,
					this.parent.getCenterY());

			// change connections from process node&neighbors to port&neighbors.
			for (int i = 0; i < parent.getEdges().size(); i++)
			{
				SbgnPDEdge sEdge = (SbgnPDEdge) parent.getEdges().get(i);

				if (sEdge.type.equals(SbgnPDConstants.CONSUMPTION))
				{
					parent.getEdges().remove(sEdge);
					sEdge.setTarget(this);
					this.getEdges().add(sEdge);
					i--;
				}
			}
		}

		// reset the topology
		this.graphManager.resetAllNodes();
		this.graphManager.resetAllNodesToApplyGravitation();
		this.graphManager.resetAllEdges();
	}

	/**
	 * This method returns the neighbor edges EXCEPT RIGID EDGE
	 */
	public ArrayList<SbgnPDEdge> getEdgeList()
	{
		ArrayList<SbgnPDEdge> edgeList = new ArrayList<SbgnPDEdge>();

		for (Object o : this.getEdges())
		{
			if (!((SbgnPDEdge) o).type.equals(SbgnPDConstants.RIGID_EDGE))
			{
				edgeList.add((SbgnPDEdge) o);
			}
		}

		return edgeList;
	}

	// ---------------------------------- The following methods are not used. ----------------------------------
	
	/**
	 * This method calculates the relativity forces for each node in the graph
	 * by examining the production and consumption neighbors.
	 */
	public void calcRelativityForce()
	{
		ArrayList<SbgnPDNode> nodeList = new ArrayList<SbgnPDNode>();
		Point targetPoint = new Point();

		// find the neighbor nodes except the parent(connected with a rigid
		// edge)
		for (Object o : this.getEdges())
		{
			SbgnPDEdge edge = (SbgnPDEdge) o;
			if (!(edge.type.equals(SbgnPDConstants.RIGID_EDGE)))
			{
				if (this.type.equals(SbgnPDConstants.PRODUCTION_PORT))
					nodeList.add((SbgnPDNode) edge.getTarget());
				else if (this.type.equals(SbgnPDConstants.CONSUMPTION_PORT))
					nodeList.add((SbgnPDNode) edge.getSource());
			}
		}

		// calculate target point
		targetPoint = calcTargetPoint();

		// apply the forces to all effected nodes
		applyRelativity(nodeList, targetPoint);
	}

	/**
	 * This methods applies relativity forces to members of the given list
	 * towards the given point
	 */
	private void applyRelativity(ArrayList<SbgnPDNode> nodeList,
			Point targetPoint)
	{
		SbgnPDLayout layout = (SbgnPDLayout) this.graphManager.getLayout();
		double multiplier = SbgnPDConstants.RELATIVITY_CONSTRAINT_CONSTANT
				* layout.coolingFactor;

		for (SbgnPDNode s : nodeList)
		{
			s.relativityConstraintX = (targetPoint.x - s.getCenterX())
					* multiplier;
			s.relativityConstraintY = (targetPoint.y - s.getCenterY())
					* multiplier;
		}
	}

	/**
	 * This method calculates a target point to move the elements of given list.
	 * The target pt is in the middle of the given node list. The returned point
	 * should have a distance of default edge length (origin is the process
	 * node)
	 */
	private Point calcTargetPoint()
	{
		int differenceX = (int) (this.getCenterX() - this.parent.getCenterX());
		int differenceY = (int) (this.getCenterY() - this.parent.getCenterY());

		return new Point(
				(int) (Math.signum(differenceX)
						* FDLayoutConstants.DEFAULT_EDGE_LENGTH + this.parent
						.getCenterX()),
				(int) (Math.signum(differenceY)
						* FDLayoutConstants.DEFAULT_EDGE_LENGTH + this.parent
						.getCenterY()));
	}

}
