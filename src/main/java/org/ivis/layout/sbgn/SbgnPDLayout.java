package org.ivis.layout.sbgn;

import java.util.*;

import org.ivis.layout.LEdge;
import org.ivis.layout.LGraph;
import org.ivis.layout.LNode;
import org.ivis.layout.cose.CoSELayout;
import org.ivis.layout.fd.FDLayoutConstants;

/**
 * This class implements the layout process of SBGN notation.
 * 
 * @author Begum Genc
 * 
 *         Copyright: i-Vis Research Group, Bilkent University, 2007 - present
 */
public class SbgnPDLayout extends CoSELayout
{

	/**
	 * For remembering contents of a complex.
	 */
	Map<SbgnPDNode, LGraph> childGraphMap;
	Map<SbgnPDNode, MemberPack> memberPackMap;

	/**
	 * This list stores the complex molecules in a depth first search manner.
	 * The first element corresponds to the deep-most node.
	 */
	LinkedList<SbgnPDNode> complexOrder;

	/**
	 * The constructor creates and associates with this layout a new graph
	 * manager as well.
	 */
	public SbgnPDLayout()
	{
		childGraphMap = new HashMap<SbgnPDNode, LGraph>();
		memberPackMap = new HashMap<SbgnPDNode, MemberPack>();
		complexOrder = new LinkedList<SbgnPDNode>();
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
		clearComplexes();
		boolean b = super.layout();
		repopulateComplexes();

		return b;
	}

	/**
	 * This method is for orienting Ps and Ss
	 */
	private void calcRelativityConstraintForces()
	{
		for (int i = 0; i < this.getAllNodes().length; i++)
		{
			SbgnPDNode processNode = (SbgnPDNode) this.getAllNodes()[i];

			if (processNode.type.equals(SbgnPDConstants.PROCESS))
				continue;

			Iterator itr = processNode.getEdges().iterator();
			while (itr.hasNext())
			{
				SbgnPDEdge edge = (SbgnPDEdge) itr.next();

				SbgnPDNode otherEnd = (SbgnPDNode) (edge
						.getOtherEnd(processNode));
				double orientationX = otherEnd.orientationX;
				double orientationY = otherEnd.orientationY;
				double orientation = Math.sqrt(orientationX * orientationX
						+ orientationY * orientationY);

				// Here we have substrates defined by the consumption type of
				// the edges.
				if (edge.type.equals(SbgnPDConstants.CONSUMPTION))
				{
					// As this is a substrate the orientation is inverse
					orientationX = -orientationX;
					orientationY = -orientationY;
				}

				// else it is

				// This is the point we target for this node
				double orientationTargetX = orientationX * this.idealEdgeLength
						* 1.5 + otherEnd.getCenterX();
				double orientationTargetY = orientationY * this.idealEdgeLength
						* 1.5 + otherEnd.getCenterY();

				// This is the vector that heads for that point
				double distanceX = orientationTargetX
						- processNode.getCenterX();
				double distanceY = orientationTargetY
						- processNode.getCenterY();
				double distance = Math.sqrt(distanceX * distanceX + distanceY
						* distanceY);
				double forceX = (distance * distanceX)
						* SbgnPDConstants.RELATIVITY_CONSTRAINT_CONSTANT
						/ distance;
				double forceY = (distance * distanceY)
						* SbgnPDConstants.RELATIVITY_CONSTRAINT_CONSTANT
						/ distance;

				// Are force too big or too small?

				if (forceX > 10)
				{
					forceX = 10;
				}
				else if (forceX < -10)
				{
					forceX = -10;
				}

				if (forceY > 10)
				{
					forceY = 10;
				}
				else if (forceY < -10)
				{
					forceY = -10;
				}

				processNode.relativityConstraintX += forceX;
				processNode.relativityConstraintY += forceY;
			}
		}
	}

	/**
	 * @Override This method performs the actual layout on the l-level compound
	 *           graph. An update() needs to be called for changes to be
	 *           propogated to the v-level compound graph.
	 */
	public void runSpringEmbedder()
	{
		// if (!this.incremental)
		// {
		// CoSELayout.randomizedMovementCount = 0;
		// CoSELayout.nonRandomizedMovementCount = 0;
		// }

		// this.updateAnnealingProbability();

		do
		{
			this.totalIterations++;

			if (this.totalIterations
					% FDLayoutConstants.CONVERGENCE_CHECK_PERIOD == 0)
			{
				if (this.isConverged())
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
			this.calcRelativityConstraintForces();
			this.moveNodes();
			this.updateNodeOrientations();

			this.animate();
		}
		while (this.totalIterations < this.maxIterations);

		this.graphManager.updateBounds();
	}

	/**
	 * This method is for updating orientatiıons of nodes
	 */
	private void updateNodeOrientations()
	{
		for (int i = 0; i < this.getAllNodes().length; i++)
		{
			SbgnPDNode node = (SbgnPDNode) this.getAllNodes()[i];
			node.updateOrientation();
		}
	}

	/**
	 * This method searched unmarked complex nodes recursively, because they may
	 * contain complex children. After the order is found, child graphs of each
	 * complex node are cleared.
	 */
	public void clearComplexes()
	{
		for (Object o : getAllNodes())
		{
			if (!(o instanceof SbgnPDNode) || !((SbgnPDNode) o).isComplex())
				continue;

			SbgnPDNode comp = (SbgnPDNode) o;

			// complex is found, recurse on it.
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
	 * @param comp
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

	private void clearComplex(SbgnPDNode comp)
	{
		LGraph childGr = comp.getChild();

		childGraphMap.put(comp, childGr);
		MemberPack pack = new MemberPack(childGr);
		memberPackMap.put(comp, pack);

		// for debug purposes, print the complex and its children
		// System.out.println(comp);
		// System.out.println("children");
		// for (Object n : childGr.getNodes())
		// {
		// SbgnPDNode n2 = (SbgnPDNode) n;
		// System.out.println(n2);
		// }
		// System.out.println();

		getGraphManager().getGraphs().remove(childGr);
		comp.setChild(null);

		comp.setWidth(pack.getWidth());
		comp.setHeight(pack.getHeight());

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
	 * Reassigns the complex content. The outermost complex is placed first.
	 */
	protected void repopulateComplexes()
	{
		for (int i = complexOrder.size() - 1; i >= 0; i--)
		{
			SbgnPDNode comp = complexOrder.get(i);
			LGraph chGr = childGraphMap.get(comp);

			comp.setChild(chGr);
			getGraphManager().getGraphs().add(chGr);
			MemberPack pack = memberPackMap.get(comp);
			pack.adjustLocations(comp.getLeft(), comp.getTop());

		}
		getGraphManager().resetAllNodes();
		getGraphManager().resetAllNodesToApplyGravitation();
		getGraphManager().resetAllEdges();
	}

	protected class Organization
	{
		private double width;
		private double height;

		private List<Double> rowWidth;
		private List<LinkedList<SbgnPDNode>> rows;

		public Organization()
		{
			this.width = SbgnPDConstants.COMPLEX_MEM_MARGIN * 2;
			this.height = (SbgnPDConstants.COMPLEX_MEM_MARGIN * 2);

			rowWidth = new ArrayList<Double>();
			rows = new ArrayList<LinkedList<SbgnPDNode>>();
		}

		public double getWidth()
		{
			shiftToLastRow();
			return width;
		}

		public double getHeight()
		{
			return height;
		}

		private int getShortestRowIndex()
		{
			int r = -1;
			double min = Double.MAX_VALUE;

			for (int i = 0; i < rows.size(); i++)
			{
				if (rowWidth.get(i) < min)
				{
					r = i;
					min = rowWidth.get(i);
				}
			}

			return r;
		}

		private int getLongestRowIndex()
		{
			int r = -1;
			double max = Double.MIN_VALUE;

			for (int i = 0; i < rows.size(); i++)
			{
				if (rowWidth.get(i) > max)
				{
					r = i;
					max = rowWidth.get(i);
				}
			}

			return r;
		}

		public void insertNode(SbgnPDNode node)
		{
			if (rows.isEmpty())
			{
				insertNodeToRow(node, 0);
			}
			else if (canAddHorizontal(node.getWidth(), node.getHeight()))
			{
				insertNodeToRow(node, getShortestRowIndex());
			}
			else
			{
				insertNodeToRow(node, rows.size());
			}
		}

		private void insertNodeToRow(SbgnPDNode node, int rowIndex)
		{
			// Add new row if needed

			if (rowIndex == rows.size())
			{
				if (!rows.isEmpty())
				{
					height += SbgnPDConstants.COMPLEX_MEM_VERTICAL_BUFFER;
				}
				rows.add(new LinkedList<SbgnPDNode>());
				height += node.getHeight();
				rowWidth.add(SbgnPDConstants.COMPLEX_MIN_WIDTH);

				assert rows.size() == rowWidth.size();
			}

			// Update row width
			double w = rowWidth.get(rowIndex) + node.getWidth();
			if (!rows.get(rowIndex).isEmpty())
				w += SbgnPDConstants.COMPLEX_MEM_HORIZONTAL_BUFFER;
			rowWidth.set(rowIndex, w);

			// Insert node
			rows.get(rowIndex).add(node);

			// Update complex width
			if (width < w)
			{
				width = w;
			}
		}

		private void shiftToLastRow()
		{
			int longest = getLongestRowIndex();
			int last = rowWidth.size() - 1;
			LinkedList<SbgnPDNode> row = rows.get(longest);
			SbgnPDNode node = row.getLast();
			double diff = node.getWidth()
					+ SbgnPDConstants.COMPLEX_MEM_HORIZONTAL_BUFFER;

			if (width - rowWidth.get(last) > diff)
			{
				row.removeLast();
				rows.get(last).add(node);
				rowWidth.set(longest, rowWidth.get(longest) - diff);
				rowWidth.set(last, rowWidth.get(last) + diff);

				width = rowWidth.get(getLongestRowIndex());

				shiftToLastRow();
			}
		}

		private boolean canAddHorizontal(double extraWidth, double extraHeight)
		{
			int sri = getShortestRowIndex();

			if (sri < 0)
				return true;

			double min = rowWidth.get(sri);

			if (width - min >= extraWidth
					+ SbgnPDConstants.COMPLEX_MEM_HORIZONTAL_BUFFER)
			{
				return true;
			}

			return height + SbgnPDConstants.COMPLEX_MEM_VERTICAL_BUFFER
					+ extraHeight > min + extraWidth
					+ SbgnPDConstants.COMPLEX_MEM_HORIZONTAL_BUFFER;
		}

		public void adjustLocations(double x, double y)
		{
			x += SbgnPDConstants.COMPLEX_MEM_MARGIN;
			y += SbgnPDConstants.COMPLEX_MEM_MARGIN;

			double left = x;

			for (LinkedList<SbgnPDNode> row : rows)
			{
				x = left;
				double maxHeight = 0;
				for (SbgnPDNode node : row)
				{
					node.setLocation(x, y);
					x += node.getWidth()
							+ SbgnPDConstants.COMPLEX_MEM_HORIZONTAL_BUFFER;

					if (node.getHeight() > maxHeight)
						maxHeight = node.getHeight();
				}

				y += maxHeight + SbgnPDConstants.COMPLEX_MEM_VERTICAL_BUFFER;
			}
		}
	}

	protected class MemberPack
	{
		private List<SbgnPDNode> members;
		private Organization org;

		public MemberPack(LGraph childG)
		{
			members = new ArrayList<SbgnPDNode>();
			members.addAll(childG.getNodes());
			org = new Organization();

			layout();
		}

		public void layout()
		{
			ComparableNode[] compar = new ComparableNode[members.size()];

			int i = 0;
			for (SbgnPDNode node : members)
			{
				compar[i++] = new ComparableNode(node);
			}

			Arrays.sort(compar);

			members.clear();
			for (ComparableNode com : compar)
			{
				members.add(com.getNode());
			}

			for (SbgnPDNode node : members)
			{
				org.insertNode(node);
			}
		}

		public double getWidth()
		{
			return org.getWidth();
		}

		public double getHeight()
		{
			return org.getHeight();
		}

		public void adjustLocations(double x, double y)
		{
			org.adjustLocations(x, y);
		}
	}

	protected class ComparableNode implements Comparable
	{
		private SbgnPDNode node;

		public ComparableNode(SbgnPDNode node)
		{
			this.node = node;
		}

		public SbgnPDNode getNode()
		{
			return node;
		}

		/**
		 * Inverse compare function to order descending.
		 */
		public int compareTo(Object o)
		{
			return (new Double(((ComparableNode) o).getNode().getWidth()))
					.compareTo(node.getWidth());
		}
	}

}
