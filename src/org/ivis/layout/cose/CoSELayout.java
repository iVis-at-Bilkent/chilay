package org.ivis.layout.cose;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.awt.*;

import org.ivis.layout.*;
import org.ivis.layout.fd.*;
import org.ivis.util.*;

/**TODO:
 * use of randomness to beat local minima
 * grid-variant
 * parallelization
 */

/**
 * This class implements the overall layout process for the CoSE algorithm.
 * Details of this algorithm can be found in the following article:
 * 		U. Dogrusoz, E. Giral, A. Cetintas, A. Civril, and E. Demir,
 * 		"A Layout Algorithm For Undirected Compound Graphs",
 * 		Information Sciences, 179, pp. 980�994, 2009.
 *
 * @author Ugur Dogrusoz
 * @author Erhan Giral
 * @author Cihan Kucukkececi
 * @author Alper Karacelik
 * 
 * Copyright: i-Vis Research Group, Bilkent University, 2007 - present
 */
public class CoSELayout extends FDLayout
{
// -----------------------------------------------------------------------------
// Section: Instance variables
// -----------------------------------------------------------------------------
	/**
	 * Whether or not smart calculation of ideal edge lengths should be
	 * performed. When true, ideal edge length values take sizes of end nodes
	 * into account as well as depths of end nodes (how many levels of compounds
	 * does each edge need to go through from source to target, if any).
	 */
	protected boolean useSmartIdealEdgeLengthCalculation =
		CoSEConstants.DEFAULT_USE_SMART_IDEAL_EDGE_LENGTH_CALCULATION;

	/**
	 * Whether or not multi-level scaling should be used to speed up layout
	 */
	protected boolean useMultiLevelScaling =
		CoSEConstants.DEFAULT_USE_MULTI_LEVEL_SCALING;
	
	/**
	 * Level of the current graph manager in the coarsening process
	 */
	private int level;
	
	/**
	 * Total level number
	 */
	private int noOfLevels;
	
	/**
	 * Holds all graph managers (M0 to Mk)
	 */
	ArrayList<CoSEGraphManager> MList;

// -----------------------------------------------------------------------------
// Section: Constructors and initializations
// -----------------------------------------------------------------------------
	/**
	 * The constructor creates and associates with this layout a new graph
	 * manager as well.
	 */
	public CoSELayout()
	{
		super();
	}

	/**
	 * This method creates a new graph manager associated with this layout.
	 */
	protected LGraphManager newGraphManager()
	{
		LGraphManager gm = new CoSEGraphManager(this);
		this.graphManager = gm;
		return gm;
	}
	
	/**
	 * This method creates a new graph associated with the input view graph.
	 */
	public LGraph newGraph(Object vGraph)
	{
		return new CoSEGraph(null, this.graphManager, vGraph);
	}

	/**
	 * This method creates a new node associated with the input view node.
	 */
	public LNode newNode(Object vNode)
	{
		return new CoSENode(this.graphManager, vNode);
	}

	/**
	 * This method creates a new edge associated with the input view edge.
	 */
	public LEdge newEdge(Object vEdge)
	{
		return new CoSEEdge(null, null, vEdge);
	}

	/**
	 * This method is used to set all layout parameters to default values.
	 */
	public void initParameters()
	{
		super.initParameters();

		if (!this.isSubLayout)
		{
			LayoutOptionsPack.CoSE layoutOptionsPack =
				LayoutOptionsPack.getInstance().getCoSE();

			if (layoutOptionsPack.getIdealEdgeLength() < 10)
			{
				this.idealEdgeLength = 10;
			}
			else
			{
				this.idealEdgeLength = layoutOptionsPack.getIdealEdgeLength();
			}

			this.useSmartIdealEdgeLengthCalculation =
				layoutOptionsPack.isSmartEdgeLengthCalc();
			this.useMultiLevelScaling =
				layoutOptionsPack.isMultiLevelScaling();
			this.springConstant =
				transform(layoutOptionsPack.getSpringStrength(),
					FDLayoutConstants.DEFAULT_SPRING_STRENGTH, 5.0, 5.0);
			this.repulsionConstant =
				transform(layoutOptionsPack.getRepulsionStrength(),
					FDLayoutConstants.DEFAULT_REPULSION_STRENGTH, 5.0, 5.0);
			this.gravityConstant =
				transform(layoutOptionsPack.getGravityStrength(),
					FDLayoutConstants.DEFAULT_GRAVITY_STRENGTH);
			this.compoundGravityConstant =
				transform(layoutOptionsPack.getCompoundGravityStrength(),
					FDLayoutConstants.DEFAULT_COMPOUND_GRAVITY_STRENGTH);
			this.gravityRangeFactor =
				transform(layoutOptionsPack.getGravityRange(),
					FDLayoutConstants.DEFAULT_GRAVITY_RANGE_FACTOR);
			this.compoundGravityRangeFactor =
				transform(layoutOptionsPack.getCompoundGravityRange(),
					FDLayoutConstants.DEFAULT_COMPOUND_GRAVITY_RANGE_FACTOR);
		}
	}
		
// -----------------------------------------------------------------------------
// Section: Layout!
// -----------------------------------------------------------------------------
	/**
	 * This method performs layout on constructed l-level graph. It returns true
	 * on success, false otherwise.
	 */
	public boolean layout()
	{
		boolean createBendsAsNeeded = LayoutOptionsPack.getInstance().
		getGeneral().isCreateBendsAsNeeded();

		if (createBendsAsNeeded)
		{
			this.createBendpoints();
			
			// reset edge list, since the topology has changed
			this.graphManager.resetAllEdges();
		}
		
		if (this.useMultiLevelScaling && !this.incremental)
		{
			return this.multiLevelScalingLayout();
		}
		else
		{
			this.level = 0;
			return this.classicLayout();
		}
	}

	/**
	 * This method applies multi-level scaling during layout
	 */
	private boolean multiLevelScalingLayout ()
	{
		CoSEGraphManager gm = (CoSEGraphManager)this.graphManager;

		//TODO: Remove comments after testing
		//GraphMLWriter graphMLWriter;
		
		// Start coarsening process
		
		// save graph managers M0 to Mk in an array list
		this.MList = gm.coarsenGraph();

		this.noOfLevels = MList.size()-1;
		this.level = this.noOfLevels;
		
		while (this.level >= 0)
		{
			this.graphManager = gm = this.MList.get(this.level);
			
			// TODO: remove after testing
			//this.transform();
			//graphMLWriter = new GraphMLWriter("D:\\workspace\\research_work\\chied2x\\graphs\\rome\\level_" + 
			//	this.level + "_(before).graphml");
			//graphMLWriter.saveGraph(this.graphManager);
			
			System.out.print("@" + this.level + "th level, with " + gm.getRoot().getNodes().size() + " nodes. ");
			this.classicLayout();

			// TODO: remove after testing
			//this.transform();
			//graphMLWriter = new GraphMLWriter("D:\\workspace\\research_work\\chied2x\\graphs\\rome\\level_" + 
			//	this.level + "_(after).graphml");
			//graphMLWriter.saveGraph(this.graphManager);
			
			// after finishing layout of first (coarsest) level,
			this.incremental = true;
			
			if (this.level >= 1) 
			{	
				this.uncoarsen(); // also makes initial placement for Mi-1
			}
			
			// reset total iterations
			this.totalIterations = 0;
			
			this.level--;
		}
		
		this.incremental = false;
		return true;
	}
	
	/**
	 * This method uses classic layout method (without multi-scaling)
	 * @return
	 */
	private boolean classicLayout ()
	{
		this.calculateNodesToApplyGravitationTo();
	
		this.graphManager.calcLowestCommonAncestors();
		this.graphManager.calcInclusionTreeDepths();
	
		this.graphManager.getRoot().calcEstimatedSize();
		this.calcIdealEdgeLengths();
	
		if (!this.incremental)
		{
			ArrayList<ArrayList<LNode>> forest = this.getFlatForest();
	
			if (forest.size() > 0)
			// The graph associated with this layout is flat and a forest
			{
				this.positionNodesRadially(forest);
			}
			else
			// The graph associated with this layout is not flat or a forest
			{
				this.positionNodesRandomly();
			}
		}
	
		this.initSpringEmbedder();
		this.runSpringEmbedder();
	
		System.out.println("Classic CoSE layout finished after " +
			this.totalIterations + " iterations");
		
		return true;
	}
	/**
	 * This method performs the actual layout on the l-level compound graph. An
	 * update() needs to be called for changes to be propogated to the v-level
	 * compound graph.
	 */
	public void runSpringEmbedder()
	{
//		if (!this.incremental)
//		{
//			CoSELayout.randomizedMovementCount = 0;
//			CoSELayout.nonRandomizedMovementCount = 0;
//		}

//		this.updateAnnealingProbability();

		do
		{
			this.totalIterations++;

			if (this.totalIterations % FDLayoutConstants.CONVERGENCE_CHECK_PERIOD == 0)
			{
				if (this.isConverged())
				{
					break;
				}

				this.coolingFactor = this.initialCoolingFactor *
					((this.maxIterations - this.totalIterations) / (double)this.maxIterations);
				
//				this.updateAnnealingProbability();
			}

			this.totalDisplacement = 0;

			this.graphManager.updateBounds();
			this.calcSpringForces();
			this.calcRepulsionForces();
			this.calcGravitationalForces();
			this.moveNodes();

			this.animate();
		}
		while (this.totalIterations < this.maxIterations);
	}

	/**
	 * This method finds and forms a list of nodes for which gravitation should
	 * be applied. For connected graphs (root graph or compounds / child graphs)
	 * there is no need to apply gravitation. While doing so, each graph in the
	 * associated graph manager is marked as connected or not.
	 */
	public void calculateNodesToApplyGravitationTo()
	{
		LinkedList nodeList = new LinkedList();
		LGraph graph;

		for (Object obj : this.graphManager.getGraphs())
		{
			graph = (LGraph) obj;

			graph.updateConnected();

			if (!graph.isConnected())
			{
				nodeList.addAll(graph.getNodes());
			}
		}

		this.graphManager.setAllNodesToApplyGravitation(nodeList);

//		// Use this to apply the idea for flat graphs only
//		if (this.graphManager.getGraphs().size() == 1)
//		{
//			LGraph root = this.graphManager.getRoot();
//			assert this.graphManager.getGraphs().get(0) == root;
//
//			root.updateConnected();
//
//			if (!root.isConnected())
//			{
//				this.graphManager.setAllNodesToApplyGravitation(
//					this.graphManager.getAllNodes());
//			}
//			else
//			{
//				this.graphManager.setAllNodesToApplyGravitation(new LinkedList());
//			}
//		}
//		else
//		{
//			this.graphManager.setAllNodesToApplyGravitation(
//				this.graphManager.getAllNodes());
//		}
	}

	/**
	 * This method creates bendpoints multi-edges which are incident to same  
	 * source and target nodes, and for all edges that have the same node as 
	 * both source and target.
	 */
	private void createBendpoints()
	{
		List edges = new ArrayList();
		edges.addAll(Arrays.asList(this.graphManager.getAllEdges()));
		Set visited = new HashSet();

		for (int i = 0; i < edges.size(); i++)
		{
			LEdge edge = (LEdge) edges.get(i);

			if (!visited.contains(edge))
			{
				LNode source = edge.getSource();
				LNode target = edge.getTarget();

				if (source == target)
				{
					edge.getBendpoints().add(new PointD());
					edge.getBendpoints().add(new PointD());
					this.createDummyNodesForBendpoints(edge);
					visited.add(edge);
				}
				else
				{
					List edgeList = new ArrayList();
					
					edgeList.addAll(source.getEdgeListToNode(target));
					edgeList.addAll(target.getEdgeListToNode(source));

					if (!visited.contains(edgeList.get(0)))
					{
						if (edgeList.size() > 1)
						{
							for(int k = 0; k < edgeList.size(); k++)
							{
								LEdge multiEdge = (LEdge)edgeList.get(k);
								multiEdge.getBendpoints().add(new PointD());
								this.createDummyNodesForBendpoints(multiEdge);
							}
						}

						visited.addAll(edgeList);
					}
				}
			}

			if (visited.size() == edges.size())
			{
				break;
			}
		}
	}
	
	/**
	 * This method calculates the ideal edge length of each edge based on the
	 * depth and dimension of the ancestor nodes in the lowest common ancestor
	 * graph of the edge's end nodes. We assume depth and dimension of each node
	 * has already been calculated.
	 */
	protected void calcIdealEdgeLengths()
	{
		CoSEEdge edge;
		int lcaDepth;
		LNode source;
		LNode target;
		int sizeOfSourceInLca;
		int sizeOfTargetInLca;

		for (Object obj : this.graphManager.getAllEdges())
		{
			edge = (CoSEEdge) obj;

			edge.idealLength = this.idealEdgeLength;

			if (edge.isInterGraph())
			{
				source = edge.getSource();
				target = edge.getTarget();

				sizeOfSourceInLca = edge.getSourceInLca().getEstimatedSize();
				sizeOfTargetInLca = edge.getTargetInLca().getEstimatedSize();

				if (this.useSmartIdealEdgeLengthCalculation)
				{
					edge.idealLength +=	sizeOfSourceInLca + sizeOfTargetInLca -
						2 * LayoutConstants.SIMPLE_NODE_SIZE;
				}

				lcaDepth = edge.getLca().getInclusionTreeDepth();

				edge.idealLength += FDLayoutConstants.DEFAULT_EDGE_LENGTH *
					FDLayoutConstants.PER_LEVEL_IDEAL_EDGE_LENGTH_FACTOR *
						(source.getInclusionTreeDepth() +
							target.getInclusionTreeDepth() - 2 * lcaDepth);
			}

//			NodeModel vSourceNode = (NodeModel)(edge.getSource().vGraphObject);
//			NodeModel vTargetNode = (NodeModel)(edge.getTarget().vGraphObject);
//
//			System.out.printf("\t%s-%s: %5.1f\n",
//				new Object [] {vSourceNode.getText(), vTargetNode.getText(), edge.idealLength});
		}
	}

	/**
	 * This method performs initial positioning of given forest radially. The
	 * final drawing should be centered at the gravitational center.
	 */
	protected void positionNodesRadially(ArrayList<ArrayList<LNode>> forest)
	{
		// We tile the trees to a grid row by row; first tree starts at (0,0)
		Point currentStartingPoint = new Point(0, 0);
		int numberOfColumns = (int) Math.ceil(Math.sqrt(forest.size()));
		int height = 0;
		int currentY = 0;
		int currentX = 0;
		PointD point = new PointD(0, 0);

		for (int i = 0; i < forest.size(); i++)
		{
			if (i % numberOfColumns == 0)
			{
				// Start of a new row, make the x coordinate 0, increment the
				// y coordinate with the max height of the previous row
				currentX = 0;
				currentY = height;

				if (i !=0)
				{
					currentY += CoSEConstants.DEFAULT_COMPONENT_SEPERATION;
				}

				height = 0;
			}

			ArrayList<LNode> tree = forest.get(i);

			// Find the center of the tree
			LNode centerNode = Layout.findCenterOfTree(tree);

			// Set the staring point of the next tree
			currentStartingPoint.x = currentX;
			currentStartingPoint.y = currentY;

			// Do a radial layout starting with the center
			point =
				CoSELayout.radialLayout(tree, centerNode, currentStartingPoint);

			if (point.y > height)
			{
				height = (int) point.y;
			}

			currentX = (int)
				(point.x + CoSEConstants.DEFAULT_COMPONENT_SEPERATION);
		}

		this.transform(
			new PointD(LayoutConstants.WORLD_CENTER_X - point.x / 2,
				LayoutConstants.WORLD_CENTER_Y - point.y / 2));
	}

	/**
	 * This method positions given nodes according to a simple radial layout
	 * starting from the center node. The top-left of the final drawing is to be
	 * at given location. It returns the bottom-right of the bounding rectangle
	 * of the resulting tree drawing.
	 */
	private static PointD radialLayout(ArrayList<LNode> tree,
		LNode centerNode,
		Point startingPoint)
	{
		double radialSep = Math.max(maxDiagonalInTree(tree),
			CoSEConstants.DEFAULT_RADIAL_SEPARATION);
		CoSELayout.branchRadialLayout(centerNode, null, 0, 359, 0, radialSep);
		Rectangle bounds = LGraph.calculateBounds(tree);

		Transform transform = new Transform();
		transform.setDeviceOrgX(bounds.getMinX());
		transform.setDeviceOrgY(bounds.getMinY());
		transform.setWorldOrgX(startingPoint.x);
		transform.setWorldOrgY(startingPoint.y);

		for (int i = 0; i < tree.size(); i++)
		{
			LNode node = tree.get(i);
			node.transform(transform);
		}

		PointD bottomRight =
			new PointD(bounds.getMaxX(), bounds.getMaxY());

		return transform.inverseTransformPoint(bottomRight);
	}

	/**
	 * This method is recursively called for radial positioning of a node,
	 * between the specified angles. Current radial level is implied by the
	 * distance given. Parent of this node in the tree is also needed.
	 */
	private static void branchRadialLayout(LNode node,
		LNode parentOfNode,
		double startAngle, double endAngle,
		double distance, double radialSeparation)
	{
		// First, position this node by finding its angle.
		double halfInterval = ((endAngle - startAngle) + 1) / 2;

		if (halfInterval < 0)
		{
			halfInterval += 180;
		}

		double nodeAngle = (halfInterval + startAngle) % 360;
		double teta = (nodeAngle * IGeometry.TWO_PI) / 360;

		// Make polar to java cordinate conversion.
		double x = distance * Math.cos(teta);
		double y = distance * Math.sin(teta);

		node.setCenter(x, y);

		// Traverse all neighbors of this node and recursively call this
		// function.

		List<LEdge> neighborEdges = new LinkedList<LEdge>(node.getEdges());
		int childCount = neighborEdges.size();

		if (parentOfNode != null)
		{
			childCount--;
		}

		int branchCount = 0;

		int incEdgesCount = neighborEdges.size();
		int startIndex;

		List edges = node.getEdgesBetween(parentOfNode);

		// If there are multiple edges, prune them until there remains only one
		// edge.
		while (edges.size() > 1)
		{
			neighborEdges.remove(edges.remove(0));
			incEdgesCount--;
			childCount--;
		}

		if (parentOfNode != null)
		{
			assert edges.size() == 1;
			startIndex =
				(neighborEdges.indexOf(edges.get(0)) + 1) % incEdgesCount;
		}
		else
		{
			startIndex = 0;
		}

		double stepAngle = Math.abs(endAngle - startAngle) / childCount;

		for (int i = startIndex;
			branchCount != childCount ;
			i = (++i) % incEdgesCount)
		{
			LNode currentNeighbor =
				neighborEdges.get(i).getOtherEnd(node);

			// Don't back traverse to root node in current tree.
			if (currentNeighbor == parentOfNode)
			{
				continue;
			}

			double childStartAngle =
				(startAngle + branchCount * stepAngle) % 360;
			double childEndAngle = (childStartAngle + stepAngle) % 360;

			branchRadialLayout(currentNeighbor,
					node,
					childStartAngle, childEndAngle,
					distance + radialSeparation, radialSeparation);

			branchCount++;
		}
	}

	/**
	 * This method finds the maximum diagonal length of the nodes in given tree.
	 */
	private static double maxDiagonalInTree(ArrayList<LNode> tree)
	{
		double maxDiagonal = Double.MIN_VALUE;

		for (int i = 0; i < tree.size(); i++)
		{
			LNode node = tree.get(i);
			double diagonal = node.getDiagonal();

			if (diagonal > maxDiagonal)
			{
				maxDiagonal = diagonal;
			}
		}

		return maxDiagonal;
	}

	// -----------------------------------------------------------------------------
	// Section: Multi-level Scaling
	// -----------------------------------------------------------------------------
	
	/**
	 * This method un-coarsens Mi to Mi-1 and makes initial placement for Mi-1
	 */
	public void uncoarsen ()
	{
		for (Object obj: this.graphManager.getAllNodes())
		{
			CoSENode v = (CoSENode) obj;
			// set positions of v.pred1 and v.pred2
			v.getPred1().setLocation(v.getLeft(), v.getTop());
			
			if (v.getPred2() != null)
			{
				// TODO: check 
				/*
				double w = v.getPred1().getRect().width;
				double l = this.idealEdgeLength;
				v.getPred2().setLocation((v.getPred1().getLeft()+w+l), (v.getPred1().getTop()+w+l));
				*/
				v.getPred2().setLocation(v.getLeft()+this.idealEdgeLength, 
					v.getTop()+this.idealEdgeLength);
			}
		}
	}
	
// -----------------------------------------------------------------------------
// Section: FR-Grid Variant Repulsion Force Calculation
// -----------------------------------------------------------------------------
	
	/**
	 * This method calculates the repulsion range
	 * Also it can be used to calculate the height of a grid's edge
	 */
	@Override
	protected double calcRepulsionRange()
	{
		// formula is 2 x (level + 1) x idealEdgeLength
		return (2 * ( this.level+1 ) * this.idealEdgeLength);
	}
	
// -----------------------------------------------------------------------------
// Section: Temporary methods (especially for testing)
// -----------------------------------------------------------------------------

	//TODO: Remove the method after testing
	/**
	 * This method checks if there is a node with null vGraphObject
	 */
	private boolean checkVGraphObjects()
	{
		if (this.graphManager.getAllEdges() == null)
		{
			System.out.println("Edge list is null!");
		}
		if (this.graphManager.getAllNodes() == null)
		{
			System.out.println("Node list is null!");
		}
		if (this.graphManager.getGraphs() == null)
		{
			System.out.println("Graph list is null!");
		}
		for (Object obj: this.graphManager.getAllEdges())
		{
			CoSEEdge e = (CoSEEdge) obj;
			//NodeModel nm = (NodeModel) v.vGraphObject;
			
			if (e.vGraphObject == null)
			{
				System.out.println("Edge(Source): " + e.getSource().getRect() + " has null vGraphObject!");
				return false;
			}
		}
		
		for (Object obj: this.graphManager.getAllNodes())
		{
			CoSENode v = (CoSENode) obj;
			//NodeModel nm = (NodeModel) v.vGraphObject;
			
			if (v.vGraphObject == null)
			{
				System.out.println("Node: " + v.getRect() + " has null vGraphObject!");
				return false;
			}
		}
		
		for (Object obj: this.graphManager.getGraphs())
		{
			LGraph l = (LGraph) obj;
			if (l.vGraphObject == null)
			{
				System.out.println("Graph with " + l.getNodes().size() + " nodes has null vGraphObject!");
				return false;
			}
		}
		return true;
	}
//	private void updateAnnealingProbability()
//	{
//		CoSELayout.annealingProbability = Math.pow(Math.E,
//			CoSELayout.annealingConstant / this.coolingFactor);
//	}
}