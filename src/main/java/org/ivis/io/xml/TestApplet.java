package org.ivis.io.xml;

import java.applet.Applet;
import java.awt.Button;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;

import org.ivis.layout.Layout;
import org.ivis.layout.cose.CoSEEdge;
import org.ivis.layout.cose.CoSELayout;
import org.ivis.layout.cose.CoSENode;
import org.ivis.layout.sbgn.SbgnPDConstants;
import org.ivis.layout.sbgn.SbgnPDEdge;
import org.ivis.layout.sbgn.SbgnPDLayout;
import org.ivis.layout.sbgn.SbgnPDNode;
import org.ivis.layout.sbgn.SbgnProcessNode;
import org.ivis.layout.sbgn.SbgnProcessNode.Orientation;
import org.ivis.util.IGeometry;
import org.ivis.util.PointD;
import org.ivis.util.RectangleD;

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
	public final int RUN_COUNT = 1;
	
	private ArrayList<SbgnPDNode> sbgnNodes;
	private ArrayList<SbgnPDEdge> sbgnEdges;

	private ArrayList<CoSENode> cosenodes;
	private ArrayList<CoSEEdge> coseedges;

	private ArrayList<CoSENode> prodNodes;
	private ArrayList<CoSENode> consNodes;

	private ArrayList<String> fileList;
	private ArrayList<Orientation> orientationList;

	private Button layoutButton;
	private Button exitButton;

	private double zoomLevel = 2.1;
	private double properlyOrientedCoSEEdgeCnt;
	double totalEdgeCount = 0;
	Object myLayout;
	Graphics g2;
	
	
	private boolean isDebugOn = false;

	public void init()
	{
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		this.setSize(dim.width, dim.height);
		setBackground(Color.WHITE);
		g2 = this.getGraphics();

		this.sbgnNodes = new ArrayList<SbgnPDNode>();
		this.sbgnEdges = new ArrayList<SbgnPDEdge>();
		this.cosenodes = new ArrayList<CoSENode>();
		this.coseedges = new ArrayList<CoSEEdge>();
		this.prodNodes = new ArrayList<CoSENode>();
		this.consNodes = new ArrayList<CoSENode>();

		this.fileList = new ArrayList<String>();

		this.orientationList = new ArrayList<Orientation>();
		this.orientationList.add(Orientation.LEFT_TO_RIGHT);
		this.orientationList.add(Orientation.RIGHT_TO_LEFT);
		this.orientationList.add(Orientation.TOP_TO_BOTTOM);
		this.orientationList.add(Orientation.BOTTOM_TO_TOP);

		this.layoutButton = new Button("sbgn");
		this.exitButton = new Button("exit");
		layoutButton.addMouseListener(this);
		exitButton.addMouseListener(this);
		add(layoutButton);
		add(exitButton);

//		fileList.add("org/ivis/io/xml/brca1gadd45.xml");
//		fileList.add("org/ivis/io/xml/ube2sube3a.xml");
//		fileList.add("org/ivis/io/xml/mdm2ube2d1.xml");
//		fileList.add("org/ivis/io/xml/small.xml");
		fileList.add("org/ivis/io/xml/layout.xml");
//		fileList.add("org/ivis/io/xml/insuline.xml");
//		fileList.add("org/ivis/io/xml/neuronal.xml");
//		fileList.add("org/ivis/io/xml/huaiyu.xml");

//		fileList.add("org/ivis/io/xml/glycolysis.xml");
//		fileList.add("org/ivis/io/xml/adenine.xml");
//		fileList.add("org/ivis/io/xml/androgen.xml");

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

		Dimension d = getSize();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, (int) d.getWidth(), (int) d.getHeight());
		// display an information if no layout has been applied.
		if (sbgnNodes.size() <= 0)
		{
			g.drawString("Click on a button", 2 * this.getWidth() / 5,
					(int) (this.getHeight() / zoomLevel));
		}

		if (myLayout instanceof SbgnPDLayout)
		{
			drawNodes(g);
			drawEdges(g);
		}

		else if (myLayout instanceof CoSELayout)
		{
			drawCoseNodes(g);
			drawCoseEdges(g);
		}
	}

	private void drawCoseNodes(Graphics2D g)
	{
		for (int i = 0; i < cosenodes.size(); ++i)
		{
			CoSENode node = cosenodes.get(i);

			g.setColor(Color.DARK_GRAY);

			g.drawRect((int) (node.getLeft() / zoomLevel),
					(int) (node.getTop() / zoomLevel),
					(int) (node.getWidth() / zoomLevel),
					(int) (node.getHeight() / zoomLevel));

//			if (node.type.equals(SbgnPDConstants.PROCESS))
//			{
//				g.drawString("" + node.label + " " + node.OKCount,
//						(int) (node.getCenterX() / zoomLevel),
//						(int) (node.getCenterY() / zoomLevel));
//			}
		}
	}

	private void drawCoseEdges(Graphics2D g)
	{
		for (int i = 0; i < coseedges.size(); ++i)
		{
			if (coseedges.get(i).type.equals(SbgnPDConstants.PRODUCTION))
				g.setColor(Color.RED);
			else if (coseedges.get(i).type.equals(SbgnPDConstants.CONSUMPTION))
				g.setColor(Color.GREEN);

			double[] clipPointCoordinates = new double[4];
			RectangleD rectTarget;
			RectangleD rectSource;

			rectTarget = coseedges.get(i).getTarget().getRect();
			rectSource = coseedges.get(i).getSource().getRect();

			IGeometry.getIntersection(rectTarget, rectSource,
					clipPointCoordinates);

			g.drawLine((int) (clipPointCoordinates[0] / zoomLevel),
					(int) (clipPointCoordinates[1] / zoomLevel),
					(int) (clipPointCoordinates[2] / zoomLevel),
					(int) (clipPointCoordinates[3] / zoomLevel));

			g.setColor(Color.BLACK);
			g.setFont(new Font("TimesRoman", Font.PLAIN, 10));

			// // display edge labels
			int x = (int) ((clipPointCoordinates[0] / zoomLevel + clipPointCoordinates[2]
					/ zoomLevel) / 2);
			int y = (int) ((clipPointCoordinates[1] / zoomLevel + clipPointCoordinates[3]
					/ zoomLevel) / 2);
		}

	}

	private void drawNodes(Graphics2D g)
	{
		// draw the nodes
		for (int i = 0; i < sbgnNodes.size(); ++i)
		{
			SbgnPDNode node = sbgnNodes.get(i);

			if (node.type != null && node.type.equals(SbgnPDConstants.COMPLEX)
					&& node.visited)
			{
				g.setColor(Color.ORANGE);
			}
			else if (node.type != null
					&& node.type.equals(SbgnPDConstants.COMPLEX)
					&& !node.visited)
			{
				g.setColor(Color.BLUE);
			}
			else if (node.type != null
					&& (node.type.equals(SbgnPDConstants.INPUT_PORT)))
			{
				g.setColor(Color.GREEN);
			}
			else if (node.type != null
					&& (node.type.equals(SbgnPDConstants.OUTPUT_PORT)))
			{
				g.setColor(Color.RED);
			}
			else if (node.type != null && node.isDummyCompound)
			{
				System.out.println("dummy");
				g.setColor(Color.MAGENTA);
			}

			else if (node.getChild() != null)
			{
				g.setColor(Color.PINK);
			}
			
			else
			{
				g.setColor(Color.DARK_GRAY);
			}
			
			//
			if (node instanceof SbgnProcessNode
					&& ((SbgnProcessNode) node).isHighlighted)
			{
				SbgnProcessNode processNode = (SbgnProcessNode) node;

				// g.setColor(Color.GRAY);
				// g.fillRect(
				// (int) (processNode.getParentCompound().getLeft() /
				// zoomLevel),
				// (int) (processNode.getParentCompound().getTop() / zoomLevel),
				// (int) (processNode.getParentCompound().getWidth() /
				// zoomLevel),
				// (int) (processNode.getParentCompound().getHeight() /
				// zoomLevel));
			}
			// // System.out.println("node.label " +
			// // ((SbgnProcessNode)node).getParentCompound().label);
			// g.setColor(Color.BLACK);
			//
			// if (node instanceof SbgnProcessNode)
			// {
//			if(node.label != null)
//			 g.drawString(node.label, (int) (node.getCenterX() /
//					 zoomLevel), (int) (node.getCenterY() / zoomLevel));
			//
			// g.setColor(Color.BLACK);
			// }
			g.drawRect((int) (node.getLeft() / zoomLevel),
					(int) (node.getTop() / zoomLevel),
					(int) (node.getWidth() / zoomLevel),
					(int) (node.getHeight() / zoomLevel));

			// display the node areas
			// g.setFont(new Font("TimesRoman", Font.PLAIN, 10));
			// g.drawString("" + (int) (node.getWidth() * node.getHeight()),
			// (int) (node.getLeft() / zoomLevel) + 5, (int) (node.getTop() /
			// zoomLevel) + 10);

			// draw node labels
			// if (!node.type.equalsIgnoreCase(SbgnPDConstants.COMPLEX))
			// {
			g.setColor(Color.BLACK);

			// g.drawString(""+node.correspondingAngle,
			// (int) (node.getCenterX() / zoomLevel) - 8,
			// (int) (node.getCenterY() / zoomLevel) );
			// }
			// else
			// {
			// g.setColor(Color.RED);
			//
			// g.drawString(node.label.substring(5),
			// (int) node.getLeft() / 2 + 5,
			// (int) node.getTop() / 2 - 4);
			// }
		}
	}

	private void drawEdges(Graphics2D g)
	{
		// draw the edges
		for (int i = 0; i < sbgnEdges.size(); ++i)
		{
			if (sbgnEdges.get(i).type.equals(SbgnPDConstants.PRODUCTION))
			{
				if (sbgnEdges.get(i).isProperlyOriented)
					g.setColor(Color.LIGHT_GRAY);
				else if (sbgnEdges.get(i).getTarget() instanceof SbgnProcessNode)
					g.setColor(Color.RED);
				else
					g.setColor(Color.LIGHT_GRAY);
			}
			else if (sbgnEdges.get(i).type.equals(SbgnPDConstants.CONSUMPTION))
			{
				if (sbgnEdges.get(i).isProperlyOriented)
					g.setColor(Color.LIGHT_GRAY);
				else if (sbgnEdges.get(i).getSource() instanceof SbgnProcessNode)
					g.setColor(Color.GREEN);
				else
					g.setColor(Color.LIGHT_GRAY);
			}
			else if (sbgnEdges.get(i).type.equals(SbgnPDConstants.RIGID_EDGE))
				g.setColor(Color.BLUE);
			else
				g.setColor(Color.BLACK);

			double[] clipPointCoordinates = new double[4];
			RectangleD rectTarget;
			RectangleD rectSource;

			rectTarget = sbgnEdges.get(i).getTarget().getRect();
			rectSource = sbgnEdges.get(i).getSource().getRect();

			IGeometry.getIntersection(rectTarget, rectSource,
					clipPointCoordinates);

			g.drawLine((int) (clipPointCoordinates[0] / zoomLevel),
					(int) (clipPointCoordinates[1] / zoomLevel),
					(int) (clipPointCoordinates[2] / zoomLevel),
					(int) (clipPointCoordinates[3] / zoomLevel));

			g.setColor(Color.BLACK);
			g.setFont(new Font("TimesRoman", Font.PLAIN, 10));

			// // display edge labels
			int x = (int) ((clipPointCoordinates[0] / zoomLevel + clipPointCoordinates[2]
					/ zoomLevel) / 2);
			int y = (int) ((clipPointCoordinates[1] / zoomLevel + clipPointCoordinates[3]
					/ zoomLevel) / 2);

			// g.drawString(""+edges.get(i).correspondingAngle, x, y+5);

			// if(edges.get(i).type.equals(SbgnPDConstants.PRODUCTION))
			// g.drawString(""+((SbgnPDNode)edges.get(i).getSource()).getSpringForceX(),
			// x, y+5);
			// else if(edges.get(i).type.equals(SbgnPDConstants.CONSUMPTION))
			// g.drawString(""+((SbgnPDNode)edges.get(i).getTarget()).getSpringForceX(),
			// x, y+5);

		}
	}

	private void calculateAngles()
	{
		properlyOrientedCoSEEdgeCnt = 0;

		PointD inputPort = new PointD();
		PointD outputPort = new PointD();
		PointD inputPortTarget = new PointD();
		PointD outputPortTarget = new PointD();

		double bestResult = Double.MIN_VALUE;
		double appropriateEdgeCnt = 0;
		double notAppropriateEdgeCnt = 0;
		totalEdgeCount = 0;

		PointD centerPointD = new PointD();

		for (CoSENode node : cosenodes)
		{
			if (node.type.equals(SbgnPDConstants.PROCESS))
			{
				if (isDebugOn)
					System.out.println("\n process node " + node.label);

				centerPointD.x = node.getCenterX();
				centerPointD.y = node.getCenterY();

				prodNodes.clear();
				consNodes.clear();

				for (Object edgeObj : node.getEdges())
				{
					if (((CoSEEdge) edgeObj).type
							.equals(SbgnPDConstants.PRODUCTION))
						prodNodes.add((CoSENode) ((CoSEEdge) edgeObj)
								.getTarget());
					else if (((CoSEEdge) edgeObj).type
							.equals(SbgnPDConstants.CONSUMPTION))
						consNodes.add((CoSENode) ((CoSEEdge) edgeObj)
								.getSource());
				}

				if (isDebugOn)
				{
					for (CoSENode c : prodNodes)
						System.out.println("prod: " + c.label);
					for (CoSENode c : consNodes)
						System.out.println("cons: " + c.label);

					System.out.println();
				}
				bestResult = Double.MIN_VALUE;

				for (Orientation orient : orientationList)
				{
					if (isDebugOn)
						System.out.println("trying orientation: " + orient);
					appropriateEdgeCnt = 0;
					notAppropriateEdgeCnt = 0;

					inputPort = findPortLocation(true, centerPointD, orient);
					outputPort = findPortLocation(false, centerPointD, orient);

					inputPortTarget = findPortTargetPoint(true, orient,
							inputPort, outputPort);
					outputPortTarget = findPortTargetPoint(false, orient,
							inputPort, outputPort);

					if (isDebugOn)
						System.out.println("consumption nodes");
					for (CoSENode node2 : consNodes)
					{
						if (isAngleAppropriate(node2, inputPort,
								inputPortTarget))
						{
							appropriateEdgeCnt++;
							if (isDebugOn)
								System.out.println("" + node2.label + " +");
						}
						else
						{
							notAppropriateEdgeCnt++;
							if (isDebugOn)
								System.out.println("" + node2.label + " -");

						}
					}

					if (isDebugOn)
						System.out.println("production nodes");
					for (CoSENode node2 : prodNodes)
					{
						if (isAngleAppropriate(node2, outputPort,
								outputPortTarget))
						{
							appropriateEdgeCnt++;
							if (isDebugOn)
								System.out.println("" + node2.label + " +");

						}
						else
						{
							notAppropriateEdgeCnt++;
							if (isDebugOn)
								System.out.println("" + node2.label + " -");

						}
					}

					if (isDebugOn)
						System.out.println("best: " + bestResult + " current: "
								+ appropriateEdgeCnt);
					if (appropriateEdgeCnt > bestResult)
					{
						bestResult = appropriateEdgeCnt;
						node.orient = orient;
						node.OKCount = bestResult;

						if (isDebugOn)
							System.out.println("best updated: " + node.OKCount);
					}
				}

				properlyOrientedCoSEEdgeCnt += bestResult;
				totalEdgeCount += (appropriateEdgeCnt + notAppropriateEdgeCnt);
			}
		}

		System.out.println("totalResult: " + properlyOrientedCoSEEdgeCnt
				+ " / " + totalEdgeCount);
	}

	private boolean isAngleAppropriate(CoSENode node, PointD portNode,
			PointD targetPoint)
	{
		PointD nodeCenter = node.getCenter();
		double angleValue = 0.0;

		PointD point1 = new PointD(targetPoint.x - portNode.x, targetPoint.y
				- portNode.y);
		PointD point2 = new PointD(nodeCenter.x - portNode.x, nodeCenter.y
				- portNode.y);

		if (Math.abs(point1.x) < 0)
			point1.x = 0.0001;
		if (Math.abs(point1.y) < 0)
			point1.y = 0.0001;

		angleValue = (point1.x * point2.x + point1.y * point2.y)
				/ (Math.sqrt(point1.x * point1.x + point1.y * point1.y) * Math
						.sqrt(point2.x * point2.x + point2.y * point2.y));

		double angle = Math.abs(Math.toDegrees(Math.acos(angleValue)));

		if (isDebugOn)
			System.out.println("angle: " + angle);

		if (angle < SbgnPDConstants.ANGLE_TOLERANCE)
		{
			return true;
		}
		else
			return false;
	}

	private PointD findPortLocation(boolean isInputPort, PointD centerPoint,
			Orientation orientation)
	{
		if (orientation.equals(Orientation.LEFT_TO_RIGHT))
		{
			if (isInputPort)
				return new PointD(
						(centerPoint.x - SbgnPDConstants.RIGID_EDGE_LENGTH),
						centerPoint.y);
			else
				return new PointD(
						(centerPoint.x + SbgnPDConstants.RIGID_EDGE_LENGTH),
						centerPoint.y);
		}
		else if (orientation.equals(Orientation.RIGHT_TO_LEFT))
		{
			if (isInputPort)
				return new PointD(
						(centerPoint.x + SbgnPDConstants.RIGID_EDGE_LENGTH),
						centerPoint.y);
			else
				return new PointD(
						(centerPoint.x - SbgnPDConstants.RIGID_EDGE_LENGTH),
						centerPoint.y);
		}
		else if (orientation.equals(Orientation.TOP_TO_BOTTOM))
		{
			if (isInputPort)
				return new PointD(centerPoint.x,
						(centerPoint.y - SbgnPDConstants.RIGID_EDGE_LENGTH));
			else
				return new PointD(centerPoint.x,
						(centerPoint.y + SbgnPDConstants.RIGID_EDGE_LENGTH));
		}
		else if (orientation.equals(Orientation.BOTTOM_TO_TOP))
		{
			if (isInputPort)
				return new PointD(centerPoint.x,
						(centerPoint.y + SbgnPDConstants.RIGID_EDGE_LENGTH));
			else
				return new PointD(centerPoint.x,
						(centerPoint.y - SbgnPDConstants.RIGID_EDGE_LENGTH));
		}

		return null;
	}

	private PointD findPortTargetPoint(boolean isInputPort,
			Orientation orientation, PointD inputPort, PointD outputPort)
	{
		double idealEdgeLength = ((CoSELayout) myLayout).idealEdgeLength;
		if (orientation.equals(Orientation.LEFT_TO_RIGHT))
		{
			if (isInputPort)
				return new PointD((inputPort.x - idealEdgeLength), inputPort.y);
			else
				return new PointD((outputPort.x + idealEdgeLength),
						outputPort.y);
		}
		else if (orientation.equals(Orientation.RIGHT_TO_LEFT))
		{
			if (isInputPort)
				return new PointD((inputPort.x + idealEdgeLength), inputPort.y);
			else
				return new PointD((outputPort.x - idealEdgeLength),
						outputPort.y);
		}
		else if (orientation.equals(Orientation.TOP_TO_BOTTOM))
		{
			if (isInputPort)
				return new PointD(inputPort.x, (inputPort.y - idealEdgeLength));
			else
				return new PointD(outputPort.x,
						(outputPort.y + idealEdgeLength));
		}
		else if (orientation.equals(Orientation.BOTTOM_TO_TOP))
		{
			if (isInputPort)
				return new PointD(inputPort.x, (inputPort.y + idealEdgeLength));
			else
				return new PointD(outputPort.x,
						(outputPort.y - idealEdgeLength));
		}

		return null;
	}

	@Override
	public void mouseClicked(MouseEvent e)
	{
		try
		{
			if (e.getSource() == layoutButton)
			{
				FileWriter writer = new FileWriter("results.csv");
				writer.append("Phase1IterCnt,Phase2IterCnt,SbgnProperEdgeCnt,"
						+ "TotalEdgeCnt,SbgnResult,SbgnExecTime,CoSEProperEdgeCnt,"
						+ "CoSEResult,CoSEExecTime\n");
				performSbgnPDLayout(writer);

				//performCoSELayout(writer);

				writer.flush();
				writer.close();

			}
			else if (e.getSource() == exitButton)
			{
				System.exit(0);
			}
		}
		catch (IOException e1)
		{
			e1.printStackTrace();
		}
	}

	private void performCoSELayout(Writer writer)
	{
		System.out.println("Cose started");

		for (int j = 0; j < fileList.size(); j++)
		{
			try
			{
				for (int i = 0; i < RUN_COUNT; i++)
				{
					myLayout = new CoSELayout();

					// apply layout and get the resulting layout
					XmlIOHandler xih = new XmlIOHandler((CoSELayout) myLayout);
					Layout l = xih.test(fileList.get(j));

					Object[] allNodes = ((CoSELayout) myLayout).getAllNodes();
					Object[] allEdges = ((CoSELayout) myLayout).getAllEdges();

					// for each node and edge, specify the object types.
					this.cosenodes.clear();
					this.coseedges.clear();

					for (int k = 0; k < allNodes.length; k++)
					{
						this.cosenodes.add((CoSENode) allNodes[k]);
					}

					for (int k = 0; k < allEdges.length; k++)
					{
						this.coseedges.add((CoSEEdge) allEdges[k]);
					}

					calculateAngles();

					writer.append("" + properlyOrientedCoSEEdgeCnt);
					writer.append(",");
					writer.append(""
							+ (properlyOrientedCoSEEdgeCnt / totalEdgeCount));
					writer.append(",");
					writer.append("" + (((CoSELayout) myLayout).executionTime));
					writer.append("\n");
				}
			}
			catch (Exception e1)
			{
				e1.printStackTrace();
			}
		}

		 repaint();
	}

	private void performSbgnPDLayout(Writer writer)
	{
		for (int j = 0; j < fileList.size(); j++)
		{
			try
			{
				for (int i = 0; i < RUN_COUNT; i++)
				{
					myLayout = new SbgnPDLayout();

					// apply layout and get the resulting layout
					XmlIOHandler xih = new XmlIOHandler((SbgnPDLayout) myLayout);
					Layout l = xih.test(fileList.get(j));

					writer.append(""
							+ ((SbgnPDLayout) myLayout).phase1IterationCount);
					writer.append(',');
					writer.append(""
							+ ((SbgnPDLayout) myLayout).phase2IterationCount);
					writer.append(',');
					writer.append(""
							+ ((SbgnPDLayout) myLayout).properlyOrientedEdgeCount);
					writer.append(',');
					writer.append("" + ((SbgnPDLayout) myLayout).totalEdgeCount);
					writer.append(',');
					writer.append(""
							+ (((SbgnPDLayout) myLayout).properlyOrientedEdgeCount / ((SbgnPDLayout) myLayout).totalEdgeCount));
					writer.append(',');
					writer.append(""
							+ (((SbgnPDLayout) myLayout).executionTime));
					writer.append('\n');
				}

			}
			catch (Exception e1)
			{
				e1.printStackTrace();
			}
		}
		
		 Object[] allNodes = ((SbgnPDLayout) myLayout).getAllNodes();
		 Object[] allEdges = ((SbgnPDLayout) myLayout).getAllEdges();
		
		 // for each node and edge, specify the object types.
		 this.sbgnNodes.clear();
		 this.sbgnEdges.clear();
		
		 for (int i = 0; i < allNodes.length; i++)
		 {
		 this.sbgnNodes.add((SbgnPDNode) allNodes[i]);
		 }
		
		 for (int i = 0; i < allEdges.length; i++)
		 {
		 this.sbgnEdges.add((SbgnPDEdge) allEdges[i]);
		 }
		
		 repaint();
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
