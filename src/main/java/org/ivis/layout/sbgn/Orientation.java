package org.ivis.layout.sbgn;

import java.awt.Point;
import java.util.ArrayList;

import org.ivis.layout.LNode;

/**
 * This class finds the best orientation of two port nodes of a process node.
 * The orientations can be: S-P, P-S, S|P, P|S where -:horizontal, |:vertical,
 * S:substrate port node, P:product port node
 * 
 * @author Begum Genc
 * 
 */
public class Orientation
{
	private SbgnPortNode productionPort;
	private SbgnPortNode consumptionPort;
	private PlacementOfProductionPort bestPlacement;

	private PlacementOfProductionPort[] options = {
			PlacementOfProductionPort.TOP, PlacementOfProductionPort.BOTTOM,
			PlacementOfProductionPort.LEFT, PlacementOfProductionPort.RIGHT };

	/**
	 * Incident edges of the production port node
	 */
	private ArrayList<SbgnPDEdge> productionEdgeList;
	/**
	 * Incident edges of the consumption port node
	 */
	private ArrayList<SbgnPDEdge> consumptionEdgeList;

	public Orientation(SbgnPortNode productionPort, SbgnPortNode consumptionPort)
	{
		this.productionPort = productionPort;
		this.consumptionPort = consumptionPort;
		this.productionEdgeList = productionPort.getEdgeList();
		this.consumptionEdgeList = consumptionPort.getEdgeList();
	}

	/**
	 * This method checks if the constraints can be satisfied for any of the
	 * placement choices (vertical/horizontal). If the constraints can not be
	 * satisfied, the position which yields in the minimum total edge length is
	 * chosen as the best orientation.
	 */
	public void findBestOrientation(SbgnPortNode prodPort, SbgnPortNode consPort)
	{
		productionPort = prodPort;
		consumptionPort = consPort;

		int minTotalEdgeLength = Integer.MAX_VALUE;
		int stepSum = 0;

		// check if the constraints can be satisfied
		PlacementOfProductionPort result = canConstraintsBeSatisfied();
		if (result != null)
		{
			bestPlacement = result;
		}
		else
		{
			// find the orientation that gives min total edge length
			for (PlacementOfProductionPort option : options)
			{
				stepSum = calculateTotalLengthForPosition(option);

				if (stepSum < minTotalEdgeLength)
				{
					minTotalEdgeLength = stepSum;
					bestPlacement = option;
				}
			}
		}
	}

	/**
	 * This methods checks if any orientation option can be satisfied. (If all
	 * product nodes fall on one side of the product port and all consumption
	 * nodes fall on the other side of the consumption node)
	 */
	private PlacementOfProductionPort canConstraintsBeSatisfied()
	{
		boolean satisfied = true;

		ArrayList<SbgnPDNode> productionNodes = getNodesFromEdges(productionPort);
		ArrayList<SbgnPDNode> consumptionNodes = getNodesFromEdges(consumptionPort);

		// for each orientation option, check satisfiability
		for (PlacementOfProductionPort option : options)
		{
			satisfied = checkNodes(option, productionNodes, consumptionNodes);

			if (satisfied)
				return option;
		}

		return null;
	}

	/**
	 * This method checks if all nodes respect to the given placement option
	 */
	private boolean checkNodes(PlacementOfProductionPort option,
			ArrayList<SbgnPDNode> productionNodes,
			ArrayList<SbgnPDNode> consumptionNodes)
	{

		// check if the constraints can be satisfied for all product nodes
		for (SbgnPDNode s : productionNodes)
		{
			if (checkProductConstraint(option, s))
				return false;
		}

		// check if the constraints can be satisfied for all consumption nodes
		for (SbgnPDNode s : consumptionNodes)
		{
			if (checkConsumptionConstraint(option, s))
				return false;
		}

		return true;
	}

	private boolean checkProductConstraint(PlacementOfProductionPort option,
			SbgnPDNode s)
	{
		if (option.equals(PlacementOfProductionPort.BOTTOM)
				&& s.getCenterY() > consumptionPort.parent.getCenterY())
			return true;
		else if (option.equals(PlacementOfProductionPort.TOP)
				&& s.getCenterY() > productionPort.parent.getCenterY())
			return true;
		else if (option.equals(PlacementOfProductionPort.LEFT)
				&& s.getCenterX() > productionPort.parent.getCenterX())
			return true;
		else if (option.equals(PlacementOfProductionPort.RIGHT)
				&& s.getCenterX() > consumptionPort.parent.getCenterX())
			return true;
		else
			return false;
	}

	private boolean checkConsumptionConstraint(
			PlacementOfProductionPort option, SbgnPDNode s)
	{
		if (option.equals(PlacementOfProductionPort.BOTTOM)
				&& s.getCenterY() < productionPort.parent.getCenterY())
			return true;
		else if (option.equals(PlacementOfProductionPort.TOP)
				&& s.getCenterY() < consumptionPort.parent.getCenterY())
			return true;
		else if (option.equals(PlacementOfProductionPort.LEFT)
				&& s.getCenterX() < consumptionPort.parent.getCenterX())
			return true;
		else if (option.equals(PlacementOfProductionPort.RIGHT)
				&& s.getCenterX() < productionPort.parent.getCenterX())
			return true;
		else
			return false;
	}

	/**
	 * This method sums the incident edges' lengths for a given port node
	 * positioning. (Q: If the port nodes are oriented as the given value, what
	 * will be the total length of their incident edges?)
	 */
	private int calculateTotalLengthForPosition(
			PlacementOfProductionPort placement)
	{
		int prodPortXCoef = 0;
		int prodPortYCoef = 0;
		int stepSum = 0;

		prodPortXCoef = getXCoefficient(placement);
		prodPortYCoef = getYCoefficient(placement);

		for (SbgnPDEdge s : productionEdgeList)
			stepSum += (calculateLength(s.getTarget(), new Point(
					(int) (productionPort.parent.getCenterX() + prodPortXCoef
							* SbgnPDConstants.RIGID_EDGE_LENGTH),
					(int) (productionPort.parent.getCenterY() + prodPortYCoef
							* SbgnPDConstants.RIGID_EDGE_LENGTH))));

		// consumption port has opposite direction to the production port
		for (SbgnPDEdge s : consumptionEdgeList)
			stepSum += calculateLength(s.getTarget(),
					new Point(
							(int) (consumptionPort.parent.getCenterX() + (-1)
									* prodPortXCoef
									* SbgnPDConstants.RIGID_EDGE_LENGTH),
							(int) (consumptionPort.parent.getCenterY() + (-1)
									* prodPortYCoef
									* SbgnPDConstants.RIGID_EDGE_LENGTH)));
		return stepSum;
	}

	/**
	 * This method calculates the distance between the center point of the given node and a given point.
	 */
	private double calculateLength(LNode target, Point point)
	{
		double result;

		result = Math.sqrt(Math.pow((target.getCenterX() - point.x), 2)
				+ Math.pow((target.getCenterY() - point.y), 2));

		return result;
	}

	/**
	 * This method returns the neighbor nodes of a given port node. All neighbor
	 * edges except rigid edge are visited.
	 */
	ArrayList<SbgnPDNode> getNodesFromEdges(SbgnPortNode s)
	{
		ArrayList<SbgnPDNode> nodeList = new ArrayList<SbgnPDNode>();

		// notice that the edge list has been initialized in the constructor.
		if (s.type.equals(SbgnPDConstants.PRODUCTION_PORT))
		{
			for (SbgnPDEdge edge : productionEdgeList)
			{
				nodeList.add((SbgnPDNode) edge.getOtherEnd(s));
			}
		}
		else if (s.type.equals(SbgnPDConstants.CONSUMPTION_PORT))
		{
			for (SbgnPDEdge edge : consumptionEdgeList)
			{
				nodeList.add((SbgnPDNode) edge.getOtherEnd(s));
			}
		}
		return nodeList;
	}

	/**
	 * This method returns a sign (+1, -1 or 0) relative to the parent's x
	 * position. For example, if the placement is TOP, it means that they have
	 * the same x value, (parent.x = 10 parent.y = 10, port.x = 10 port.y = 5)
	 * so 0 returns.
	 */
	private int getXCoefficient(PlacementOfProductionPort placement)
	{
		int coef = 0;

		if (placement.equals(PlacementOfProductionPort.TOP)
				|| placement.equals(PlacementOfProductionPort.BOTTOM))
			coef = 0;
		else if (placement.equals(PlacementOfProductionPort.LEFT))
			coef = -1;
		else if (placement.equals(PlacementOfProductionPort.RIGHT))
			coef = 1;

		return coef;
	}

	/**
	 * This method returns a sign (+1, -1 or 0) relative to the parent's y
	 * position. For example, if the placement is TOP, it means that they have
	 * different y values, (parent.x = 10 parent.y = 10, port.x = 10 port.y = 5)
	 * so -1 returns.
	 */
	private int getYCoefficient(PlacementOfProductionPort placement)
	{
		int coef = 0;

		if (placement.equals(PlacementOfProductionPort.TOP))
			coef = -1;
		else if (placement.equals(PlacementOfProductionPort.BOTTOM))
			coef = 1;
		else if (placement.equals(PlacementOfProductionPort.LEFT)
				|| placement.equals(PlacementOfProductionPort.RIGHT))
			coef = 0;

		return coef;
	}

	public enum PlacementOfProductionPort
	{
		BOTTOM, LEFT, RIGHT, TOP
	}

	/**
	 * This methods sets the placement of the production node and returns the updated node
	 */
	public SbgnPortNode getLocatedProductionNode()
	{
		productionPort.setCenter(productionPort.parent.getCenterX()
				+ getXCoefficient(bestPlacement)
				* SbgnPDConstants.RIGID_EDGE_LENGTH,
				productionPort.parent.getCenterY()
						+ getYCoefficient(bestPlacement)
						* SbgnPDConstants.RIGID_EDGE_LENGTH);

		return productionPort;
	}
	
	/**
	 * This methods sets the placement of the consumption node and returns the updated node
	 */
	public SbgnPortNode getLocatedConsumptionNode()
	{
		consumptionPort.setCenter(productionPort.parent.getCenterX() + (-1)
				* getXCoefficient(bestPlacement)
				* SbgnPDConstants.RIGID_EDGE_LENGTH,
				consumptionPort.parent.getCenterY() + (-1)
						* getYCoefficient(bestPlacement)
						* SbgnPDConstants.RIGID_EDGE_LENGTH);

		return consumptionPort;
	}
}
