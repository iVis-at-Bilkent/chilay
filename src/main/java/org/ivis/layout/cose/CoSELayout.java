package org.ivis.layout.cose;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.awt.*;

import org.ivis.layout.*;
import org.ivis.layout.cose.CoSEConstants.Phase;
import org.ivis.layout.fd.*;
import org.ivis.layout.util.GraphMLWriter;
import org.ivis.util.*;

/**
 * This class implements the overall layout process for the CoSE algorithm.
 * Details of this algorithm can be found in the following article:
 * 		U. Dogrusoz, E. Giral, A. Cetintas, A. Civril, and E. Demir,
 * 		"A Layout Algorithm For Undirected Compound Graphs",
 * 		Information Sciences, 179, pp. 980-994, 2009.
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
	 * Whether or not multi-level scaling should be used to speed up layout
	 */
	public boolean useMultiLevelScaling =
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

    /**
     * Maximum number of layout iterations allowed for phase one
     * of two phase gradual size increase method.
     */
    protected int phaseOneIterations = (int)(maxIterations * (0.25));
    
    /**
     * Maximum number of layout iterations allowed for phase one
     * of two phase gradual size increase method.
     */
    protected int maxPhaseOneIterationsForSizeIncrease = (int)(phaseOneIterations * (0.25));
    
    /**
     * Percent that will be used while incrementing the size of the nodes 
     * in the phase one of the two phase gradual size increase method.
     */
    protected double initialPercentPhaseOne = 0.1;

    /**
     * Maximum number of layout iterations allowed for phase two
     * of two phase gradual size increase method.
     */
    protected int phaseTwoIterations = maxIterations - phaseOneIterations;

    /**
     * Flag for determining whether we are using two phase gradual size
     * increase method or not.
     */
    protected boolean useTwoPhaseGradualSizeIncrease;

    /**
     * variable that holds the current state of the two phase gradual size
     * increase method
     */
    protected CoSEConstants.Phase currentPhase;

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

			if (layoutOptionsPack.idealEdgeLength < 10)
			{
				this.idealEdgeLength = 10;
			}
			else
			{
				this.idealEdgeLength = layoutOptionsPack.idealEdgeLength;
			}

			this.useSmartIdealEdgeLengthCalculation =
				layoutOptionsPack.smartEdgeLengthCalc;
			this.useMultiLevelScaling =
				layoutOptionsPack.multiLevelScaling;
			this.springConstant =
				transform(layoutOptionsPack.springStrength,
					FDLayoutConstants.DEFAULT_SPRING_STRENGTH, 5.0, 5.0);
			this.repulsionConstant =
				transform(layoutOptionsPack.repulsionStrength,
					FDLayoutConstants.DEFAULT_REPULSION_STRENGTH, 5.0, 5.0);
			this.gravityConstant =
				transform(layoutOptionsPack.gravityStrength,
					FDLayoutConstants.DEFAULT_GRAVITY_STRENGTH);
			this.compoundGravityConstant =
				transform(layoutOptionsPack.compoundGravityStrength,
					FDLayoutConstants.DEFAULT_COMPOUND_GRAVITY_STRENGTH);
			this.gravityRangeFactor =
				transform(layoutOptionsPack.gravityRange,
					FDLayoutConstants.DEFAULT_GRAVITY_RANGE_FACTOR);
			this.compoundGravityRangeFactor =
				transform(layoutOptionsPack.compoundGravityRange,
					FDLayoutConstants.DEFAULT_COMPOUND_GRAVITY_RANGE_FACTOR);
			this.useTwoPhaseGradualSizeIncrease =
					layoutOptionsPack.twoPhaseGradualSizeIncrease;
			if (this.useTwoPhaseGradualSizeIncrease) 
			{
				this.currentPhase = CoSEConstants.Phase.FIRST;
			}
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
		getGeneral().createBendsAsNeeded;

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

		// Start coarsening process
		
		// save graph managers M0 to Mk in an array list
		this.MList = gm.coarsenGraph();

		this.noOfLevels = MList.size()-1;
		this.level = this.noOfLevels;
		
		while (this.level >= 0)
		{
			this.graphManager = gm = this.MList.get(this.level);
			
//			System.out.print("@" + this.level + "th level, with " + gm.getRoot().getNodes().size() + " nodes. ");
			this.classicLayout();

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
		
		// Set initial radii of nodes before starting layout
		if (this.useTwoPhaseGradualSizeIncrease)
			this.setInitialRadii();

		do
		{
			this.totalIterations++;

			if (this.totalIterations > phaseOneIterations &&
			   (this.totalIterations % FDLayoutConstants.CONVERGENCE_CHECK_PERIOD == 0))
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
            //this.increaseNodeSizesGradually();
			this.calcSpringForces();
			//GraphMLWriter writer = new GraphMLWriter("C:\\Users\\Istemi\\Desktop\\beforeRep.graphml");
			//GraphMLWriter writer2 = new GraphMLWriter("C:\\Users\\Istemi\\Desktop\\afterRep.graphml");
			//writer.saveGraph(this.getGraphManager());
			this.calcRepulsionForces();
			//this.moveNodes();
			//writer2.saveGraph(this.getGraphManager());
			this.calcGravitationalForces();
			this.moveNodes();

			this.animate();
			
	        /*Object[] lEdges = this.getAllEdges();
	        FDLayoutEdge edge;

	        for (int i = 0; i < lEdges.length; i++)
	        {
	            edge = (FDLayoutEdge) lEdges[i];

	            System.out.println("-----------------------");
		        System.out.println(edge.getSource().label + " " + edge.getTarget().label);
				System.out.println("Edge Length: " + edge.getLength());
	        }*/
		}
		while (this.totalIterations < this.maxIterations);
	
		this.graphManager.updateBounds();
	}
	
	protected void setInitialRadii()
	{
		Object[] lNodes = this.getAllNodes();
		CoSENode node;
        double nodeWidth = 0;
        double nodeHeight = 0;
		
		 for (int i = 0; i < lNodes.length; i++)
         {
             node = (CoSENode) lNodes[i];

             if (node.getChild() == null)
             {
                 nodeWidth  = node.getWidth();
                 nodeHeight = node.getHeight() ;
             }
             
             //TODO handle ellipse case also

             /*
              If the nodes are represented by circles in the first phase
              update the radius of the node  to half of the maximum of width
              and height. Set radiusY also for convenience
             */
             node.setRadiusX(Math.max(nodeWidth/2,nodeHeight/2) /* * initialPercentPhaseOne*/);
             node.setRadiusY(Math.max(nodeWidth/2,nodeHeight/2) /* * initialPercentPhaseOne*/);
         }

	}

    /**
     * This method updates the radius of the CoSENodes in the first phase by setting the radii of the ellipse or
     * radius of the circle that is representing the node according to the number of total iterations and the number
     * of phase one iterations.
     * */
    protected void increaseNodeSizesGradually()
    {
        Object[] lNodes = this.getAllNodes();
        CoSENode node;
        double nodeWidth;
        double nodeHeight;
        double increaseRatio = (this.totalIterations / this.maxPhaseOneIterationsForSizeIncrease);

        if (this.useTwoPhaseGradualSizeIncrease)
        {
            if (this.totalIterations == this.phaseOneIterations)
            {
                this.currentPhase = CoSEConstants.Phase.SECOND;
            }

            if (this.currentPhase == CoSEConstants.Phase.FIRST /*&&
               (this.totalIterations % CoSEConstants.DEFAULT_TWO_PHASE_SIZE_INCREASE_CHECK_PERIOD == 0)*/)
            {
                for (int i = 0; i < lNodes.length; i++)
                {
                    node = (CoSENode) lNodes[i];
                    nodeWidth  = node.getWidth();
                    nodeHeight = node.getHeight();

                    if (node.getChild() == null)
                    //Increase the size gradually for the non compound nodes
                    {
                        nodeWidth  = node.getWidth()*initialPercentPhaseOne + (node.getWidth() *  (1-initialPercentPhaseOne))*increaseRatio;
                        nodeHeight = node.getHeight()*initialPercentPhaseOne +(node.getHeight() * (1-initialPercentPhaseOne))*increaseRatio;
                    }

                    //TODO handle ellipse case also

                    /*
                     If the nodes are represented by circles in the first phase
                     update the radius of the node  to half of the maximum of width
                     and height. Set radiusY also for convenience
                    */
                    node.setRadiusX(10*Math.max(nodeWidth/2,nodeHeight/2));
                    node.setRadiusY(10*Math.max(nodeWidth/2,nodeHeight/2));

                }
            }
        }
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
// Section: Two Phase Gradual Size Increase Method Calculations
// -----------------------------------------------------------------------------

    @Override
    protected boolean updateEdgeLength(LEdge edge)
    {
        FDLayoutNode sourceNode = (FDLayoutNode) edge.getSource();
        FDLayoutNode targetNode = (FDLayoutNode) edge.getTarget();

        // Update edge length
        if (this.uniformLeafNodeSizes &&
                sourceNode.getChild() == null && targetNode.getChild() == null)
        {
            edge.updateLengthSimple();
        }
        else
        {
            if (this.useTwoPhaseGradualSizeIncrease && currentPhase == CoSEConstants.Phase.FIRST)
            // If two phase gradual size increase method is active update edges according to circular vertices.
            {
                ((CoSEEdge)edge).updateLengthCircular();
                
                if (edge.isOverlapingSourceAndTarget())
                {
                    return false;
                }
            }
            else
            // Regular rectangular calculations takes place here.
            {
                edge.updateLength();
            }
        }
        
        if (edge.isOverlapingSourceAndTarget())
        {
            return false;
        }
        
        return true;
    }

    @Override
    protected void getRepulsionForceOverlap(FDLayoutNode nodeA, FDLayoutNode nodeB, double[] repulsionForceComponents)
    {
        RectangleD rectA = nodeA.getRect();
        RectangleD rectB = nodeB.getRect();
        double[] overlapAmount = new double[2];

        if (this.useTwoPhaseGradualSizeIncrease && this.currentPhase == CoSEConstants.Phase.FIRST)
        // we are using two phase gradual size increase method
        // we should handle overlap case differently.
        // circular overlap calculations for first phase
        {
	        // calculate circular separation amount in X and Y directions
	        PointD centerA = new PointD(rectA.getCenterX(), rectA.getCenterY());
	        PointD centerB = new PointD(rectB.getCenterX(), rectB.getCenterY());
	        IGeometry.calcCircularSeperationAmount(centerA, 
	        		centerB, 
	        		((CoSENode)nodeA).getRadiusX(),
	                ((CoSENode)nodeB).getRadiusX(),
	                overlapAmount, 
	                FDLayoutConstants.DEFAULT_EDGE_LENGTH / 2.0);
	
	        repulsionForceComponents[0] = overlapAmount[0];
	        repulsionForceComponents[1] = overlapAmount[1];
	        
	        // Assert if two circles overlap after seperation
	        double radiusA = ((CoSENode)nodeA).getRadiusX();
   		 	double radiusB = ((CoSENode)nodeB).getRadiusX();
   		 	
   		    centerA.setX(centerA.getX() - repulsionForceComponents[0]);
   		    centerA.setY(centerA.getY() - repulsionForceComponents[1]);
   		    
   		    centerB.setX(centerB.getX() + repulsionForceComponents[0]);
   		    centerB.setY(centerB.getY() + repulsionForceComponents[1]);
   		    
	        /*System.out.println("-----------------------");
	        System.out.println(nodeA.label + " " + nodeB.label);
   		    System.out.println(centerA + " radius: " + radiusA + " rectW: " + rectA.getWidth()+ " rectH: " + rectA.getHeight());
   		    System.out.println(centerB + " radius: " + radiusB + " rect: "  + rectB.getWidth() + " rectH: " + rectA.getHeight());
   		    System.out.println("Seperation amount: " + repulsionForceComponents[0] + " " + repulsionForceComponents[1]);
   		    System.out.println(IGeometry.intersectsCircle(centerA, centerB, radiusA, radiusB));*/
   		    
	        assert !(IGeometry.intersectsCircle(centerA, 
	        		centerB,
	        		radiusA, 
	        		radiusB));
        }
        else
        // calculations for second phase
        {
            // calculate separation amount in x and y directions
            IGeometry.calcSeparationAmount(rectA,
                    rectB,
                    overlapAmount,
                    FDLayoutConstants.DEFAULT_EDGE_LENGTH / 2.0);

            repulsionForceComponents[0] = overlapAmount[0];
            repulsionForceComponents[1] = overlapAmount[1];
            
            RectangleD rect1 = new RectangleD((rectA.x - repulsionForceComponents[0]),
                    (rectA.y - repulsionForceComponents[1]),
                    rectA.width,
                    rectA.height);
            RectangleD rect2 = new RectangleD((rectB.x + repulsionForceComponents[0]),
                    (rectB.y + repulsionForceComponents[1]),
                    rectB.width,
                    rectB.height);

           assert !(rect1.intersects(rect2));
        }
    }
    
    /**
     * This method inherits the intersects method of FDLayout class.
     * If layout is running in first phase this method checks whether
     * circles that represent these nodes intersects or not 
     * otherwise method checks rectangular intersection state
     * 
     * */
    @Override
    protected boolean intersects(FDLayoutNode nodeA, FDLayoutNode nodeB)
    {
    	 if (this.useTwoPhaseGradualSizeIncrease && 
    		 this.currentPhase == CoSEConstants.Phase.FIRST )
         // we are using two phase gradual size increase method
         // we should calculate circular intersection in first phase
         {
    		 PointD centerA = new PointD(nodeA.getCenterX(), nodeA.getCenterY());
    		 PointD centerB = new PointD(nodeB.getCenterX(), nodeB.getCenterY());
    		 double radiusA = ((CoSENode)nodeA).getRadiusX();
    		 double radiusB = ((CoSENode)nodeB).getRadiusX();
    		 return IGeometry.intersectsCircle(centerA, centerB, radiusA, radiusB);
         }
    	 else
    		 return super.intersects(nodeA, nodeB);
    }

    @Override
    protected void  getDistanceBetweenTwoNodes(FDLayoutNode nodeA, FDLayoutNode nodeB, double [] distanceComponents)
    {
        RectangleD rectA = nodeA.getRect();
        RectangleD rectB = nodeB.getRect();
        double[] clipPoints = new double[4];

        // calculate distance
        if (this.uniformLeafNodeSizes &&
                nodeA.getChild() == null && nodeB.getChild() == null)
        // simply base repulsion on distance of node centers
        {
            distanceComponents[0] = rectB.getCenterX() - rectA.getCenterX();
            distanceComponents[1] = rectB.getCenterY() - rectA.getCenterY();
        }
        else
        // non uniformly sized vertices, use clipping points
        {
            if (this.useTwoPhaseGradualSizeIncrease && this.currentPhase == CoSEConstants.Phase.FIRST)
            // nodes are circular in first phase of gradual size increase method so calculate the distance
            // using circular clipping point calculations
            {
                // retrieve the shortest distance between two circular vertices.
                PointD centerA = new PointD(rectA.getCenterX(), rectA.getCenterY());
                PointD centerB = new PointD(rectB.getCenterX(), rectB.getCenterY());
                // Get circular clipping points
                IGeometry.getCircularIntersection(centerA, 
                		centerB, 
                		((CoSENode)nodeA).getRadiusX(),
                        ((CoSENode)nodeB).getRadiusX(),
                        clipPoints);

            }
            else
            // Regular rectangular clipping point calculation takes place
            {
                IGeometry.getIntersection(rectA, rectB, clipPoints);
            }

            distanceComponents[0] = clipPoints[2] - clipPoints[0];
            distanceComponents[1] = clipPoints[3] - clipPoints[1];

        }
    }

// -----------------------------------------------------------------------------
// Section: Temporary methods (especially for testing)
// -----------------------------------------------------------------------------

//	/**
//	 * This method checks if there is a node with null vGraphObject
//	 */
//	private boolean checkVGraphObjects()
//	{
//		if (this.graphManager.getAllEdges() == null)
//		{
//			System.out.println("Edge list is null!");
//		}
//		if (this.graphManager.getAllNodes() == null)
//		{
//			System.out.println("Node list is null!");
//		}
//		if (this.graphManager.getGraphs() == null)
//		{
//			System.out.println("Graph list is null!");
//		}
//		for (Object obj: this.graphManager.getAllEdges())
//		{
//			CoSEEdge e = (CoSEEdge) obj;
//			//NodeModel nm = (NodeModel) v.vGraphObject;
//
//			if (e.vGraphObject == null)
//			{
//				System.out.println("Edge(Source): " + e.getSource().getRect() + " has null vGraphObject!");
//				return false;
//			}
//		}
//
//		for (Object obj: this.graphManager.getAllNodes())
//		{
//			CoSENode v = (CoSENode) obj;
//			//NodeModel nm = (NodeModel) v.vGraphObject;
//
//			if (v.vGraphObject == null)
//			{
//				System.out.println("Node: " + v.getRect() + " has null vGraphObject!");
//				return false;
//			}
//		}
//
//		for (Object obj: this.graphManager.getGraphs())
//		{
//			LGraph l = (LGraph) obj;
//			if (l.vGraphObject == null)
//			{
//				System.out.println("Graph with " + l.getNodes().size() + " nodes has null vGraphObject!");
//				return false;
//			}
//		}
//		return true;
//	}
//	private void updateAnnealingProbability()
//	{
//		CoSELayout.annealingProbability = Math.pow(Math.E,
//			CoSELayout.annealingConstant / this.coolingFactor);
//	}
}