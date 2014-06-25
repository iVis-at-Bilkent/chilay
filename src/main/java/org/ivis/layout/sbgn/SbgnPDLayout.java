package org.ivis.layout.sbgn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.ivis.layout.LEdge;
import org.ivis.layout.LGraph;
import org.ivis.layout.LNode;
import org.ivis.layout.LayoutConstants;
import org.ivis.layout.cose.CoSELayout;
import org.ivis.layout.fd.FDLayoutConstants;
import org.ivis.layout.fd.FDLayoutEdge;
import org.ivis.layout.fd.FDLayoutNode;
import org.ivis.layout.util.MemberPack;
import org.ivis.layout.util.RectProc;
import org.ivis.util.PointD;
import org.ivis.util.RectangleD;

/**
 * This class implements the layout process of SBGN notation.
 * 
 * @author Begum Genc
 * 
 *         Copyright: i-Vis Research Group, Bilkent University, 2007 - present
 */
public class SbgnPDLayout extends CoSELayout
{
	// ************************* SECTION : VARIABLES *************************

	/**
	 * For remembering contents of a complex.
	 */
	Map<SbgnPDNode, LGraph> childGraphMap;

	/**
	 * Used during Tiling.
	 */
	Map<SbgnPDNode, MemberPack> memberPackMap;

	/**
	 * List of dummy complexes (a dummy complex stores zero degree nodes)
	 */
	LinkedList<SbgnPDNode> dummyComplexList;

	Map<SbgnPDNode, LGraph> removedDummyComplexMap;

	/**
	 * This list stores the complex molecules as a result of DFS. The first
	 * element corresponds to the deep-most node.
	 */
	LinkedList<SbgnPDNode> complexOrder;

	/**
	 * This parameter indicates the chosen compaction method.
	 */
	private DefaultCompactionAlgorithm compactionMethod;

	/**
	 * This parameter stores the properly oriented total edge count of all
	 * process nodes.
	 */
	public double properlyOrientedEdgeCount;

	/**
	 * This parameter stores the total neighboring edge count of all process
	 * nodes.
	 */
	public double totalEdgeCount;

	/**
	 * This parameter indicates the phase number (1 or 2)
	 */
	private int phaseNumber;

	public int phase1IterationCount;
	public int phase2IterationCount;

	/**
	 * The constructor creates and associates with this layout a new graph
	 * manager as well. No tiling performs CoSE Layout.
	 * 
	 * @param testApplet
	 * 
	 * @param compactionMethod
	 *            - SbgnPDConstants.TILING, SbgnPDConstants.POLYOMINO_PACKING
	 */
	public SbgnPDLayout()
	{
		compactionMethod = DefaultCompactionAlgorithm.TILING;

		childGraphMap = new HashMap<SbgnPDNode, LGraph>();
		complexOrder = new LinkedList<SbgnPDNode>();
		dummyComplexList = new LinkedList<SbgnPDNode>();
		removedDummyComplexMap = new HashMap<SbgnPDNode, LGraph>();

		if (compactionMethod == DefaultCompactionAlgorithm.TILING)
			memberPackMap = new HashMap<SbgnPDNode, MemberPack>();
	}

	// ********************** SBGNPD SPECIFIC METHODS **********************

	/**
	 * At this phase, pure CoSE is applied for a number of iterations.
	 */
	private void doPhase1()
	{
		this.maxIterations = 2000;
		this.totalIterations = 0;

		do
		{
			totalIterations++;
			if (this.totalIterations
					% FDLayoutConstants.CONVERGENCE_CHECK_PERIOD == 0)
			{

				if (this.isConverged())
				{
					break;
				}

				this.coolingFactor = this.initialCoolingFactor
						* ((this.maxIterations - this.totalIterations) / (double) this.maxIterations);
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

		this.graphManager.updateBounds();
		phase1IterationCount = totalIterations;
		System.out.print("" + totalIterations + " ");
	}

	private void doPhase2()
	{
		double newRatio = 0.0;
		this.maxIterations = 5000;
		this.initialCoolingFactor = 0.3;
		this.coolingFactor = this.initialCoolingFactor;

		this.totalIterations = 0;

		do
		{
			this.totalIterations++;
			// System.out.println("iter: " + totalIterations);

			if (this.totalIterations == 1 || this.totalIterations % 401 == 0)
			{
				approximateSingleNodesPositions();
			}

			if (this.totalIterations
					% FDLayoutConstants.CONVERGENCE_CHECK_PERIOD == 0)
			{
				newRatio = properlyOrientedEdgeCount / totalEdgeCount;

				if (this.isConverged()
						&& newRatio > SbgnPDConstants.ROTATIONAL_FORCE_CONVERGENCE)
				{
					break;
				}

				this.coolingFactor = this.initialCoolingFactor
						* ((this.maxIterations - this.totalIterations) / (double) this.maxIterations);
				// this.updateAnnealingProbability();
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

		System.out.println(properlyOrientedEdgeCount + " " + totalEdgeCount
				+ " " + totalIterations + " "
				+ (properlyOrientedEdgeCount / totalEdgeCount));

		// if (properlyOrientedEdgeCount / totalEdgeCount >
		// SbgnPDConstants.ROTATIONAL_FORCE_CONVERGENCE)
		// System.out.println("converged: "
		// + properlyOrientedEdgeCount + " / "
		// + totalEdgeCount);
		// else
		// System.out.println("NOT converged: "
		// + properlyOrientedEdgeCount + " / "
		// + totalEdgeCount);

		phase2IterationCount = totalIterations;
		this.graphManager.updateBounds();
		System.out.println("phase2 has finished after " + totalIterations
				+ " iterations");
	}

	/**
	 * This method creates a port node with the associated type (input/output
	 * port)
	 */
	public LNode newPortNode(Object vNode, String type)
	{
		SbgnPDNode n = new SbgnPDNode(this.graphManager, vNode);
		n.type = type;
		n.setWidth(SbgnPDConstants.PORT_NODE_DEFAULT_WIDTH);
		n.setHeight(SbgnPDConstants.PORT_NODE_DEFAULT_HEIGHT);

		return n;
	}

	/**
	 * This method creates an SBGNProcessNode object
	 */
	public LNode newProcessNode(Object vNode)
	{
		return new SbgnProcessNode(this.graphManager, vNode);
	}

	/**
	 * This method creates a rigid edge.
	 */
	public LEdge newRigidEdge(Object vEdge)
	{
		SbgnPDEdge e = new SbgnPDEdge(null, null, vEdge);
		e.type = SbgnPDConstants.RIGID_EDGE;
		return e;
	}

	/**
	 * This method creates two port nodes and a compound for each process nodes
	 * and adds them to graph.
	 */
	private void createPortNodes()
	{
		ArrayList<SbgnProcessNode> processList = new ArrayList<SbgnProcessNode>();
		for (Object o : this.getAllNodes())
		{
			SbgnPDNode s = (SbgnPDNode) o;
			if (s.type.equals(SbgnPDConstants.PROCESS))
			{
				LGraph ownerGraph = s.getOwner();

				// create new nodes and graphs
				SbgnProcessNode processNode = (SbgnProcessNode) newProcessNode(null);
				SbgnPDNode inputPort = (SbgnPDNode) newPortNode(null,
						SbgnPDConstants.INPUT_PORT);
				SbgnPDNode outputPort = (SbgnPDNode) newPortNode(null,
						SbgnPDConstants.OUTPUT_PORT);
				SbgnPDNode compoundNode = (SbgnPDNode) newNode(null);
				compoundNode.type = SbgnPDConstants.DUMMY_COMPOUND;

				processList.add(processNode);

				compoundNode.label = "DummyCompound_" + s.label;
				inputPort.label = "InputPort_" + s.label;
				outputPort.label = "OutputPort_" + s.label;

				LGraph childGraph = newGraph(null);

				ownerGraph.add(processNode);
				
				// convert the process node to our specific SbgnProcessNode
				processNode.copyFromSBGNPDNode(s, this.getGraphManager());

				processNode.setConnectedNodes(compoundNode, inputPort,
						outputPort);

				// create rigid edges, change edge connections
				connectEdges(inputPort, outputPort, processNode);

				SbgnPDEdge rigidPortProcess = (SbgnPDEdge) newRigidEdge(null);
				rigidPortProcess.label = ""
						+ (this.graphManager.getAllEdges().length + 1);

				SbgnPDEdge rigidPortConsumption = (SbgnPDEdge) newRigidEdge(null);
				rigidPortConsumption.label = ""
						+ (this.graphManager.getAllEdges().length + 1);

				ownerGraph.remove(processNode);
				
				// move nodes to compound
				childGraph.add(processNode);
				childGraph.add(inputPort);
				childGraph.add(outputPort);

				// move edges to compound
				childGraph.add(rigidPortProcess, inputPort, processNode);
				childGraph.add(rigidPortConsumption, outputPort, processNode);

				compoundNode.setOwner(ownerGraph);

				compoundNode.setCenter(processNode.getCenterX(),
						processNode.getCenterY());

				ownerGraph.add(compoundNode);

				this.graphManager.add(childGraph, compoundNode);

				ownerGraph.remove(s);

				graphManager.updateBounds();
			}
		}

		// reset the topology
		this.graphManager.resetAllNodes();
		this.graphManager.resetAllNodesToApplyGravitation();
		this.graphManager.resetAllEdges();

		// int random = (int) (Math.random() * processList.size());
		// processList.get(random).isHighlighted = true;
	}

	/**
	 * This method checks whether there exists any process nodes in the graph.
	 * If there exist any process nodes, the existence of port nodes are
	 * checked. The method returns true if the graph has at least one port node
	 * OR the graph has no process nodes.
	 * 
	 * @return
	 */
	private boolean arePortNodesCreated()
	{
		boolean flag = false;

		// if there are any process nodes, check for port nodes
		for (Object o : this.getAllNodes())
		{
			SbgnPDNode s = (SbgnPDNode) o;
			if (s.type.equals(SbgnPDConstants.PROCESS))
			{
				flag = true;
				break;
			}
		}

		// if there are no process nodes, no need to check for port nodes
		if (!flag)
			return true;

		else
		{
			// check for the port nodes. if any found, return true.
			for (Object o : this.getAllNodes())
			{
				if (((SbgnPDNode) o).type.equals(SbgnPDConstants.INPUT_PORT)
						|| ((SbgnPDNode) o).type
								.equals(SbgnPDConstants.OUTPUT_PORT))
					return true;
			}
		}
		return false;
	}

	/**
	 * Connect the port node to its process node (parent) and connect the edges
	 * of neighbor nodes to the port node by considering their types (for both
	 * input port and output port)
	 */
	public void connectEdges(SbgnPDNode inputPort, SbgnPDNode outputPort,
			SbgnProcessNode processNode)
	{
		// change connections from process node&neighbors to port&neighbors.
		for (int i = 0; i < processNode.getEdges().size(); i++)
		{
			SbgnPDEdge sEdge = (SbgnPDEdge) processNode.getEdges().get(i);

			processNode.getEdges().remove(sEdge);
			if (sEdge.type.equals(SbgnPDConstants.CONSUMPTION))
			{
				sEdge.setTarget(inputPort);
				inputPort.getEdges().add(sEdge);
				System.out.println("label: " + sEdge.label);
			}
			else if (sEdge.type.equals(SbgnPDConstants.PRODUCTION))
			{
				sEdge.setSource(outputPort);
				outputPort.getEdges().add(sEdge);
				System.out.println("label: " + sEdge.label);
			}

			i--;
		}
	}

	/**
	 * This method re-calculates nodes to apply gravitation. (It is a good idea
	 * to call this method after new node creation. Here, it is used after the
	 * creation of port nodes)
	 */
	private void resetNodesToApplyGravitation()
	{
		ArrayList<Object> allNodesToApplyGravitation = new ArrayList<Object>();
		for (Object o : getAllNodes())
		{
			if (o instanceof SbgnProcessNode
					|| ((SbgnPDNode) o).type.equals(SbgnPDConstants.INPUT_PORT)
					|| ((SbgnPDNode) o).type
							.equals(SbgnPDConstants.OUTPUT_PORT))
				continue;
			else
				allNodesToApplyGravitation.add(o);
		}

		this.getGraphManager().setAllNodesToApplyGravitation(
				allNodesToApplyGravitation);
	}

	/**
	 * This method finds all the single-edge neighbors of port nodes -except
	 * rigid-edged node aka process node-. They are grouped as the nodes that
	 * have a single edge and the nodes that have more than one edge.
	 */
	private void approximateSingleNodesPositions()
	{
		LinkedList<SbgnPDNode> oneEdgeNodes;
		LinkedList<SbgnPDNode> multiEdgeNodes;

		// get all process nodes
		for (Object o : getAllNodes())
		{
			oneEdgeNodes = new LinkedList<SbgnPDNode>();
			multiEdgeNodes = new LinkedList<SbgnPDNode>();

			if (!(o instanceof SbgnProcessNode))
				continue;

			SbgnProcessNode processNode = (SbgnProcessNode) o;

			// get all non-rigid edges of the input port node
			for (Object e : processNode.getInputPort().getEdges())
			{
				SbgnPDEdge edge = (SbgnPDEdge) e;

				if (edge.type.equals(SbgnPDConstants.RIGID_EDGE))
					continue;

				if (edge.getSource().getEdges().size() == 1)
					oneEdgeNodes.add((SbgnPDNode) edge.getSource());
				else if (edge.getSource().getEdges().size() > 1)
					multiEdgeNodes.add((SbgnPDNode) edge.getSource());
			}

			if (oneEdgeNodes.size() > 0)
				moveOneEdgeNodes(oneEdgeNodes, multiEdgeNodes);

			oneEdgeNodes.clear();
			multiEdgeNodes.clear();

			// get all non-rigid edges of the output port node
			for (Object e : processNode.getOutputPort().getEdges())
			{
				SbgnPDEdge edge = (SbgnPDEdge) e;

				if (edge.type.equals(SbgnPDConstants.RIGID_EDGE))
					continue;

				if (edge.getTarget().getEdges().size() == 1)
					oneEdgeNodes.add((SbgnPDNode) edge.getTarget());
				else if (edge.getTarget().getEdges().size() > 1)
					multiEdgeNodes.add((SbgnPDNode) edge.getTarget());
			}

			if (oneEdgeNodes.size() > 0)
				moveOneEdgeNodes(oneEdgeNodes, multiEdgeNodes);
		}
	}

	/**
	 * Single-edge nodes are moved around the average center point of a set of
	 * node whose edge degree > 0. If all the neighbor nodes of a port node are
	 * single-edged, one of them is chosen randomly and the others are placed
	 * around it.
	 */
	private void moveOneEdgeNodes(LinkedList<SbgnPDNode> oneEdgeNodes,
			LinkedList<SbgnPDNode> multiEdgeNodes)
	{
		PointD approximationPnt = new PointD(0, 0);
		int randomIndex = -1;
		SbgnPDNode approximationNode = null;

		if (multiEdgeNodes.size() > 0)
		{
			randomIndex = (int) (Math.random() * multiEdgeNodes.size());
			approximationNode = multiEdgeNodes.get(randomIndex);
			approximationPnt.x = approximationNode.getCenterX();
			approximationPnt.y = approximationNode.getCenterY();
			// for (SbgnPDNode s : multiEdgeNodes)
			// {
			// approximationPnt.x += s.getCenterX();
			// approximationPnt.y += s.getCenterY();
			// }
			// approximationPnt.x /= multiEdgeNodes.size();
			// approximationPnt.y /= multiEdgeNodes.size();
		}
		else if (multiEdgeNodes.size() == 0)
		{
			randomIndex = (int) (Math.random() * oneEdgeNodes.size());
			approximationNode = oneEdgeNodes.get(randomIndex);
			approximationPnt.x = approximationNode.getCenterX();
			approximationPnt.y = approximationNode.getCenterY();
		}

		for (SbgnPDNode s : oneEdgeNodes)
		{
			if (approximationNode.getOwner() != s.getOwner())
				continue;

			PointD newPoint = new PointD();
			newPoint.x = approximationPnt.x
					+ (Math.random() * SbgnPDConstants.APPROXIMATION_DISTANCE * 2)
					- SbgnPDConstants.APPROXIMATION_DISTANCE;
			newPoint.y = approximationPnt.y
					+ (Math.random() * SbgnPDConstants.APPROXIMATION_DISTANCE * 2)
					- SbgnPDConstants.APPROXIMATION_DISTANCE;

			s.setCenter(newPoint.x, newPoint.y);
		}
	}

	/**
	 * This method is used to remove the dummy compounds from the graph.
	 */
	private void removeDummyCompounds()
	{
		for (Object o : getAllNodes())
		{
			SbgnPDNode node = (SbgnPDNode) o;

			if (node.isDummyCompound)
			{
				LGraph childGraph = node.getChild();
				LGraph owner = node.getOwner();


				// add children to original parent
				for (Object s : childGraph.getNodes())
					owner.add((SbgnPDNode) s);
				for (Object e : childGraph.getEdges())
				{
					SbgnPDEdge edge = (SbgnPDEdge) e;
					owner.add(edge, edge.getSource(), edge.getTarget());
				}

				// remove the graph
				getGraphManager().getGraphs().remove(childGraph);
				node.setChild(null);
				owner.remove(node);
			}
		}
		
		getGraphManager().resetAllNodes();
		getGraphManager().resetAllNodesToApplyGravitation();
		getGraphManager().resetAllEdges();
	}

	// ************************** TILING METHODS **************************

	/**
	 * This method searched unmarked complex nodes recursively, because they may
	 * contain complex children. After the order is found, child graphs of each
	 * complex node are cleared.
	 */
	private void applyDFSOnComplexes()
	{
		// LGraph>();
		for (Object o : getAllNodes())
		{
			if (!(o instanceof SbgnPDNode) || !((SbgnPDNode) o).isComplex())
				continue;

			SbgnPDNode comp = (SbgnPDNode) o;

			// complex is found, recurse on it until no visited complex remains.
			if (!comp.visited)
				DFSVisitComplex(comp);
		}

		// clear each complex
		for (SbgnPDNode o : complexOrder)
		{
			clearComplex(o);
		}

		this.getGraphManager().updateBounds();

		getGraphManager().resetAllNodes();
		getGraphManager().resetAllNodesToApplyGravitation();
		getGraphManager().resetAllEdges();
	}

	/**
	 * This method recurses on the complex objects. If a node does not contain
	 * any complex nodes or all the nodes in the child graph is already marked,
	 * it is reported. (Depth first)
	 * 
	 */
	private void DFSVisitComplex(SbgnPDNode node)
	{
		if (node.getChild() != null)
		{
			for (Object n : node.getChild().getNodes())
			{
				SbgnPDNode sbgnChild = (SbgnPDNode) n;
				DFSVisitComplex(sbgnChild);
			}
		}

		if (node.isComplex() && !node.containsUnmarkedComplex())
		{
			complexOrder.add(node);
			node.visited = true;
			return;
		}
	}

	/**
	 * This method finds all the zero degree nodes in the graph which are not
	 * owned by a complex node. Zero degree nodes are grouped wrt their parents
	 * and each group is placed inside a complex.
	 */
	private void groupZeroDegreeMembers()
	{
		Map <SbgnPDNode, LGraph> childComplexMap = new HashMap<SbgnPDNode, LGraph>();
		for (Object graphObj : this.getGraphManager().getGraphs())
		{
			ArrayList<SbgnPDNode> zeroDegreeNodes = new ArrayList<SbgnPDNode>();
			LGraph ownerGraph = (LGraph) graphObj;

			// do not process complex nodes
			if (ownerGraph.getParent().type != null
					&& ((SbgnPDNode) ownerGraph.getParent()).isComplex())
				continue;

			for (Object nodeObj : ownerGraph.getNodes())
			{
				SbgnPDNode node = (SbgnPDNode) nodeObj;

				if (calcGraphDegree(node) == 0)
				{
					zeroDegreeNodes.add(node);
				}
			}

			if (zeroDegreeNodes.size() > 1)
			{
				// create a new dummy complex
				SbgnPDNode complex = (SbgnPDNode) newNode(null);
				complex.type = SbgnPDConstants.COMPLEX;
				complex.label = "DummyComplex_" + ownerGraph.getParent().label;

				ownerGraph.add(complex);

				LGraph childGraph = newGraph(null);

				for (SbgnPDNode zeroNode : zeroDegreeNodes)
				{
					ownerGraph.remove(zeroNode);
					childGraph.add(zeroNode);
				}
				dummyComplexList.add(complex);
				childComplexMap.put(complex, childGraph);
			}
		}

		for (SbgnPDNode complex : dummyComplexList)
			this.graphManager.add(childComplexMap.get(complex), complex);

		this.getGraphManager().updateBounds();

		this.graphManager.resetAllNodes();
		this.graphManager.resetAllNodesToApplyGravitation();
		this.graphManager.resetAllEdges();
	}

	private int calcGraphDegree(SbgnPDNode parentNode)
	{
		int degree = 0;
		if (parentNode.getChild() == null)
		{
			degree = parentNode.getEdges().size();
			return degree;
		}

		for (Object o : parentNode.getChild().getNodes())
		{
			degree = degree + parentNode.getEdges().size()
					+ calcGraphDegree((SbgnPDNode) o);
		}

		return degree;
		// if ((parentNode.getChild() == null && parentNode.getEdges().size() ==
		// 0)
		// || (parentNode.getChild() != null && parentNode.getEdges().size() ==
		// 0
		// && parentNode.getChild().getEdges().size() == 0))
		// return 0;
	}

	/**
	 * This method applies polyomino packing on the child graph of a complex
	 * member and then..
	 * 
	 * @param comp
	 */
	private void clearComplex(SbgnPDNode comp)
	{
		MemberPack pack = null;
		LGraph childGr = comp.getChild();
		childGraphMap.put(comp, childGr);

		if (childGr == null)
			return;

		if (compactionMethod == DefaultCompactionAlgorithm.POLYOMINO_PACKING)
		{
			applyPolyomino(comp);
		}
		else if (compactionMethod == DefaultCompactionAlgorithm.TILING)
		{
			pack = new MemberPack(childGr);
			memberPackMap.put(comp, pack);
		}

		if (dummyComplexList.contains(comp))
		{
			for (Object o : comp.getChild().getNodes())
			{
				removeDummyComplexGraphs((SbgnPDNode) o);
			}
		}

		getGraphManager().getGraphs().remove(childGr);
		comp.setChild(null);

		if (compactionMethod == DefaultCompactionAlgorithm.TILING)
		{
			comp.setWidth(pack.getWidth());
			comp.setHeight(pack.getHeight());
		}

		// Redirect the edges of complex members to the complex.
		if (childGr != null)
		{
			for (Object ch : childGr.getNodes())
			{
				SbgnPDNode chNd = (SbgnPDNode) ch;

				for (Object obj : new ArrayList(chNd.getEdges()))
				{
					LEdge edge = (LEdge) obj;
					if (edge.getSource() == chNd)
					{
						chNd.getEdges().remove(edge);
						edge.setSource(comp);
						comp.getEdges().add(edge);
					}
					else if (edge.getTarget() == chNd)
					{
						chNd.getEdges().remove(edge);
						edge.setTarget(comp);
						comp.getEdges().add(edge);
					}
				}
			}
		}
	}

	private void removeDummyComplexGraphs(SbgnPDNode comp)
	{
		if (comp.getChild() == null || comp.isDummyCompound)
		{
			return;
		}
		for (Object o : comp.getChild().getNodes())
		{
			SbgnPDNode childNode = (SbgnPDNode) o;
			if (childNode.getChild() != null
					&& childNode.getEdges().size() == 0)
				removeDummyComplexGraphs(childNode);
		}
		if (this.graphManager.getGraphs().contains(comp.getChild()))
		{
			if (calcGraphDegree(comp) == 0)
			{
				removedDummyComplexMap.put(comp, comp.getChild());

				this.getGraphManager().getGraphs().remove(comp.getChild());
				comp.setChild(null);
			}
		}
	}

	/**
	 * This method tiles the given list of nodes by using polyomino packing
	 * algorithm.
	 */
	private void applyPolyomino(SbgnPDNode parent)
	{
		RectangleD r;
		LGraph childGr = parent.getChild();

		if (childGr == null)
		{
			System.out.println("Child graph is empty (Polyomino)");
		}
		else
		{
			// packing takes the input as an array. put the members in an array.
			SbgnPDNode[] mpArray = new SbgnPDNode[childGr.getNodes().size()];
			for (int i = 0; i < childGr.getNodes().size(); i++)
			{
				SbgnPDNode s = (SbgnPDNode) childGr.getNodes().get(i);
				mpArray[i] = s;
			}

			// pack rectangles
			RectProc.packRectanglesMino(
					SbgnPDConstants.COMPLEX_MEM_HORIZONTAL_BUFFER,
					mpArray.length, mpArray);

			// apply compaction
			Compaction c = new Compaction(
					(ArrayList<SbgnPDNode>) childGr.getNodes());
			c.perform();

			// get the resulting rectangle and set parent's (complex) width &
			// height
			r = calculateBounds(true,
					(ArrayList<SbgnPDNode>) childGr.getNodes());

			parent.setWidth(r.getWidth());
			parent.setHeight(r.getHeight());
		}
	}

	/**
	 * Reassigns the complex content. The outermost complex is placed first.
	 */
	protected void repopulateComplexes()
	{
		for (SbgnPDNode comp : removedDummyComplexMap.keySet())
		{
			LGraph chGr = removedDummyComplexMap.get(comp);
			comp.setChild(chGr);
			this.getGraphManager().getGraphs().add(chGr);
		}

		for (int i = complexOrder.size() - 1; i >= 0; i--)
		{
			SbgnPDNode comp = complexOrder.get(i);
			LGraph chGr = childGraphMap.get(comp);

			// repopulate the complex
			comp.setChild(chGr);

			// if the child graph is not null, adjust the positions of members
			if (chGr != null)
			{
				// adjust the positions of the members
				if (compactionMethod == DefaultCompactionAlgorithm.POLYOMINO_PACKING)
				{
					adjustLocation(comp, chGr);
					getGraphManager().getGraphs().add(chGr);
				}
				else if (compactionMethod == DefaultCompactionAlgorithm.TILING)
				{
					getGraphManager().getGraphs().add(chGr);

					MemberPack pack = memberPackMap.get(comp);
					pack.adjustLocations(comp.getLeft(), comp.getTop());
				}
			}
		}
		for (SbgnPDNode comp : removedDummyComplexMap.keySet())
		{
			LGraph chGr = removedDummyComplexMap.get(comp);

			adjustLocation(comp, chGr);
		}

		removeDummyComplexes();

		// reset
		getGraphManager().resetAllNodes();
		getGraphManager().resetAllNodesToApplyGravitation();
		getGraphManager().resetAllEdges();
	}

	private void adjustLocation(SbgnPDNode comp, LGraph chGr)
	{
		RectangleD rect = calculateBounds(false,
				(ArrayList<SbgnPDNode>) chGr.getNodes());

		int differenceX = (int) (rect.x - comp.getLeft());
		int differenceY = (int) (rect.y - comp.getTop());
		
		// if the parent graph is a compound, add compound margins
		if(!comp.type.equals(SbgnPDConstants.COMPLEX))
		{			
			differenceX -= LayoutConstants.COMPOUND_NODE_MARGIN;
			differenceY -= LayoutConstants.COMPOUND_NODE_MARGIN;		
		}
		
		for (int j = 0; j < chGr.getNodes().size(); j++)
		{
			SbgnPDNode s = (SbgnPDNode) chGr.getNodes().get(j);
						
			s.setLocation(s.getLeft() - differenceX
					+ SbgnPDConstants.COMPLEX_MEM_HORIZONTAL_BUFFER, s.getTop()
					- differenceY + SbgnPDConstants.COMPLEX_MEM_VERTICAL_BUFFER);

			if (s.getChild() != null)
				adjustLocation(s, s.getChild());
		}
	}

	/**
	 * Dummy complexes (placed in the "dummyComplexList") are removed from the
	 * graph.
	 */
	private void removeDummyComplexes()
	{
		// remove dummy complexes and connect children to original parent
		for (SbgnPDNode dummyComplex : dummyComplexList)
		{
			LGraph childGraph = dummyComplex.getChild();
			LGraph owner = dummyComplex.getOwner();

			// this.graphManager.add(childGraph, owner.getParent());

			getGraphManager().getGraphs().remove(childGraph);
			dummyComplex.setChild(null);

			owner.remove(dummyComplex);

			for (Object s : childGraph.getNodes())
				owner.add((SbgnPDNode) s);
		}
	}

	/**
	 * This method returns the bounding rectangle of the given set of nodes with
	 * or without the margins
	 * 
	 * @return
	 */
	protected RectangleD calculateBounds(boolean isMarginIncluded,
			ArrayList<SbgnPDNode> nodes)
	{
		int boundLeft = Integer.MAX_VALUE;
		int boundRight = Integer.MIN_VALUE;
		int boundTop = Integer.MAX_VALUE;
		int boundBottom = Integer.MIN_VALUE;
		int nodeLeft;
		int nodeRight;
		int nodeTop;
		int nodeBottom;

		Iterator<SbgnPDNode> itr = nodes.iterator();

		while (itr.hasNext())
		{
			LNode lNode = itr.next();
			nodeLeft = (int) (lNode.getLeft());
			nodeRight = (int) (lNode.getRight());
			nodeTop = (int) (lNode.getTop());
			nodeBottom = (int) (lNode.getBottom());

			if (boundLeft > nodeLeft)
				boundLeft = nodeLeft;

			if (boundRight < nodeRight)
				boundRight = nodeRight;

			if (boundTop > nodeTop)
				boundTop = nodeTop;

			if (boundBottom < nodeBottom)
				boundBottom = nodeBottom;
		}

		if (isMarginIncluded)
		{
			return new RectangleD(boundLeft
					- SbgnPDConstants.COMPLEX_MEM_MARGIN, boundTop
					- SbgnPDConstants.COMPLEX_MEM_MARGIN, boundRight
					- boundLeft + 2 * SbgnPDConstants.COMPLEX_MEM_MARGIN,
					boundBottom - boundTop + 2
							* SbgnPDConstants.COMPLEX_MEM_MARGIN);
		}
		else
		{
			return new RectangleD(boundLeft, boundTop, boundRight - boundLeft,
					boundBottom - boundTop);
		}
	}

	/**
	 * calculates usedArea/totalArea inside the complexes and prints them out.
	 */
	@SuppressWarnings("unused")
	protected void calculateFullnessOfComplexes()
	{
		SbgnPDNode largestComplex = null;
		double totalArea = 0;
		double usedArea = 0;
		double maxArea = Double.MIN_VALUE;

		// find the largest complex -> area
		for (int i = 0; i < getAllNodes().length; i++)
		{
			SbgnPDNode s = (SbgnPDNode) getAllNodes()[i];
			if (s.type.equals(SbgnPDConstants.COMPLEX)
					&& s.getWidth() * s.getHeight() > maxArea)
			{
				maxArea = s.getWidth() * s.getHeight();
				largestComplex = s;
			}
		}

		usedArea = calculateUsedArea(largestComplex);

		totalArea = largestComplex.getWidth() * largestComplex.getHeight();

		if (compactionMethod == DefaultCompactionAlgorithm.TILING)
			System.out.println("Tiling results");
		else if (compactionMethod == DefaultCompactionAlgorithm.POLYOMINO_PACKING)
			System.out.println("Polyomino Packing results");

		System.out.println(" = " + usedArea / totalArea);
	}

	/**
	 * This method calculates the used area of a given complex node's children
	 */
	protected double calculateUsedArea(SbgnPDNode parent)
	{
		int totalArea = 0;
		if (parent.getChild() == null)
			return 0.0;

		for (int i = 0; i < parent.getChild().getNodes().size(); i++)
		{
			SbgnPDNode node = (SbgnPDNode) parent.getChild().getNodes().get(i);

			if (!node.type.equalsIgnoreCase(SbgnPDConstants.COMPLEX))
			{
				totalArea += node.getWidth() * node.getHeight();
			}
			else
			{
				totalArea += calculateUsedArea(node);
			}
		}
		return totalArea;
	}

	public enum DefaultCompactionAlgorithm
	{
		TILING, POLYOMINO_PACKING
	};

	// ********************* SECTION : OVERRIDEN METHODS *********************

	/**
	 * This method creates a new node associated with the input view node.
	 */
	public LNode newNode(Object vNode)
	{
		return new SbgnPDNode(this.graphManager, vNode);
	}

	/**
	 * This method creates a new edge associated with the input view edge.
	 */
	public LEdge newEdge(Object vEdge)
	{
		return new SbgnPDEdge(null, null, vEdge);
	}

	/**
	 * This method performs layout on constructed l-level graph. It returns true
	 * on success, false otherwise.
	 */
	public boolean layout()
	{
		boolean b = false;

		groupZeroDegreeMembers();
		applyDFSOnComplexes();
		b = super.layout();
		repopulateComplexes();

		this.getAllNodes();
		return b;
	}

	/**
	 * This method uses classic layout method (without multi-scaling)
	 * Modification: create port nodes after random positioning
	 */
	@Override
	protected boolean classicLayout()
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

		if (!arePortNodesCreated())
		{
			createPortNodes();
			resetNodesToApplyGravitation();
		}
		this.initSpringEmbedder();
		this.runSpringEmbedder();

		return true;
	}

	@Override
	/**
	 * This method calculates the spring forces for the ends of each node.
	 * Modification: do not calculate spring force for rigid edges
	 */
	public void calcSpringForces()
	{
		Object[] lEdges = this.getAllEdges();
		FDLayoutEdge edge;

		for (int i = 0; i < lEdges.length; i++)
		{
			edge = (FDLayoutEdge) lEdges[i];

			if (!edge.type.equals(SbgnPDConstants.RIGID_EDGE))
				this.calcSpringForce(edge, edge.idealLength);
		}
	}

	@Override
	/**	 
	 * This method calculates the repulsion forces for each pair of nodes.
	 * Modification: Do not calculate repulsion for port & process nodes
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

			if (this.totalIterations
					% FDLayoutConstants.GRID_CALCULATION_CHECK_PERIOD == 1)
			{
				this.grid = this.calcGrid(this.graphManager.getRoot());

				// put all nodes to proper grid cells
				for (i = 0; i < lNodes.length; i++)
				{
					nodeA = (FDLayoutNode) lNodes[i];
					this.addNodeToGrid(nodeA, this.grid, this.graphManager
							.getRoot().getLeft(), this.graphManager.getRoot()
							.getTop());
				}
			}

			processedNodeSet = new HashSet<FDLayoutNode>();

			// calculate repulsion forces between each nodes and its surrounding
			for (i = 0; i < lNodes.length; i++)
			{
				nodeA = (FDLayoutNode) lNodes[i];
				this.calculateRepulsionForceOfANode(this.grid, nodeA,
						processedNodeSet);
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

					if (nodeA.type != null
							&& nodeB.type != null
							&& nodeA.getOwner().equals(nodeB.getOwner())
							&& (nodeA.type.equals(SbgnPDConstants.INPUT_PORT)
									|| nodeA.type
											.equals(SbgnPDConstants.OUTPUT_PORT)
									|| nodeB.type
											.equals(SbgnPDConstants.INPUT_PORT) || nodeB.type
										.equals(SbgnPDConstants.OUTPUT_PORT)))
					{
						continue;
					}

					this.calcRepulsionForce(nodeA, nodeB);
				}
			}
		}
	}

	@Override
	/**
	 * This method finds surrounding nodes of nodeA in repulsion range.
	 * And calculates the repulsion forces between nodeA and its surrounding.
	 * During the calculation, ignores the nodes that have already been processed.
	 * Modification: Do not calculate repulsion for port & process nodes
	 */
	protected void calculateRepulsionForceOfANode(Vector[][] grid,
			FDLayoutNode nodeA, HashSet<FDLayoutNode> processedNodeSet)
	{
		int i, j;

		if (this.totalIterations
				% FDLayoutConstants.GRID_CALCULATION_CHECK_PERIOD == 1)
		{
			HashSet<Object> surrounding = new HashSet<Object>();
			FDLayoutNode nodeB;

			for (i = (nodeA.startX - 1); i < (nodeA.finishX + 2); i++)
			{
				for (j = (nodeA.startY - 1); j < (nodeA.finishY + 2); j++)
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

							if (nodeA.type != null
									&& nodeB.type != null
									&& nodeA.getOwner()
											.equals(nodeB.getOwner())
									&& (nodeA.type
											.equals(SbgnPDConstants.INPUT_PORT)
											|| nodeA.type
													.equals(SbgnPDConstants.OUTPUT_PORT)
											|| nodeB.type
													.equals(SbgnPDConstants.INPUT_PORT) || nodeB.type
												.equals(SbgnPDConstants.OUTPUT_PORT)))
							{
								continue;
							}

							// check if the repulsion force between
							// nodeA and nodeB has already been calculated
							if (!processedNodeSet.contains(nodeB)
									&& !surrounding.contains(nodeB))
							{
								double distanceX = Math.abs(nodeA.getCenterX()
										- nodeB.getCenterX())
										- ((nodeA.getWidth() / 2) + (nodeB
												.getWidth() / 2));
								double distanceY = Math.abs(nodeA.getCenterY()
										- nodeB.getCenterY())
										- ((nodeA.getHeight() / 2) + (nodeB
												.getHeight() / 2));

								// if the distance between nodeA and nodeB
								// is less then calculation range
								if ((distanceX <= this.repulsionRange)
										&& (distanceY <= this.repulsionRange))
								{
									// then add nodeB to surrounding of nodeA
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

	@Override
	public void moveNodes()
	{
		ArrayList<SbgnProcessNode> processNodesToBeRotated = new ArrayList<SbgnProcessNode>();
		properlyOrientedEdgeCount = 0;
		totalEdgeCount = 0;

		// compound and two dummy nodes (input/output ports)
		SbgnPDNode c;
		SbgnPDNode d1;
		SbgnPDNode d2;

		for (Object o : this.getAllNodes())
		{
			if (o instanceof SbgnProcessNode)
			{
				SbgnProcessNode p = (SbgnProcessNode) o;
				c = p.parentCompound;
				d1 = p.getInputPort();
				d2 = p.getOutputPort();

				properlyOrientedEdgeCount += p
						.calculateRotationalForces(idealEdgeLength);
				totalEdgeCount += (p.inputNeighborNodeList.size() + p.outputNeighborNodeList
						.size());
				p.transferForces();

				p.resetForces();
				d1.resetForces();
				d2.resetForces();
			}
		}

		// each time, try to rotate one process compound that wants to rotate
		if (this.totalIterations
				% SbgnPDConstants.ROTATIONAL_FORCE_ITERATION_COUNT == 0
				&& this.phaseNumber == 2)
		{
			boolean rotationAvailability = false;

			for (Object o : this.getAllNodes())
			{
				if (o instanceof SbgnProcessNode)
				{
					rotationAvailability = ((SbgnProcessNode) o)
							.checkRotationAvailability();

					if (rotationAvailability)
						processNodesToBeRotated.add((SbgnProcessNode) o);
				}
			}

			if (processNodesToBeRotated.size() > 0)
			{
				int randomNumber = (int) (Math.random() * processNodesToBeRotated
						.size());
				SbgnProcessNode p = processNodesToBeRotated.get(randomNumber);
				p.applyRotation();
			}
		}

		super.moveNodes();
	}

	/**
	 * @Override This method performs the actual layout on the l-level compound
	 *           graph. An update() needs to be called for changes to be
	 *           propagated to the v-level compound graph.
	 */
	public void runSpringEmbedder()
	{
		this.phaseNumber = 1;
		doPhase1();

		this.phaseNumber = 2;
		doPhase2();

		 removeDummyCompounds();
	}
}
