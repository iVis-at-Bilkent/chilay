package org.ivis.layout.cise;

import java.util.*;

import org.ivis.layout.*;
import org.ivis.util.IGeometry;
import org.ivis.util.PointD;

/**
 * This class implements data and functionality required for CiSE layout per
 * cluster.
 *
 * @author Esat Belviranli
 * @author Alptug Dilek
 * @author Ugur Dogrusoz
 *
 * Copyright: i-Vis Research Group, Bilkent University, 2007 - present
 */
public class CiSECircle extends LGraph
{
// -----------------------------------------------------------------------------
// Section: Instance variables
// -----------------------------------------------------------------------------
	/**
	 * Holds the intracluster edges of this circle, initially it is null. It
	 * will be calculated and stored when getIntraClusterEdges method is first
	 * called.
	 */
	private List<CiSEEdge> intraClusterEdges = null;

	/**
	 * Holds the nodes which don't have neighbors outside this circle
	 */
	private Set<CiSENode> inNodes;

	/**
	 * Holds the nodes which have neighbors outside this circle
	 */
	private Set<CiSENode> outNodes;

	/**
	 * Holds the nodes which are on the circle
	 */
	private List<CiSENode> onCircleNodes;
	
	/**
	 * Holds the nodes which are inside the circle
	 */
	private List<CiSENode> inCircleNodes;

	/**
	 * The radius of this circle, calculated with respect to the dimensions of
	 * the nodes on this circle and node separation options
	 */
	private double radius;

// -----------------------------------------------------------------------------
// Section: Constructors and initializations
// -----------------------------------------------------------------------------
	/**
	 * Constructor
	 */
	public CiSECircle(LNode parent, LGraphManager graphMgr, Object vNode)
	{
		super(parent, graphMgr, vNode);

		this.inNodes = new HashSet<CiSENode>();
		this.outNodes = new HashSet<CiSENode>();
		this.onCircleNodes = new ArrayList<CiSENode>();
		this.inCircleNodes = new ArrayList<CiSENode>();
	}

// -----------------------------------------------------------------------------
// Section: Accessors and mutators
// -----------------------------------------------------------------------------
	/**
	 * This method downcasts and returns the child at given index.
	 */
	public CiSENode getChildAt(int index)
	{
		return (CiSENode)this.getOnCircleNodes().get(index);
	}

	/**
	 * This method is overridden to add new nodes also to on-circle nodes list
	 *  as well
	 */
	public LNode add(LNode newNode)
	{
		this.onCircleNodes.add((CiSENode)newNode);
		return super.add(newNode);
	}
	
	/**
	 * This method rotates this circle by iterating over and adjusting the
	 * relative positioning of all nodes on this circle by the calculated angle
	 * with respect to the rotation amount of the owner node.
	 */
	public void rotate()
	{
		// Take size into account when reflecting total force into rotation!
		CiSENode parentNode = (CiSENode)this.getParent();
		int noOfNodes = this.getOnCircleNodes().size();
		double rotationAmount = parentNode.rotationAmount / noOfNodes;
		CiSENode onCircleNode;
		CiSEOnCircleNodeExt onCircleNodeExt;
		CiSELayout layout = (CiSELayout)(this.getGraphManager().getLayout());

		if (rotationAmount != 0.0)
		{
			double teta = rotationAmount / this.radius;

			if (teta > CiSEConstants.MAX_ROTATION_ANGLE)
			{
				teta = CiSEConstants.MAX_ROTATION_ANGLE;
			}
			else if (teta < CiSEConstants.MIN_ROTATION_ANGLE)
			{
				teta = CiSEConstants.MIN_ROTATION_ANGLE;
			}

			for (int i = 0; i < noOfNodes ; i++)
			{
				onCircleNode = this.getChildAt(i);
				onCircleNodeExt = onCircleNode.getOnCircleNodeExt();

				onCircleNodeExt.setAngle(onCircleNodeExt.getAngle() + teta);
				onCircleNodeExt.updatePosition();
			}

			layout.totalDisplacement += parentNode.rotationAmount;
			parentNode.rotationAmount = 0.0;
		}
	}

	/**
	 * This method returns the radius of this circle.
	 */
	public double getRadius()
	{
		return this.radius;
	}

	/**
	 * This method returns nodes that don't have neighbors outside this circle.
	 */
	public Set<CiSENode> getInNodes()
	{
		return this.inNodes;
	}

	/**
	 * This method returns nodes that have neighbors outside this circle.
	 */
	public Set<CiSENode> getOutNodes()
	{
		return this.outNodes;
	}

	/**
	 * This method returns nodes that don't have neighbors outside this circle.
	 */
	public List<CiSENode> getOnCircleNodes()
	{
		return this.onCircleNodes;
	}

	/**
	 * This method returns nodes that don't have neighbors outside this circle.
	 */
	public List<CiSENode> getInCircleNodes()
	{
		return this.inCircleNodes;
	}

	/**
	 * This metho sets the radius of this circle.
	 */
	public void setRadius(double radius)
	{
		this.radius = radius;
	}

	/**
	 * This method calculates and sets dimensions of the parent node of this
	 * circle. Parent node is centered to be at the same location of the
	 * associated circle but its dimensions are larger than the circle by a
	 * factor (must be >= 1 to ensure all nodes are enclosed within its
	 * rectangle) of the largest dimension (width or height) of on-circle nodes
	 * so that it completely encapsulates the nodes on this circle.
	 */
	public void calculateParentNodeDimension()
	{
		assert this.getOnCircleNodes().size() != 0;

		double maxOnCircleNodeDimension = Integer.MIN_VALUE;
		Iterator iterator = this.getOnCircleNodes().iterator();

		while (iterator.hasNext())
		{
			LNode node = (LNode)iterator.next();

			if (node.getWidth() > maxOnCircleNodeDimension)
			{
				maxOnCircleNodeDimension = node.getWidth();
			}

			if (node.getHeight() > maxOnCircleNodeDimension)
			{
				maxOnCircleNodeDimension = node.getHeight();
			}
		}

		double dimension = 2.0 * this.radius + maxOnCircleNodeDimension;
		LNode parentNode = this.getParent();
		parentNode.setHeight(dimension);
		parentNode.setWidth(dimension);
	}

// -----------------------------------------------------------------------------
// Section: Other methods
// -----------------------------------------------------------------------------
	/**
	 * This method calculates and returns rotational and translational parts of
	 * the total force calculated for the given node. The translational part is
	 * composed of components in x and y directions.
	 */
	public CircularForce decomposeForce(CiSENode node)
	{
		assert node.getOwner() == this : "The node must belong to this circle";

		CircularForce circularForce;

		if (node.displacementX != 0.0 || node.displacementY != 0.0)
		{
			LNode ownerNode = this.getParent();
			double Cx = ownerNode.getCenterX();
			double Cy = ownerNode.getCenterY();
			double Nx = node.getCenterX();
			double Ny = node.getCenterY();
			double Fx = node.displacementX;
			double Fy = node.displacementY;

			double C_angle = IGeometry.angleOfVector(Cx, Cy, Nx, Ny);
			double F_angle = IGeometry.angleOfVector(0.0, 0.0, Fx, Fy);
			double C_rev_angle = C_angle + Math.PI;

			assert Math.PI <= C_rev_angle && C_rev_angle <= IGeometry.THREE_PI;

			// Check whether F lies between C and its opposite angle C-reverse;
			// if so, rotation is +ve (clockwise); otherwise, it's -ve.
			// We handle angles greater than 360 specially in the else part.
			boolean isRotationClockwise;

			if (Math.PI <= C_rev_angle && C_rev_angle < IGeometry.TWO_PI)
			{
				if (C_angle <= F_angle && F_angle < C_rev_angle)
				{
					isRotationClockwise = true;
				}
				else
				{
					isRotationClockwise = false;
				}
			}
			else
			{
				C_rev_angle -= IGeometry.TWO_PI;

				assert 0.0 <= C_rev_angle && C_rev_angle <= Math.PI;

				if (C_rev_angle <= F_angle && F_angle < C_angle)
				{
					isRotationClockwise = false;
				}
				else
				{
					isRotationClockwise = true;
				}
			}

			double angle_diff = Math.abs(C_angle - F_angle);
			double F_magnitude = Math.sqrt(Fx * Fx + Fy * Fy);
			double R_magnitude = Math.abs(Math.sin(angle_diff) * F_magnitude);

			if (!isRotationClockwise)
			{
				R_magnitude = -R_magnitude;
			}

			circularForce = new CircularForce(R_magnitude, Fx, Fy);
		}
		else
		{
			circularForce = new CircularForce(0.0, 0.0, 0.0);
		}

		return circularForce;
	}

	/**
	 * This method swaps the nodes given as parameter and make necessary angle
	 * and positioning updates.
	 */
	public void swapNodes(CiSENode first, CiSENode second)
	{
		assert first.getOwner() == this && second.getOwner() == this;

		// Determine which node has smaller index.

		CiSENode smallIndexNode = first;
		CiSENode bigIndexNode = second;
		CiSEOnCircleNodeExt firstExt = first.getOnCircleNodeExt();
		CiSEOnCircleNodeExt secondExt = second.getOnCircleNodeExt();

		if (smallIndexNode.getOnCircleNodeExt().getIndex() >
			second.getOnCircleNodeExt().getIndex())
		{
			smallIndexNode = second;
			bigIndexNode = first;
		}

		// Check the exceptional case where the small index node is at 0 index
		// and the big index node is at the last index of the circle. In this
		// case, we treat smaller index node as bigger index node and vice versa

		if (smallIndexNode.getOnCircleNodeExt().getPrevNode() == bigIndexNode)
		{
			CiSENode tempNode = bigIndexNode;
			bigIndexNode = smallIndexNode;
			smallIndexNode = tempNode;
		}

		CiSEOnCircleNodeExt smallIndexNodeExt = smallIndexNode.getOnCircleNodeExt();
		CiSEOnCircleNodeExt bigIndexNodeExt = bigIndexNode.getOnCircleNodeExt();

		// Calculate the angle for the big index node

		CiSENode smallIndexPrevNode = smallIndexNodeExt.getPrevNode();

		CiSELayout layout = (CiSELayout)(this.getGraphManager().getLayout());
		int nodeSeparation = layout.getNodeSeparation();

		double angle = (smallIndexPrevNode.getOnCircleNodeExt().getAngle() +
			(smallIndexPrevNode.getHalfTheDiagonal() +
			bigIndexNode.getHalfTheDiagonal() +
			nodeSeparation) / this.radius) % (2 * Math.PI);

		bigIndexNodeExt.setAngle(angle);

		// Calculate the angle for the small index node

		angle = (bigIndexNodeExt.getAngle() +
			(bigIndexNode.getHalfTheDiagonal() +
			smallIndexNode.getHalfTheDiagonal() +
			nodeSeparation) / this.radius) % (2 * Math.PI);

		smallIndexNodeExt.setAngle(angle);

		smallIndexNodeExt.updatePosition();
		bigIndexNodeExt.updatePosition();

		int tempIndex = firstExt.getIndex();
		firstExt.setIndex(secondExt.getIndex());
		secondExt.setIndex(tempIndex);
		this.getOnCircleNodes().set(firstExt.getIndex(), first);
		this.getOnCircleNodes().set(secondExt.getIndex(), second);

		firstExt.updateSwappingConditions();
		secondExt.updateSwappingConditions();

		if (firstExt.getNextNode() == second)
		{
			firstExt.getPrevNode().getOnCircleNodeExt().
				updateSwappingConditions();
			secondExt.getNextNode().getOnCircleNodeExt().
				updateSwappingConditions();
		}
		else
		{
			firstExt.getNextNode().getOnCircleNodeExt().
				updateSwappingConditions();
			secondExt.getPrevNode().getOnCircleNodeExt().
				updateSwappingConditions();
		}
	}

	/**
	 * This method returns the intra cluster edges of this circle
	 */
	public List<CiSEEdge> getIntraClusterEdges()
	{
		if (this.intraClusterEdges == null)
		{
			this.intraClusterEdges = new ArrayList<CiSEEdge>();
			Iterator<CiSEEdge> iterator = this.getEdges().iterator();

			while (iterator.hasNext())
			{
				CiSEEdge edge = iterator.next();

				if (edge.isIntraCluster)
				{
					this.intraClusterEdges.add(edge);
				}
			}
		}

		return this.intraClusterEdges;
	}

	/*
	 * This method calculates the total number of inter-cluster edge crossings
	 * of this cluster (i.e. sum of the inter-graph edge crossing number of the
	 * out nodes of this cluster) when the ordering as is and as it is reversed.
	 * We reverse the order of the nodes if we get a lower crossing number in
	 * the reverse ordering. It returns true if reverse order was adapted.
	 */
	public boolean checkAndReverseIfReverseIsBetter()
	{
		// First find all the inter-graph edges coming out of this cluster

		HashSet<CiSEEdge> interClusterEdges = new HashSet<CiSEEdge>();
		Iterator<CiSENode> nodeIterator = this.outNodes.iterator();
		CiSENode node;
		CiSEOnCircleNodeExt nodeExt;

		while (nodeIterator.hasNext())
		{
			node = nodeIterator.next();
			nodeExt = node.getOnCircleNodeExt();
			interClusterEdges.addAll(nodeExt.getInterClusterEdges());
		}

		// Update edge lengths of these inter-graph edges
		this.updateLengths(interClusterEdges);

		// Calculate crossing number before reversing
		int crossingNumberBefore = this.calcCrossingNumber(interClusterEdges);

		// Now reverse the order
		this.reverseNodes();

		// Re-update edge lengths of inter-graph edges
		this.updateLengths(interClusterEdges);

		// Re-calculate crossing number after reversing
		int crossingNumberAfter = this.calcCrossingNumber(interClusterEdges);

		// Check if reversing helped; otherwise revert back to original ordering

		if (crossingNumberBefore < crossingNumberAfter)
		{
			this.reverseNodes();
			return false;
		}
		else
		{
			System.out.println(this.getNodes().size() + " node cluster reversed!");
			return true;
		}
	}

	/*
	 * This method calculates crossing number of the edges in the input list by
	 * checking whether or not they intersect pairwise.
	 */
	private int calcCrossingNumber(Set edgeSet)
	{
		// First convert the set to a list since sets do not guarentee order
		List edgeList = new ArrayList(edgeSet);

		int count = 0;
		Iterator<CiSEEdge> iter1 = edgeList.iterator();
		CiSEEdge edge1, edge2;
		PointD point1, point2, point3, point4;
		int i = 0;
		int j;

		while (iter1.hasNext())
		{
			edge1 = iter1.next();
			i++;

			Iterator<CiSEEdge> iter2 = edgeList.iterator();
			j = 0;

			while (iter2.hasNext())
			{
				edge2 = iter2.next();
				j++;

				if (i < j)
				// Make sure we do not redundantly check intersections
				{
					point1 = edge1.getSource().getCenter();
					point2 = edge1.getTarget().getCenter();
					point3 = edge2.getSource().getCenter();
					point4 = edge2.getTarget().getCenter();

					if (IGeometry.doIntersect(point1, point2, point3, point4))
					{
						count++;
					}
				}
			}
		}

		return count;
	}

	/*
	 * This method updates the lengths of edges in the input edge list.
	 */
	private void updateLengths(Set edges)
	{
		Iterator<CiSEEdge> edgeIterator = edges.iterator();
		CiSEEdge edge;

		while (edgeIterator.hasNext())
		{
			edge = edgeIterator.next();
			edge.updateLength();
		}
	}

	/*
	 * This method reverses the nodes on this circle.
	 */
	protected void reverseNodes()
	{
		Iterator iterator = this.getOnCircleNodes().iterator();
		int noOfNodesOnCircle = this.getOnCircleNodes().size();
		CiSENode node;
		CiSEOnCircleNodeExt nodeExt;

		while (iterator.hasNext())
		{
			node = (CiSENode)iterator.next();
			nodeExt = node.getOnCircleNodeExt();

			nodeExt.setIndex((noOfNodesOnCircle - nodeExt.getIndex()) %
				noOfNodesOnCircle);
		}

		this.reCalculateNodeAnglesAndPositions();
	}

	/**
	 * This method removes given on-circle node from the circle and calls 
	 * reCalculateCircleSizeAndRadius and  reCalculateNodeAnglesAndPositions.
	 * This method should be called when an inner node is found and to be moved
	 * inside the circle.
	 * @param node
	 */
	public void moveOnCircleNodeInside(CiSENode node)
	{
		assert (node.getOnCircleNodeExt() != null);

		// Remove the node from on-circle nodes list and add it to in-circle 
		// nodes list
		this.onCircleNodes.remove(node);
		this.inCircleNodes.add(node);
		
		// Re-adjust all order indexes of remaining on circle nodes.
		for (int i=0; i < this.onCircleNodes.size(); i++)
		{
			CiSENode onCircleNode = this.onCircleNodes.get(i);
			
			onCircleNode.getOnCircleNodeExt().setIndex(i);
		}
		
		// De-register extension
		node.setAsNonOnCircleNode();
		
		// calculateRadius
		this.reCalculateCircleSizeAndRadius();
		
		//calculateNodePositions
		this.reCalculateNodeAnglesAndPositions();
		
		node.setCenter(this.getParent().getCenterX(),
			this.getParent().getCenterY());
	}

	/**
	 * This method calculates the size and radius of this circle with respect 
	 * to the sizes of the vertices and the node separation parameter.
	 */
	public void reCalculateCircleSizeAndRadius()
	{
		double totalDiagonal = 0;
		Iterator iterator = this.getOnCircleNodes().iterator();
		double temp;

		while (iterator.hasNext())
		{
			LNode node = (LNode)iterator.next();

			temp = node.getWidth() * node.getWidth() +
				node.getHeight() * node.getHeight();
			totalDiagonal += Math.sqrt(temp);
		}

		CiSELayout layout = (CiSELayout)(this.getGraphManager().getLayout());
		int nodeSeparation = layout.getNodeSeparation();

		double perimeter = totalDiagonal + 
			this.getOnCircleNodes().size() * nodeSeparation;
		this.radius = perimeter / (2 * Math.PI);
		this.getParent().setWidth(2 * this.radius);
		this.getParent().setHeight(2 * this.radius);
		
		this.calculateParentNodeDimension();
	}
	
	/**
	 * This method goes over all on-circle nodes and re-calculates their angles
	 * and corresponding positions. This method should be called when on-circle
	 * nodes (content or order) have been changed for this circle.
	 */
	public void reCalculateNodeAnglesAndPositions()
	{
		CiSELayout layout = (CiSELayout)(this.getGraphManager().getLayout());
		int nodeSeparation = layout.getNodeSeparation();

		// It is important that we sort these on-circle nodes in place.
		List inOrderCopy = this.onCircleNodes;
		new CiSENodeSort(inOrderCopy).quicksort();

		double parentCenterX = this.getParent().getCenterX();
		double parentCenterY = this.getParent().getCenterY();

		for (int i = 0; i < inOrderCopy.size(); i++)
		{
			CiSENode node = (CiSENode) inOrderCopy.get(i);
			Double angle;

			if (i == 0)
			{
				angle = 0.0;
			}
			else
			{
				CiSENode previousNode = (CiSENode) inOrderCopy.get(i - 1);
				
				// => angle in radian = (2*PI)*(circular distance/(2*PI*r))
				angle = previousNode.getOnCircleNodeExt().getAngle() +
					(node.getHalfTheDiagonal() + nodeSeparation +
						previousNode.getHalfTheDiagonal()) /
					this.radius;
			}

			node.getOnCircleNodeExt().setAngle(angle);
			node.setCenter(parentCenterX + this.radius * Math.cos(angle),
				parentCenterY +	this.radius * Math.sin(angle));
		}
	}

	/*
	 * This auxiliary main method just test circular force decomposition. 
	 */
	public static void main(String[] args)
	{
		System.out.println(IGeometry.radian2degree(IGeometry.angleOfVector(0.0, 0.0, 5.0, 10.0)));
		System.out.println(IGeometry.radian2degree(IGeometry.angleOfVector(0.0, 0.0, 5.0, -10.0)));
		System.out.println(IGeometry.radian2degree(IGeometry.angleOfVector(0.0, 0.0, -5.0, 10.0)));
		System.out.println(IGeometry.radian2degree(IGeometry.angleOfVector(0.0, 0.0, -5.0, -10.0)));

		/*TODO: needs to be re-written with the new interface! 
		Layout layout = new CiSELayout();
		LGraphManager gm = layout.getGraphManager();
		LGraph g1 = gm.addRoot();

		CiSENode parentNode = new CiSENode(gm, "");
		CiSECircle circle = new CiSECircle(parentNode, gm, "");
		CiSEOnCircleNode node = new CiSEOnCircleNode(gm, "");
		circle.setRadius(5.0);
		node.setCenter(-Math.sqrt(24), 3.0);
		parentNode.setCenter(0.0, 4.0);
		node.displacementX = -Math.sqrt(24);
		node.displacementY = -1.0;
		ArrayList nodes = new ArrayList();
		nodes.add(node);
		node.setOwner(circle);
		circle.setNodes(nodes);

		CircularForce force = circle.decomposeForce(node);

		System.out.printf("circular force: r=%5.1f \t x=%5.1f \t y=%5.1f\n",
			new Object [] {
				force.getRotationAmount(),
				force.getDisplacementX(),
				force.getDisplacementY()});
		*/
	}
}