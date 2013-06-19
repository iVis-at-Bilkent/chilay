package org.ivis.layout.sbgn;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.ivis.layout.LEdge;
import org.ivis.layout.LGraph;
import org.ivis.layout.LNode;
import org.ivis.layout.cose.CoSELayout;
import org.ivis.layout.util.MemberPack;
import org.ivis.layout.util.RectProc;

/**
 * This class implements the layout process of SBGN notation.
 * 
 * @author Begum Genc
 * 
 *         Copyright: i-Vis Research Group, Bilkent University, 2007 - present
 */
public class SbgnPDLayout extends CoSELayout
{
	// VARIABLES SECTION

	/**
	 * For remembering contents of a complex.
	 */
	Map<SbgnPDNode, LGraph> childGraphMap;

	/**
	 * Used during Tiling.
	 */
	Map<SbgnPDNode, MemberPack> memberPackMap;

	/**
	 * This list stores the complex molecules in a depth first search manner.
	 * The first element corresponds to the deep-most node.
	 */
	LinkedList<SbgnPDNode> complexOrder;

	/**
	 * This parameter indicates the chosen tiling method. It is set to Polyomino
	 * Packing by default.
	 */
	public int tilingMethod;

	// METHODS SECTION

	/**
	 * The constructor creates and associates with this layout a new graph
	 * manager as well. No tiling performs CoSE Layout.
	 * 
	 * @param tilingMethod
	 *            - SbgnPDConstants.TILING, SbgnPDConstants.POLYOMINO_PACKING or
	 *            SbgnPDConstants.NO_TILING
	 */
	public SbgnPDLayout(int tilingMethod)
	{
		childGraphMap = new HashMap<SbgnPDNode, LGraph>();
		complexOrder = new LinkedList<SbgnPDNode>();
		this.tilingMethod = tilingMethod;
		if (tilingMethod == SbgnPDConstants.TILING)
			memberPackMap = new HashMap<SbgnPDNode, MemberPack>();
	}

	/**
	 * This method creates a new node associated with the input view node.
	 */
	public LNode newNode(Object vNode)
	{
		LNode o = (LNode) vNode;
		return new SbgnPDNode(this.graphManager, o);
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
		System.out.println("SbgnPDLayout runs..");

		if (tilingMethod != SbgnPDConstants.NO_TILING)
		{
			complexSearch();
			System.out.println("Complexes are cleared.");
		}

		boolean b = super.layout();

		if (tilingMethod != SbgnPDConstants.NO_TILING)
		{
			repopulateComplexes();
			System.out.println("Complexes are repopulated.");
		}

		System.out.println("Finished SbgnPDLayout.\n");
		return b;
	}

	/**
	 * This method searched unmarked complex nodes recursively, because they may
	 * contain complex children. After the order is found, child graphs of each
	 * complex node are cleared.
	 */
	public void complexSearch()
	{
		for (Object o : getAllNodes())
		{
			if (!(o instanceof SbgnPDNode) || !((SbgnPDNode) o).isComplex())
				continue;

			SbgnPDNode comp = (SbgnPDNode) o;

			// complex is found, recurse on it until no visited complex remains.
			if (!comp.marked)
				recurseOnComplex(comp);
		}

		// clear each complex
		for (SbgnPDNode o : complexOrder)
			clearComplex(o);

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
	public void recurseOnComplex(SbgnPDNode node)
	{
		if (node.getChild() != null)
		{
			for (Object n : node.getChild().getNodes())
			{
				SbgnPDNode sbgnChild = (SbgnPDNode) n;
				recurseOnComplex(sbgnChild);
			}
		}

		if (node.type.equals(SbgnPDConstants.COMPLEX)
				&& !containsUnmarkedComplex(node))
		{
			complexOrder.add(node);
			node.marked = true;
			return;
		}
	}

	/**
	 * This method checks if the given node contains any unmarked complex nodes
	 * in its child graph.
	 * 
	 * @return true - if there are unmarked complex nodes false - otherwise
	 */
	public boolean containsUnmarkedComplex(SbgnPDNode comp)
	{
		for (Object child : comp.getChild().getNodes())
		{
			SbgnPDNode sbgnChild = (SbgnPDNode) child;

			if (sbgnChild.type.equals(SbgnPDConstants.COMPLEX)
					&& !sbgnChild.marked)
				return true;
		}
		return false;
	}

	/**
	 * This method applies polyomino packing on the child graph of a complex
	 * member and then..
	 * 
	 * @param comp
	 */
	private void clearComplex(SbgnPDNode comp)
	{
		LGraph childGr = comp.getChild();
		childGraphMap.put(comp, childGr);
		MemberPack pack = null;
		if (tilingMethod == SbgnPDConstants.POLYOMINO_PACKING)
		{
			applyPolyomino(comp, childGr);
		}
		else if (tilingMethod == SbgnPDConstants.TILING)
		{
			pack = new MemberPack(childGr);
			memberPackMap.put(comp, pack);

		}

		getGraphManager().getGraphs().remove(childGr);
		comp.setChild(null);

		if (tilingMethod == SbgnPDConstants.TILING)
		{
			comp.setWidth(pack.getWidth());
			comp.setHeight(pack.getHeight());
		}

		// Redirect the edges of complex members to the complex.
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

	/**
	 * This method tiles the given list of nodes by using polyomino packing
	 * algorithm.
	 */
	private void applyPolyomino(SbgnPDNode parent, LGraph graph)
	{
		SbgnPDNode[] mpArray = new SbgnPDNode[graph.getNodes().size()];

		for (int i = 0; i < graph.getNodes().size(); i++)
		{
			SbgnPDNode s = (SbgnPDNode) graph.getNodes().get(i);
			mpArray[i] = s;
		}

		Rectangle r = RectProc.packRectanglesMino(1, mpArray.length, mpArray);

		parent.setWidth(r.getWidth() + 2 * SbgnPDConstants.COMPLEX_MEM_MARGIN);
		parent.setHeight(r.getHeight() + 2 * SbgnPDConstants.COMPLEX_MEM_MARGIN);
	}

	/**
	 * Reassigns the complex content. The outermost complex is placed first.
	 */
	protected void repopulateComplexes()
	{
		for (int i = complexOrder.size() - 1; i >= 0; i--)
		{
			SbgnPDNode comp = complexOrder.get(i);
			LGraph chGr = childGraphMap.get(comp);

			comp.setChild(chGr);

			if (tilingMethod == SbgnPDConstants.POLYOMINO_PACKING)
			{
				Rectangle rect = LGraph.calculateBounds(chGr.getNodes());
				int differenceX = (int) (rect.x - comp.getLeft());
				int differenceY = (int) (rect.y - comp.getTop());

				for (int j = 0; j < chGr.getNodes().size(); j++)
				{
					SbgnPDNode s = (SbgnPDNode) chGr.getNodes().get(j);
					s.setLocation(s.getLeft() - differenceX
							+ SbgnPDConstants.COMPLEX_MEM_MARGIN, s.getTop()
							- differenceY + SbgnPDConstants.COMPLEX_MEM_MARGIN);
				}
				getGraphManager().getGraphs().add(chGr);
			}
			else if (tilingMethod == SbgnPDConstants.TILING)
			{
				getGraphManager().getGraphs().add(chGr);

				MemberPack pack = memberPackMap.get(comp);
				pack.adjustLocations(comp.getLeft(), comp.getTop());
			}
		}
		getGraphManager().resetAllNodes();
		getGraphManager().resetAllNodesToApplyGravitation();
		getGraphManager().resetAllEdges();
	}
}
