package org.ivis.layout.sbgn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.ivis.layout.LEdge;
import org.ivis.layout.LGraph;
import org.ivis.layout.LNode;
import org.ivis.layout.cose.CoSELayout;

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
	 * The constructor creates and associates with this layout a new graph
	 * manager as well.
	 */
	public SbgnPDLayout()
	{
        childGraphMap = new HashMap<SbgnPDNode, LGraph>();
        memberPackMap = new HashMap<SbgnPDNode, MemberPack>();
        
	}

	/**
	 * This method creates a new node associated with the input view node.
	 */
	public LNode newNode(Object vNode)
	{
		return new SbgnPDNode(this.graphManager, vNode, SbgnPDConstants.COMPLEX);
	}

	/**
	 * This method creates a new edge associated with the input view edge.
	 */
	public LEdge newEdge(Object vEdge)
	{
		return new SbgnPDEdge(null, null, vEdge, "");
		//TODO set edge type
	}
	
	/**
	 * This method performs layout on constructed l-level graph. It returns true
	 * on success, false otherwise.
	 */
	public boolean layout()
	{
		System.out.println("SbgnPD Layout is running now...");
		
        clearComplexes();
        boolean b = super.layout();        
        repopulateComplexes();
        
        return b;
	}
	
	@Override
	public void moveNodes()
	{
		super.moveNodes();
	}
	
	@Override
	public void calcSpringForces()
	{
		super.calcSpringForces();
	}
	
	// REMANINING CODE IS FOR TILING
	private void clearComplexes()
	{
        for (Object o : getAllNodes())
        {
       	
                if (!(o instanceof SbgnPDNode) || !((SbgnPDNode) o).isComplex()) continue;
                SbgnPDNode comp = (SbgnPDNode) o;

                if (comp.getChild().getNodes().isEmpty()) continue;
                
                LGraph childGr = comp.getChild();
                childGraphMap.put(comp, childGr);
                MemberPack pack = new MemberPack(childGr);
                memberPackMap.put(comp, pack);
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
        getGraphManager().resetAllNodes();
        getGraphManager().resetAllNodesToApplyGravitation();
        getGraphManager().resetAllEdges();
        
        System.out.println("complexes cleared.");

	}

	
    /**
     * Reassigns the complex content.
     */
    protected void repopulateComplexes()
    {
            for (SbgnPDNode comp : childGraphMap.keySet())
            {
                    LGraph chGr = childGraphMap.get(comp);
                    comp.setChild(chGr);
                    getGraphManager().getGraphs().add(chGr);
                    MemberPack pack = memberPackMap.get(comp);
                    pack.adjustLocations(comp.getLeft(), comp.getTop());
            }
            getGraphManager().resetAllNodes();
            getGraphManager().resetAllNodesToApplyGravitation();
            getGraphManager().resetAllEdges();
            System.out.println("complexes repopulated.");
    }

    protected class Organization
    {
            private double width;
            private double height;

            private List<Double> rowWidth;
            private List<LinkedList<SbgnPDNode>> rows;

            public Organization()
            {
                    this.width = COMPLEX_CHILD_GRAPH_BUFFER * 2;
                    this.height = (COMPLEX_CHILD_GRAPH_BUFFER * 2) + COMPLEX_LABEL_HEIGHT;

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
            	//****** removed the info part*****	 
                    return height + SbgnPDConstants.DEFAULT_INFO_BULB +
                            SbgnPDConstants.DEFAULT_INFO_BULB;
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
                                    height += COMPLEX_MEM_VERTICAL_BUFFER;
                            }
                            rows.add(new LinkedList<SbgnPDNode>());
                            height += node.getHeight();
                            rowWidth.add(COMPLEX_MIN_WIDTH);

                            assert rows.size() == rowWidth.size();
                    }

                    // Update row width

                    double w = rowWidth.get(rowIndex) + node.getWidth();
                    if (!rows.get(rowIndex).isEmpty()) w += COMPLEX_MEM_HORIZONTAL_BUFFER;
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
                    double diff = node.getWidth() + COMPLEX_MEM_HORIZONTAL_BUFFER;

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

                    if (sri < 0) return true;

                    double min = rowWidth.get(sri);

                    if (width - min >= extraWidth + COMPLEX_MEM_HORIZONTAL_BUFFER)
                    {
                            return true;
                    }

                    return width < DESIRED_COMPLEX_MIN_WIDTH ||
                            height + COMPLEX_MEM_VERTICAL_BUFFER + extraHeight >
                                    min + extraWidth + COMPLEX_MEM_HORIZONTAL_BUFFER;
            }

            //******* removed the info part ********
            
            public void adjustLocations(double x, double y)
            {
                    x += COMPLEX_CHILD_GRAPH_BUFFER;
                    y += COMPLEX_CHILD_GRAPH_BUFFER + SbgnPDConstants.DEFAULT_INFO_BULB;

                    double left = x;

                    for (LinkedList<SbgnPDNode> row : rows)
                    {
                            x = left;
                            double maxHeight = 0;
                            for (SbgnPDNode node : row)
                            {
                                    double yy = node.getHeight() - 0.0001 > SbgnPDConstants.DEFAULT_HEIGHT ?
                                            y - SbgnPDConstants.DEFAULT_INFO_BULB : y;

                                    node.setLocation(x, yy);
                                    x += node.getWidth() + COMPLEX_MEM_HORIZONTAL_BUFFER;
                                    
                                    // This check has been added to get rid of the use of default_height parameter
                                    if(node.getHeight() > maxHeight)
                                    	maxHeight = node.getHeight();
                            }

                            y += maxHeight + COMPLEX_MEM_VERTICAL_BUFFER;
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
                    return (new Double(((ComparableNode) o).getNode().getWidth())).
                            compareTo(node.getWidth());
            }
    }

    private static final int DESIRED_COMPLEX_MIN_WIDTH = 100;
    private static final int COMPLEX_MEM_HORIZONTAL_BUFFER = 5;
    private static final int COMPLEX_MEM_VERTICAL_BUFFER = 5;
    private static final double COMPLEX_CHILD_GRAPH_BUFFER = 10;
    private static final double COMPLEX_LABEL_HEIGHT = 20;
    private static final double COMPLEX_MIN_WIDTH = COMPLEX_CHILD_GRAPH_BUFFER * 2;

}
