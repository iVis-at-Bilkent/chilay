package org.ivis.layout.cise;

/**
 * This class implements a pair of on-circle nodes used for swapping in phase 4.
 *
 * @author Esat Belviranli
 */
public class CiSEOnCircleNodePair implements Comparable<CiSEOnCircleNodePair>
{
// -----------------------------------------------------------------------------
// Section: Instance variables
// -----------------------------------------------------------------------------
	/*
	 * The node of the pair which comes first in the ordering of its owner
	 * circle.
	 */
	private CiSENode firstNode;

	/*
	 * The node of the pair which comes second in the ordering of its owner
	 * circle.
	 */
	private CiSENode secondNode;

	/*
	 * The value indicating the swapping potential of the two nodes above.
	 * Higher value means that nodes are more inclined to swap.
	 */
	private double displacement;

	/*
	 * The iteration number where this swapping takes place.
	 */
	private int iterationNo;

// -----------------------------------------------------------------------------
// Section: Constructors and initializations
// -----------------------------------------------------------------------------
	/**
	 * Constructor
	 */
	public CiSEOnCircleNodePair(CiSENode first,
		CiSENode second,
		double displacement,
		int iterationNo)
	{
		assert first.getOnCircleNodeExt() != null &&
			second.getOnCircleNodeExt() != null;

		this.firstNode = first;
		this.secondNode = second;
		this.displacement = displacement;
		this.iterationNo = iterationNo;
	}

// -----------------------------------------------------------------------------
// Section: Accessors
// -----------------------------------------------------------------------------
	public double getDisplacement()
	{
		return this.displacement;
	}

	public CiSENode getFirstNode()
	{
		return this.firstNode;
	}

	public CiSENode getSecondNode()
	{
		return this.secondNode;
	}

	public int getIterationNo()
	{
		return this.iterationNo;
	}

// -----------------------------------------------------------------------------
// Section: Remaining methods
// -----------------------------------------------------------------------------
	public int compareTo(CiSEOnCircleNodePair other)
	{
		return (int)(this.getDisplacement() - other.getDisplacement());
	}

	public void swap()
	{
		this.getFirstNode().getOnCircleNodeExt().swapWith(
			this.getSecondNode().getOnCircleNodeExt());
	}

	public boolean equals(Object other)
	{
		boolean result = other instanceof CiSEOnCircleNodePair;

		if (result)
		{
			CiSEOnCircleNodePair pair = (CiSEOnCircleNodePair) other;

			result &= (this.firstNode.equals(pair.getFirstNode()) &&
				this.secondNode.equals(pair.getSecondNode())) ||
				(this.secondNode.equals(pair.getFirstNode()) &&
						this.firstNode.equals(pair.getSecondNode()));
		}

		return result;
	}

	public int hashCode()
	{
		return this.firstNode.hashCode() + this.secondNode.hashCode();
	}

	public String toString()
	{
		String result = "Swap: " + this.getFirstNode().vGraphObject;
		result += " - "+ this.getSecondNode().vGraphObject;
		result +=", "+ this.iterationNo;
		result +=", "+ this.getDisplacement();

		return result;
	}
}