package org.ivis.layout.fd;

import java.util.HashSet;
import java.util.Vector;

import org.ivis.util.*;
import org.ivis.layout.*;

/**
 * This class implements common data and functionality for all layout styles
 * that are force-directed.
 *
 * @author: Ugur Dogrusoz
 *
 * Copyright: i-Vis Research Group, Bilkent University, 2007 - present
 */
public abstract class FDLayout extends Layout
{
// -----------------------------------------------------------------------------
// Section: Instance variables
// -----------------------------------------------------------------------------
	/**
	 * Ideal length of an edge
	 */
	protected double idealEdgeLength = (double) FDLayoutConstants.DEFAULT_EDGE_LENGTH;

	/**
	 * Constant for calculating spring forces
	 */
	protected double springConstant = FDLayoutConstants.DEFAULT_SPRING_STRENGTH;

	/**
	 * Constant for calculating repulsion forces
	 */
	protected double repulsionConstant =
		FDLayoutConstants.DEFAULT_REPULSION_STRENGTH;

	/**
	 * Constants for calculating gravitation forces
	 */
	protected double gravityConstant = FDLayoutConstants.DEFAULT_GRAVITY_STRENGTH;
	protected double compoundGravityConstant =
		FDLayoutConstants.DEFAULT_COMPOUND_GRAVITY_STRENGTH;

	/**
	 * Factors to determine the ranges within which gravity is not to be applied
	 */
	protected double gravityRangeFactor =
		FDLayoutConstants.DEFAULT_GRAVITY_RANGE_FACTOR;
	protected double compoundGravityRangeFactor =
		FDLayoutConstants.DEFAULT_COMPOUND_GRAVITY_RANGE_FACTOR;

	/**
	 * Threshold for convergence per node
	 */
	public double displacementThresholdPerNode =
		(3.0 * (double) FDLayoutConstants.DEFAULT_EDGE_LENGTH) / 100;

	/**
	 * Whether or not FR grid variant should be used for repulsion force calculations.
	 */
	protected boolean useFRGridVariant = 
		FDLayoutConstants.DEFAULT_USE_SMART_REPULSION_RANGE_CALCULATION;
	
	/**
	 * Factor used for cooling layout; starts from 1.0 and goes down towards
	 * zero as we approach maximum iterations. Incremental layout might start
	 * with a smaller value.
	 */
	public double coolingFactor = 1.0;
	public double initialCoolingFactor = 1.0;
	
	/**
	 * Total displacement made in this iteration
	 */
	public double totalDisplacement = 0.0;

	/**
	 * Total displacement made in the previous iteration
	 */
	public double oldTotalDisplacement = 0.0;

	/**
	 * Maximum number of layout iterations allowed
	 */
	protected int maxIterations = 2500;

	/**
	 * Total number of iterations currently performed
	 */
	protected int totalIterations;

	/**
	 * Number of layout iterations that has not been animated (rendered)
	 */
	protected int notAnimatedIterations;

	/**
	 * Threshold for convergence (calculated according to graph to be laid out)
	 */
	public double totalDisplacementThreshold;

	/**
	 * Maximum node displacement in allowed in one iteration
	 */
	public double maxNodeDisplacement;
	
	/**
	 * Repulsion range & edge size of a grid
	 */
	protected double repulsionRange;
	
	/**
	 * Screen is divided into grid of squares.
	 * At each iteration, each node is placed in its grid square(s)
	 * Grid is re-calculated after every tenth iteration.
	 */
	protected Vector[][] grid;
	
// -----------------------------------------------------------------------------
// Section: Constructors and initialization
// -----------------------------------------------------------------------------
	/**
	 * The constructor creates and associates with this layout a new graph
	 * manager as well.
	 */
	public FDLayout()
	{
		super();
	}

	/**
	 * This method is used to set all layout parameters to default values.
	 */
	public void initParameters()
	{
		super.initParameters();

		LayoutOptionsPack.CoSE layoutOptionsPack =
			LayoutOptionsPack.getInstance().getCoSE();
		
		if (this.layoutQuality == LayoutConstants.DRAFT_QUALITY)
		{
			this.displacementThresholdPerNode += 0.30;
			this.maxIterations *= 0.8;
		}
		else if (this.layoutQuality == LayoutConstants.PROOF_QUALITY)
		{
			this.displacementThresholdPerNode -= 0.30;
			this.maxIterations *= 1.2;
		}

		this.totalIterations = 0;
		this.notAnimatedIterations = 0;
		
		this.useFRGridVariant = layoutOptionsPack.isSmartRepulsionRangeCalc();
	}

// -----------------------------------------------------------------------------
// Section: Remaining methods
// -----------------------------------------------------------------------------
	/**
	 * This method is used to set parameters used by spring embedder.
	 */
	public void initSpringEmbedder()
	{
		if (this.incremental)
		{
			this.coolingFactor = 0.8;
			this.initialCoolingFactor = 0.8;
			this.maxNodeDisplacement = 
				FDLayoutConstants.MAX_NODE_DISPLACEMENT_INCREMENTAL;
		}
		else
		{
			this.coolingFactor = 1.0;
			this.initialCoolingFactor = 1.0;
			this.maxNodeDisplacement =
				FDLayoutConstants.MAX_NODE_DISPLACEMENT;
		}

		this.maxIterations =
			Math.max(this.getAllNodes().length * 5, this.maxIterations);

		this.totalDisplacementThreshold =
			this.displacementThresholdPerNode * this.getAllNodes().length;
		
		this.repulsionRange = this.calcRepulsionRange();
	}

	/**
	 * This method calculates the spring forces for the ends of each node.
	 */
	public void calcSpringForces()
	{
		Object[] lEdges = this.getAllEdges();
		FDLayoutEdge edge;

		for (int i = 0; i < lEdges.length; i++)
		{
			edge = (FDLayoutEdge) lEdges[i];

			this.calcSpringForce(edge, edge.idealLength);
		}
	}

	/**
	 * This method calculates the repulsion forces for each pair of nodes.
	 */
	public void calcRepulsionForces()
	{
		int i, j;
		FDLayoutNode nodeA, nodeB;
		Object[] lNodes = this.getAllNodes();
		HashSet<FDLayoutNode> processedNodeSet;
		
		if (this.useFRGridVariant)
		{
			// grid is a vector matrix that holds CoSENodes.
			// be sure to convert the Object type to CoSENode.
			
			if (this.totalIterations % FDLayoutConstants.GRID_CALCULATION_CHECK_PERIOD == 1)
			{
				this.grid = this.calcGrid(this.graphManager.getRoot());
				
				// put all nodes to proper grid cells
				for (i = 0; i < lNodes.length; i++)
				{
					nodeA = (FDLayoutNode) lNodes[i];
					this.addNodeToGrid(nodeA, this.grid, 
						this.graphManager.getRoot().getLeft(),
						this.graphManager.getRoot().getTop());
				}
			}
			
			processedNodeSet = new HashSet<FDLayoutNode>();
			
			// calculate repulsion forces between each nodes and its surrounding
			for (i = 0; i < lNodes.length; i++)
			{
				nodeA = (FDLayoutNode) lNodes[i];
				this.calculateRepulsionForceOfANode(this.grid, nodeA, processedNodeSet);
				processedNodeSet.add(nodeA);
			}			
		}
		else
		{
			for (i = 0; i < lNodes.length; i++)
			{
				nodeA = (FDLayoutNode) lNodes[i];

				for (j = i + 1; j < lNodes.length; j++)
				{
					nodeB = (FDLayoutNode) lNodes[j];

					// If both nodes are not members of the same graph, skip.
					if (nodeA.getOwner() != nodeB.getOwner())
					{
						continue;
					}

					this.calcRepulsionForce(nodeA, nodeB);
				}
			}
		}
	}

	/**
	 * This method calculates gravitational forces to keep components together.
	 */
	public void calcGravitationalForces()
	{
		FDLayoutNode node;
		Object[] lNodes = this.getAllNodesToApplyGravitation();

		for (int i = 0; i < lNodes.length; i++)
		{
			node = (FDLayoutNode) lNodes[i];

			this.calcGravitationalForce(node);
		}
	}

	/**
	 * This method updates positions of each node at the end of an iteration.
	 */
	public void moveNodes()
	{
		Object[] lNodes = this.getAllNodes();
		FDLayoutNode node;

		for (int i = 0; i < lNodes.length; i++)
		{
			node = (FDLayoutNode) lNodes[i];
			node.move();
		}
	}

	/**
	 * This method calculates the spring force for the ends of input edge based
	 * on the input ideal length.
	 */
	protected void calcSpringForce(LEdge edge, double idealLength)
	{
		FDLayoutNode sourceNode = (FDLayoutNode) edge.getSource();
		FDLayoutNode targetNode = (FDLayoutNode) edge.getTarget();
		double length;
		double springForce;
		double springForceX;
		double springForceY;

		// Update edge length

		if (this.uniformLeafNodeSizes &&
			sourceNode.getChild() == null && targetNode.getChild() == null)
		{
			edge.updateLengthSimple();
		}
		else
		{
			edge.updateLength();

			if (edge.isOverlapingSourceAndTarget())
			{
				return;
			}
		}

		length = edge.getLength();

		// Calculate spring forces
		springForce = this.springConstant * (length - idealLength);

	//			// does not seem to be needed
	//			if (Math.abs(springForce) > CoSEConstants.MAX_SPRING_FORCE)
	//			{
	//				springForce = IMath.sign(springForce) * CoSEConstants.MAX_SPRING_FORCE;
	//			}

		// Project force onto x and y axes
		springForceX = springForce * (edge.getLengthX() / length);
		springForceY = springForce * (edge.getLengthY() / length);

		//TODO: remove this dependency!
	//			NodeModel vSourceNode = (NodeModel)sourceNode.vGraphObject;
	//			NodeModel vTargetNode = (NodeModel)targetNode.vGraphObject;
	//
	//			if (vSourceNode.getText().equals("1") || vTargetNode.getText().equals("1"))
	//			{
	//				System.out.printf("\t%s-%s\n",
	//					new Object [] {vSourceNode.getText(), vTargetNode.getText()});
	//			}

		// Apply forces on the end nodes
		sourceNode.springForceX += springForceX;
		sourceNode.springForceY += springForceY;
		targetNode.springForceX -= springForceX;
		targetNode.springForceY -= springForceY;
	}

	/**
	 * This method calculates the repulsion forces for the input node pair.
	 */
	protected void calcRepulsionForce(FDLayoutNode nodeA, FDLayoutNode nodeB)
	{
		RectangleD rectA = nodeA.getRect();
		RectangleD rectB = nodeB.getRect();
		double[] overlapAmount = new double[2];
		double[] clipPoints = new double[4];
		double distanceX;
		double distanceY;
		double distanceSquared;
		double distance;
		double repulsionForce;
		double repulsionForceX;
		double repulsionForceY;

		if (rectA.intersects(rectB))
		// two nodes overlap
		{
			// calculate overlap amount in x and y directions
			IGeometry.calcSmallerIntersection(rectA, rectB, overlapAmount);

			if (Math.abs(overlapAmount[0]) < Math.abs(overlapAmount[1]))
			// should repell in x direction to avoid overlap
			{
				if (rectA.x < rectB.x)
				{
					repulsionForceX = (IMath.sign(overlapAmount[0]) *
						FDLayoutConstants.DEFAULT_EDGE_LENGTH +
						overlapAmount[0]) / 2;
				}
				else
				{
					repulsionForceX = -(IMath.sign(overlapAmount[0]) *
						FDLayoutConstants.DEFAULT_EDGE_LENGTH +
						overlapAmount[0]) / 2;
				}

				repulsionForceY = 0.0;
			}
			else
			// should repell in y direction to avoid overlap
			{
				repulsionForceX = 0.0;

				if (rectA.y < rectB.y)
				{
					repulsionForceY = (IMath.sign(overlapAmount[1]) *
						FDLayoutConstants.DEFAULT_EDGE_LENGTH +
						overlapAmount[1]) / 2;
				}
				else
				{
					repulsionForceY = -(IMath.sign(overlapAmount[1]) *
						FDLayoutConstants.DEFAULT_EDGE_LENGTH +
						overlapAmount[1]) / 2;
				}
			}

//					System.out.printf("\toverlap=(%5.1f,%5.1f)\n",
//						new Object [] {overlapAmount[0], overlapAmount[1]});
		}
		else
		// no overlap
		{
			// calculate distance

			if (this.uniformLeafNodeSizes &&
				nodeA.getChild() == null && nodeB.getChild() == null)
			// simply base repulsion on distance of node centers
			{
				distanceX = rectB.getCenterX() - rectA.getCenterX();
				distanceY = rectB.getCenterY() - rectA.getCenterY();
			}
			else
			// use clipping points
			{
				IGeometry.getIntersection(rectA, rectB, clipPoints);

				distanceX = clipPoints[2] - clipPoints[0];
				distanceY = clipPoints[3] - clipPoints[1];
			}

			//TODO: No repulsion range. The grid variant should take care of this...

			if (Math.abs(distanceX) < FDLayoutConstants.MIN_REPULSION_DIST)
			{
				distanceX = IMath.sign(distanceX) *
					FDLayoutConstants.MIN_REPULSION_DIST;
			}

			if (Math.abs(distanceY) < FDLayoutConstants.MIN_REPULSION_DIST)
			{
				distanceY = IMath.sign(distanceY) *
					FDLayoutConstants.MIN_REPULSION_DIST;
			}

			distanceSquared = distanceX * distanceX + distanceY * distanceY;
			distance = Math.sqrt(distanceSquared);

			repulsionForce = this.repulsionConstant / distanceSquared;

//					// does not seem to be needed
//						if (Math.abs(repulsionForce) > CoSEConstants.MAX_REPULSION_FORCE)
//						{
//							repulsionForce = IMath.sign(repulsionForce) * CoSEConstants.MAX_REPULSION_FORCE;
//						}

			// Project force onto x and y axes
			repulsionForceX = repulsionForce * distanceX / distance;
			repulsionForceY = repulsionForce * distanceY / distance;
		}

		// Apply forces on the two nodes
		nodeA.repulsionForceX -= repulsionForceX;
		nodeA.repulsionForceY -= repulsionForceY;
		nodeB.repulsionForceX += repulsionForceX;
		nodeB.repulsionForceY += repulsionForceY;
	}

	/**
	 * This method calculates gravitational force for the input node.
	 */
	protected void calcGravitationalForce(FDLayoutNode node)
	{
		assert node.gravitationForceX == 0 && node.gravitationForceY == 0;

		LGraph ownerGraph;
		double ownerCenterX;
		double ownerCenterY;
		double distanceX;
		double distanceY;
		double absDistanceX;
		double absDistanceY;
		int estimatedSize;
		ownerGraph = node.getOwner();

		ownerCenterX = (ownerGraph.getRight() + ownerGraph.getLeft()) / 2;
		ownerCenterY = (ownerGraph.getTop() + ownerGraph.getBottom()) / 2;
		distanceX = node.getCenterX() - ownerCenterX;
		distanceY = node.getCenterY() - ownerCenterY;
		absDistanceX = Math.abs(distanceX);
		absDistanceY = Math.abs(distanceY);

		// Apply gravitation only if the node is "roughly" outside the
		// bounds of the initial estimate for the bounding rect of the owner
		// graph. We relax (not as much for the compounds) the estimated
		// size here since the initial estimates seem to be rather "tight".

		if (node.getOwner() == this.graphManager.getRoot())
		// in the root graph
		{
			estimatedSize = (int) (ownerGraph.getEstimatedSize() *
				this.gravityRangeFactor);

			if (absDistanceX > estimatedSize || absDistanceY > estimatedSize)
			{
				node.gravitationForceX = -this.gravityConstant * distanceX;
				node.gravitationForceY = -this.gravityConstant * distanceY;
			}
		}
		else
		// inside a compound
		{
			estimatedSize = (int) (ownerGraph.getEstimatedSize() *
				this.compoundGravityRangeFactor);

			if (absDistanceX > estimatedSize || absDistanceY > estimatedSize)
			{
				node.gravitationForceX = -this.gravityConstant * distanceX *
					this.compoundGravityConstant;
				node.gravitationForceY = -this.gravityConstant * distanceY *
					this.compoundGravityConstant;
			}
		}

//			System.out.printf("\tgravitation=(%5.1f,%5.1f)\n",
//				new Object [] {node.gravitationForceX, node.gravitationForceY});
	}

	/**
	 * This method inspects whether the graph has reached to a minima. It
	 * returns true if the layout seems to be oscillating as well.
	 */
	protected boolean isConverged()
	{
		boolean converged;
		boolean oscilating = false;

		if (this.totalIterations > this.maxIterations / 3)
		{
			oscilating =
				Math.abs(this.totalDisplacement - this.oldTotalDisplacement) < 2;
		}

		converged = this.totalDisplacement < this.totalDisplacementThreshold;

		this.oldTotalDisplacement = this.totalDisplacement;

		return converged || oscilating;
	}

	/**
	 * This method updates the v-level compound graph coordinates and refreshes
	 * the display if corresponding flag is on.
	 */
	protected void animate()
	{
		if (this.animationDuringLayout && !this.isSubLayout)
		{
			if (this.notAnimatedIterations == this.animationPeriod)
			{
				this.update();

				this.notAnimatedIterations = 0;
			}
			else
			{
				this.notAnimatedIterations++;
			}
		}
	}
	
// -----------------------------------------------------------------------------
// Section: FR-Grid Variant Repulsion Force Calculation
// -----------------------------------------------------------------------------
	/**
	 * This method creates the empty grid with proper dimensions
	 */
	private Vector[][] calcGrid(LGraph g)
	{
		int i, j;
		Vector[][] grid;
		
		int sizeX = 0;
		int sizeY = 0;
		
		sizeX = (int) Math.ceil((g.getRight() - g.getLeft()) / this.repulsionRange);
		sizeY = (int) Math.ceil((g.getBottom() - g.getTop()) / this.repulsionRange);

		grid = new Vector[sizeX][sizeY];
		
		for (i = 0; i < sizeX; i++)
		{
			for (j = 0; j < sizeY; j++)
			{
				grid[i][j] = new Vector();
			}
		}
		return grid;
	}
	
	/**
	 * This method adds input node v to the proper grid squares, 
	 * and also sets the grid start and finish points of v 
	 */
	private void addNodeToGrid(FDLayoutNode v, 
		Vector[][] grid, 
		double left, 
		double top)
	{
		int startX = 0;
		int finishX = 0;
		int startY = 0;
		int finishY = 0;
		
		startX = (int) Math.floor((v.getRect().x - left) / this.repulsionRange);
		finishX = (int) Math.floor((v.getRect().width + v.getRect().x - left) / this.repulsionRange);
		startY = (int) Math.floor((v.getRect().y - top) / this.repulsionRange);
		finishY = (int) Math.floor((v.getRect().height + v.getRect().y - top) / this.repulsionRange);
		
		for (int i = startX; i <= finishX; i++)
		{
			for (int j = startY; j <= finishY; j++)
			{
				grid[i][j].add(v);
				v.setGridCoordinates(startX, finishX, startY, finishY); 
			}
		}
	}
	
	/**
	 * This method finds surrounding nodes of nodeA in repulsion range.
	 * And calculates the repulsion forces between nodeA and its surrounding.
	 * During the calculation, ignores the nodes that have already been processed.
	 */
	private void calculateRepulsionForceOfANode (Vector[][] grid, 
		FDLayoutNode nodeA,
		HashSet<FDLayoutNode> processedNodeSet)
	{
		int i,j;
		
		if (this.totalIterations % FDLayoutConstants.GRID_CALCULATION_CHECK_PERIOD == 1)
		{
			HashSet<Object> surrounding = new HashSet<Object>();
			FDLayoutNode nodeB;
			
			for (i = (nodeA.startX-1); i < (nodeA.finishX+2); i++)
			{
				for (j = (nodeA.startY-1); j < (nodeA.finishY+2); j++)
				{
					if (!((i < 0) || (j < 0) || (i >= grid.length) || (j >= grid[0].length)))
					{
						for (Object obj : grid[i][j])
						{ 
							nodeB = (FDLayoutNode) obj;
							
							// If both nodes are not members of the same graph, 
							// or both nodes are the same, skip.
							if ((nodeA.getOwner() != nodeB.getOwner()) 
								|| (nodeA == nodeB))
							{
								continue;
							}
							
							// check if the repulsion force between 
							// nodeA and nodeB has already been calculated
							if (!processedNodeSet.contains(nodeB) && !surrounding.contains(nodeB))
							{	
								double distanceX = Math.abs(nodeA.getCenterX()-nodeB.getCenterX()) - 
									((nodeA.getWidth()/2) + (nodeB.getWidth()/2));
								double distanceY = Math.abs(nodeA.getCenterY()-nodeB.getCenterY()) - 
									((nodeA.getHeight()/2) + (nodeB.getHeight()/2));
								
								// if the distance between nodeA and nodeB 
								// is less then calculation range
								if ((distanceX <= this.repulsionRange) && (distanceY <= this.repulsionRange))
								{
									//then add nodeB to surrounding of nodeA
									surrounding.add(nodeB);
								}
							}
						}
					}
				}
			}
			nodeA.surrounding = surrounding.toArray();
		}

		for (i = 0; i < nodeA.surrounding.length; i++)
		{
			this.calcRepulsionForce(nodeA, (FDLayoutNode) nodeA.surrounding[i]);
		}		
		
	}
	/**
	 * This method calculates repulsion range
	 * Also it can be used to calculate the height of a grid's edge
	 */
	protected double calcRepulsionRange()
	{
		return 0.0;
	}
}