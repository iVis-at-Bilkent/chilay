package org.ivis.io.xml;

import java.applet.Applet;
import java.awt.BasicStroke;
import java.awt.Button;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import org.ivis.layout.LGraph;
import org.ivis.layout.Layout;
import org.ivis.layout.sbgn.SbgnPDConstants;
import org.ivis.layout.sbgn.SbgnPDEdge;
import org.ivis.layout.sbgn.SbgnPDLayout;
import org.ivis.layout.sbgn.SbgnPDNode;
import org.ivis.util.IGeometry;

/**
 * This class is used to test the results of sbgnpd layout algorithm with two
 * tiling methods: polyomino packing and basic tiling. The applet only displays
 * the bounding boxes of the nodes and the edges.
 * 
 * @author Begum Genc
 * 
 */
public class TestApplet extends Applet implements MouseListener
{
	public ArrayList<SbgnPDNode> nodes;
	public ArrayList<SbgnPDEdge> edges;

	Button polyominoButton;
	Button tilingButton;
	Button polyominoCompButton;
	Button tilingCompButton;
	
	public int tilingMethod;

	public void init()
	{
		this.setSize(200, 200);
		setBackground(Color.WHITE);

		this.nodes = new ArrayList<SbgnPDNode>();
		this.edges = new ArrayList<SbgnPDEdge>();
		
		this.polyominoButton = new Button("sbgn");
		polyominoButton.addMouseListener(this);
		add(polyominoButton);
	}

	/**
	 * This method draws the bounding boxes of the nodes and the edges between
	 * them.
	 * 
	 * Complexes are colored with RED if they are visited while applying the
	 * tiling. They are colored with GREEN if they are not visited while
	 * applying the tiling. (For the testing purposes)
	 */
	public void paint(Graphics g2)
	{
		Graphics2D g = (Graphics2D) g2;
		// display an information if no layout has been applied.
		if (nodes.size() <= 0)
		{
			g.drawString("Click on a button", 2 * this.getWidth() / 5,
					(int)(this.getHeight()  / 2));
		}

		// draw the edges
		for (int i = 0; i < edges.size(); ++i)
		{
			if(edges.get(i).type.equals(SbgnPDConstants.PRODUCTION))
				g.setColor(Color.BLUE);
			else if (edges.get(i).type.equals(SbgnPDConstants.CONSUMPTION))
				g.setColor(Color.GREEN);
			else if (edges.get(i).type.equals(SbgnPDConstants.RIGID_EDGE))
				g.setColor(Color.RED);
			else
				g.setColor(Color.BLACK);
			
			double[] clipPointCoordinates = new double[4];

			IGeometry.getIntersection( edges.get(i).getTarget().getRect(),
					 edges.get(i).getSource().getRect(), clipPointCoordinates);

			// target clip point minus source clip point gives us length
			double lengthX = clipPointCoordinates[0] - clipPointCoordinates[2];
			double lengthY = clipPointCoordinates[1] - clipPointCoordinates[3];
			
			g.drawLine((int) (clipPointCoordinates[0]  / 2),
					(int) (clipPointCoordinates[1]  / 2),
					(int) (clipPointCoordinates[2]  / 2),
					(int) (clipPointCoordinates[3]  / 2));

			
			//display edge labels
//			int x = ((int) edges.get(i).getSource().getCenterX() / 2 + (int) edges
//					.get(i).getTarget().getCenterX() / 2) / 2;
//			int y = ((int) edges.get(i).getSource().getCenterY() / 2 + (int) edges
//					.get(i).getTarget().getCenterY() / 2) / 2;
//			
//			System.out.println("type: " + edges.get(i).type + " label: " + edges.get(i).label);
//			g.drawString(edges.get(i).label, x, y+5);			
			
			// display the edge types
			// g.drawString(edges.get(i).type, x,y);
		}

		// draw the nodes
		for (int i = 0; i < nodes.size(); ++i)
		{
			SbgnPDNode node = nodes.get(i);
			if (node.type.equals(SbgnPDConstants.COMPLEX) && node.visited)
			{
				g.setColor(Color.RED);
			}
			else if (node.type.equals(SbgnPDConstants.COMPLEX) && !node.visited)
			{
				g.setColor(Color.GREEN);
			}
			else if (node.type.equals(SbgnPDConstants.PRODUCTION_PORT) 
					|| node.type.equals(SbgnPDConstants.CONSUMPTION_PORT))
			{
				g.setColor(Color.RED);
			}
			else
			{
				g.setColor(Color.DARK_GRAY);
			}
			g.drawRect((int) (node.getLeft()  / 2), (int) (node.getTop()  / 2),
					(int) (node.getWidth()  / 2), (int) (node.getHeight()  / 2));

			// display the node areas
			g.setFont(new Font("TimesRoman", Font.PLAIN, 10));
			// g.drawString("" + (int) (node.getWidth() * node.getHeight()),
			// (int) node.getLeft() / 2 + 5, (int) node.getTop() / 2 + 10);

			// draw node labels
//			if (!node.type.equalsIgnoreCase(SbgnPDConstants.COMPLEX))
//			{
//				g.setColor(Color.BLACK);
//
//				g.drawString(node.label.substring(5),
//						(int) node.getCenterX() / 2 - 8,
//						(int) node.getCenterY() / 2 );
//			}
//			else
//			{
//				g.setColor(Color.RED);
//
//				g.drawString(node.label.substring(5),
//						(int) node.getLeft() / 2 + 5,
//						(int) node.getTop() / 2 - 4);
//			}
		}
	}

	@Override
	public void mouseClicked(MouseEvent e)
	{
		if (e.getSource() == polyominoButton)
		{
			Object[] allNodes;
			Object[] allEdges;
			tilingMethod = 0;

//			if (e.getSource() == polyominoButton)
//				tilingMethod = SbgnPDConstants.POLYOMINO_PACKING;
//			else if (e.getSource() == tilingButton)
//				tilingMethod = SbgnPDConstants.TILING;
			try
			{
				// clear the previous information
				nodes.clear();
				edges.clear();

				// apply layout and get the resulting layout
				XmlIOHandler xih = new XmlIOHandler(new SbgnPDLayout());
				Layout l = xih.test();

				// for each node and edge, specify the object types.
				allNodes = l.getAllNodes();
				allEdges = l.getAllEdges();
				for (int i = 0; i < allNodes.length; i++)
				{
					nodes.add((SbgnPDNode) allNodes[i]);
				}

				for (int i = 0; i < allEdges.length; i++)
				{
					edges.add((SbgnPDEdge) allEdges[i]);
				}
			}
			catch (Exception e1)
			{
				e1.printStackTrace();
			}
		}
		repaint();
	}

	/**
	 * calculates usedArea/totalArea inside the complexes and prints them out.
	 */
	private void calculateFullnessOfComplexes()
	{
//		if (tilingMethod == SbgnPDConstants.TILING)
//			System.out.println("Tiling results");
//		else if (tilingMethod == SbgnPDConstants.POLYOMINO_PACKING)
//			System.out.println("Polyomino Packing results");

		for (int i = 0; i < nodes.size(); i++)
		{
			double totalArea = 0;
			double usedArea = 0;

			if (nodes.get(i).type.equalsIgnoreCase(SbgnPDConstants.COMPLEX))
			{
				totalArea = nodes.get(i).getWidth() * nodes.get(i).getHeight();
				System.out.print(nodes.get(i).label);
				LGraph childGr = nodes.get(i).getChild();

				for (int j = 0; j < childGr.getNodes().size(); j++)
				{
					SbgnPDNode s = (SbgnPDNode) childGr.getNodes().get(j);
					usedArea += (s.getWidth() * s.getHeight());
				}
//				System.out.println("   used/total = " + usedArea + "/" + totalArea
//						+ "=" + usedArea / totalArea);
				System.out.println(" = " + usedArea / totalArea);

			}
		}
	}

	@Override
	public void mousePressed(MouseEvent e)
	{
	}

	@Override
	public void mouseReleased(MouseEvent e)
	{
	}

	@Override
	public void mouseEntered(MouseEvent e)
	{
	}

	@Override
	public void mouseExited(MouseEvent e)
	{
	}
}
