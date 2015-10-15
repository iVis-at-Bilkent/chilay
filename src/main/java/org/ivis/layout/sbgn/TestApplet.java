package org.ivis.layout.sbgn;

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

import javax.xml.bind.JAXBException;

import org.ivis.io.xml.XmlIOHandler;
import org.ivis.layout.Layout;
import org.ivis.layout.cose.CoSEEdge;
import org.ivis.layout.cose.CoSELayout;
import org.ivis.layout.cose.CoSENode;
import org.ivis.layout.sbgn.SbgnProcessNode.Orientation;
import org.ivis.util.IGeometry;
import org.ivis.util.PointD;
import org.ivis.util.RectangleD;

/**
 * This class is used to test the results of sbgn-pd layout algorithm with two tiling methods:
 * polyomino packing and basic tiling. The applet only displays the bounding boxes of the nodes and
 * draws the edges.
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
	private ArrayList<CoSENode> effNodes;

	private ArrayList<String> fileList;
	private ArrayList<Orientation> orientationList;

	private Button layoutButton;
	private Button exitButton;

	private double zoomLevel = 1.3;
	private double properlyOrientedCoSEEdgeCnt;

	double totalEdgeCount = 0;
	Object myLayout;
	Graphics g2;

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
		this.effNodes = new ArrayList<CoSENode>();

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

		loadFiles();
	}

	private void loadFiles()
	{
		// Following graphs have been used to obtain results
		// fileList.add("../../graphs/sbgn/test/2-assembly.xml");
		// fileList.add("../../graphs/sbgn/test/2-commissural.xml");
		// fileList.add("../../graphs/sbgn/test/2-geneexpression.xml");
		// fileList.add("../../graphs/sbgn/test/2-ifn.xml");
		// fileList.add("../../graphs/sbgn/test/2-innate.xml");
		// fileList.add("../../graphs/sbgn/test/2-mek.xml");
		// fileList.add("../../graphs/sbgn/test/2-sprouty.xml");
		// fileList.add("../../graphs/sbgn/test/2-transcriptional.xml");
		// fileList.add("../../graphs/sbgn/test/3-adrenaline.xml");
		// fileList.add("../../graphs/sbgn/test/3-akt.xml");
		// fileList.add("../../graphs/sbgn/test/3-disinhibition.xml");
		// fileList.add("../../graphs/sbgn/test/3-gb1p.xml");
		// fileList.add("../../graphs/sbgn/test/3-grb2.xml");
		// fileList.add("../../graphs/sbgn/test/3-presenilin.xml");
		// fileList.add("../../graphs/sbgn/test/3-recruitment.xml");
		// fileList.add("../../graphs/sbgn/test/3-regulation.xml");
		// fileList.add("../../graphs/sbgn/test/3-syndecan.xml");
		// fileList.add("../../graphs/sbgn/test/advanced.xml");
		// fileList.add("../../graphs/sbgn/test/ampk.xml");
		// fileList.add("../../graphs/sbgn/test/bcr.xml");
		// fileList.add("../../graphs/sbgn/test/dcc.xml");
		// fileList.add("../../graphs/sbgn/test/erk1.xml");
		// fileList.add("../../graphs/sbgn/test/gprotein.xml");
		// fileList.add("../../graphs/sbgn/test/il2.xml");
		// fileList.add("../../graphs/sbgn/test/multistep.xml");
		// fileList.add("../../graphs/sbgn/test/neurotrophic.xml");
		// fileList.add("../../graphs/sbgn/test/pka.xml");
		// fileList.add("../../graphs/sbgn/test/ppara.xml");
		// fileList.add("../../graphs/sbgn/test/rafmap.xml");
		// fileList.add("../../graphs/sbgn/test/ras.xml");
		// fileList.add("../../graphs/sbgn/test/role.xml");
		// fileList.add("../../graphs/sbgn/test/rora.xml");
		// fileList.add("../../graphs/sbgn/test/sema3a.xml");

		// Following graphs have been used for parameter tests
		// fileList.add("../../graphs/sbgn/test2/uba7ube2l6.xml");
		// fileList.add("../../graphs/sbgn/test2/nf1rasa1.xml");
		// fileList.add("../../graphs/sbgn/test2/pik3cdpkn1.xml");
		// fileList.add("../../graphs/sbgn/test2/ZFYVE28egfr.xml");
		// fileList.add("../../graphs/sbgn/test2/rad51cbrca2.xml");
		// fileList.add("../../graphs/sbgn/test2/uba1ube2k.xml");
		// fileList.add("../../graphs/sbgn/test2/uba7ube2l6.xml");
		// fileList.add("../../graphs/sbgn/test2/brca1gadd45.xml");
		// fileList.add("../../graphs/sbgn/test2/ube2sube3a.xml");
		// fileList.add("../../graphs/sbgn/test2/small.xml");
		// fileList.add("../../graphs/sbgn/test2/insuline.xml");
		// fileList.add("../../graphs/sbgn/test2/neuronal.xml");
		// fileList.add("../../graphs/sbgn/test2/huaiyu.xml"); // causes memory
		// problems sometimes in calcGrid function
		fileList.add("../graphs/sbgn/test2/glycolysis.xml");
		// fileList.add("../../graphs/sbgn/test2/adenine.xml");
		// fileList.add("../../graphs/sbgn/test2/androgen.xml");

		// fileList.add("org/ivis/io/xml/layout.xml");
	}

	/**
	 * This method draws the bounding boxes of the nodes and the edges between them.
	 * 
	 * Complexes are colored with RED if they are visited while applying the tiling. They are
	 * colored with GREEN if they are not visited while applying the tiling. (For the testing
	 * purposes)
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
			g.drawString("Click on a button", 2 * this.getWidth() / 5, (int) (this.getHeight() / zoomLevel));
		}

		if (myLayout instanceof SbgnPDLayout)
		{
			drawSBGNNodes(g);
			drawSBGNEdges(g);
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

			g.drawRect((int) (node.getLeft() / zoomLevel), (int) (node.getTop() / zoomLevel),
					(int) (node.getWidth() / zoomLevel), (int) (node.getHeight() / zoomLevel));

			// if (node.type.equals(SbgnPDConstants.PROCESS))
			// g.drawString("" + node.orient,
			// (int) (node.getCenterX() / zoomLevel),
			// (int) (node.getCenterY() / zoomLevel));

			// if (node.isOrientationProper)
			// g.drawString("proper", (int) (node.getCenterX() / zoomLevel),
			// (int) (node.getCenterY() / zoomLevel));
			// else
			// g.drawString("not", (int) (node.getCenterX() / zoomLevel),
			// (int) (node.getCenterY() / zoomLevel));
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
			else if (isEffector(coseedges.get(i)))
				g.setColor(Color.ORANGE);

			double[] clipPointCoordinates = new double[4];
			RectangleD rectTarget;
			RectangleD rectSource;

			rectTarget = coseedges.get(i).getTarget().getRect();
			rectSource = coseedges.get(i).getSource().getRect();

			IGeometry.getIntersection(rectTarget, rectSource, clipPointCoordinates);

			g.drawLine((int) (clipPointCoordinates[0] / zoomLevel), (int) (clipPointCoordinates[1] / zoomLevel),
					(int) (clipPointCoordinates[2] / zoomLevel), (int) (clipPointCoordinates[3] / zoomLevel));

			g.setColor(Color.BLACK);
			g.setFont(new Font("TimesRoman", Font.PLAIN, 10));

			// // // display edge labels
			// int x = (int) ((clipPointCoordinates[0] / zoomLevel +
			// clipPointCoordinates[2]
			// / zoomLevel) / 2);
			// int y = (int) ((clipPointCoordinates[1] / zoomLevel +
			// clipPointCoordinates[3]
			// / zoomLevel) / 2);

		}

	}

	private void drawSBGNNodes(Graphics2D g)
	{
		// draw the nodes
		for (int i = 0; i < sbgnNodes.size(); ++i)
		{
			SbgnPDNode node = sbgnNodes.get(i);

			if (node.type != null)
			{

				if (node.type.equals(SbgnPDConstants.COMPLEX) && node.visited)
					g.setColor(Color.ORANGE);
				else if (node.type.equals(SbgnPDConstants.COMPLEX) && !node.visited)
					g.setColor(Color.BLUE);
				else if (node.type.equals(SbgnPDConstants.INPUT_PORT))
					g.setColor(Color.GREEN);
				else if (node.type.equals(SbgnPDConstants.OUTPUT_PORT))
					g.setColor(Color.RED);
				else if (node.isDummyCompound)
					g.setColor(Color.MAGENTA);
				else if (node.getChild() != null)
					g.setColor(Color.PINK);
				else
					g.setColor(Color.DARK_GRAY);
			}

			// if (node instanceof SbgnProcessNode)
			// {
			// g.drawString("" + ((SbgnProcessNode) node).getEdges().size(),
			// (int) (node.getCenterX() / zoomLevel + 2),
			// (int) (node.getCenterY() / zoomLevel - 2));
			//
			// g.setColor(Color.BLACK);
			// }

			g.drawRect((int) (node.getLeft() / zoomLevel), (int) (node.getTop() / zoomLevel),
					(int) (node.getWidth() / zoomLevel), (int) (node.getHeight() / zoomLevel));

			g.setColor(Color.BLACK);
		}
	}

	private void drawSBGNEdges(Graphics2D g)
	{
		// draw the edges
		for (int i = 0; i < sbgnEdges.size(); ++i)
		{
			if (sbgnEdges.get(i).type.equals(SbgnPDConstants.PRODUCTION))
			{
				if (sbgnEdges.get(i).isProperlyOriented)
					g.setColor(Color.LIGHT_GRAY);
				else
					g.setColor(Color.RED);
			}
			else if (sbgnEdges.get(i).type.equals(SbgnPDConstants.CONSUMPTION))
			{
				if (sbgnEdges.get(i).isProperlyOriented)
					g.setColor(Color.LIGHT_GRAY);
				else
					g.setColor(Color.GREEN);
			}
			else if (sbgnEdges.get(i).isRigidEdge())
			{
				g.setColor(Color.BLUE);
			}
			else if (sbgnEdges.get(i).isEffector())
			{
				if (sbgnEdges.get(i).isProperlyOriented)
					g.setColor(Color.LIGHT_GRAY);
				else
					g.setColor(Color.MAGENTA);
			}
			else
				g.setColor(Color.BLACK);

			double[] clipPointCoordinates = new double[4];
			RectangleD rectTarget;
			RectangleD rectSource;

			rectTarget = sbgnEdges.get(i).getTarget().getRect();
			rectSource = sbgnEdges.get(i).getSource().getRect();

			IGeometry.getIntersection(rectTarget, rectSource, clipPointCoordinates);

			g.drawLine((int) (clipPointCoordinates[0] / zoomLevel), (int) (clipPointCoordinates[1] / zoomLevel),
					(int) (clipPointCoordinates[2] / zoomLevel), (int) (clipPointCoordinates[3] / zoomLevel));

			g.setColor(Color.BLACK);
			g.setFont(new Font("TimesRoman", Font.PLAIN, 10));

			// // display edge labels
			int x = (int) ((clipPointCoordinates[0] / zoomLevel + clipPointCoordinates[2] / zoomLevel) / 2);
			int y = (int) ((clipPointCoordinates[1] / zoomLevel + clipPointCoordinates[3] / zoomLevel) / 2);

			// g.drawString("" + sbgnEdges.get(i).correspondingAngle, x, y + 5);

		}
	}

	@Override
	public void mouseClicked(MouseEvent e)
	{
		// if (e.getSource() == layoutButton)
		// {
		// try
		// {
		// testParameters();
		// }
		// catch (Exception e1)
		// {
		// e1.printStackTrace();
		// }
		// }
		try
		{
			if (e.getSource() == layoutButton)
			{
				FileWriter writer = new FileWriter("results.csv");
				writer.append("SBGNGraphSize,effectorCnt,processNodeCnt,totalEdgeCntToOrient,"
						+ "SBGNEffResult,SBGNEnhancedResult,SBGNTime,CoSEGraphSize,CoSEAngleResult,"
						+ "CoSEEdgeResult,CoSETime\n");

				performSbgnPDLayout(writer);
				// reportSBGNResult();

				// performCoSELayout(writer, 2);

				// reportCoSEResult();

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
							+ (((SbgnPDLayout) myLayout).getAllNodes().length + ((SbgnPDLayout) myLayout).getAllEdges().length));
					writer.append(',');
					writer.append("" + ((SbgnPDLayout) myLayout).totalEffCount);
					writer.append(',');
					writer.append("" + ((SbgnPDLayout) myLayout).processNodeList.size());
					writer.append(',');
					writer.append("" + ((SbgnPDLayout) myLayout).totalEdgeCountToBeOriented);
					writer.append(',');
					writer.append("" + (((SbgnPDLayout) myLayout).successRatio));
					writer.append(',');
					writer.append("" + (((SbgnPDLayout) myLayout).enhancedRatio));
					writer.append(',');
					writer.append("" + (((SbgnPDLayout) myLayout).executionTime));
					writer.append('\n');

					writer.flush();
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

		// comment out if you do not want to see the boxes
		repaint();
	}

	/**
	 * method 1: calculate best orientation by maximizing the total number of properly oriented
	 * edges method 2: calculate best orientation by minimizing the total angle between
	 * prod/cons/effector nodes and their corresponding processes
	 */
	private void performCoSELayout(Writer writer, int method)
	{
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
						this.cosenodes.add((CoSENode) allNodes[k]);

					for (int k = 0; k < allEdges.length; k++)
						this.coseedges.add((CoSEEdge) allEdges[k]);

					writer.append("" + (cosenodes.size() + coseedges.size()));
					writer.append(",");

					calcPropOrientedEdgesByAngleAmount();

					writer.append("" + (properlyOrientedCoSEEdgeCnt / totalEdgeCount));
					writer.append(",");

					calcPropOrientedCoSEEdgesByNumber();

					writer.append("" + (properlyOrientedCoSEEdgeCnt / totalEdgeCount));
					writer.append(",");
					writer.append("" + ((CoSELayout) myLayout).executionTime);

					writer.append("\n");
				}
			}
			catch (Exception e1)
			{
				e1.printStackTrace();
			}
		}

		// comment out if you do not want to see the boxes
		repaint();
	}

	/**
	 * This method is used for parameter tuning. In order to use this, the static fields in
	 * SbgnPDConstants should be changed. RUN_COUNT should be set.
	 */

	private void testParameters() throws Exception
	{
		XmlIOHandler xih;
		SbgnPDLayout sLayout = null;

		int[] appDistance = { 10, 20, 30, 50, 70 };
		double[] rot90 = { 40, 50, 55, 60, 65, 70, 90 };
		int[] approxPeriod = { 59, 101, 211, 307 };
		int[] iterCount = { 2, 5, 10, 20, 50 };

		double avgResult = 0;
		long avgExecTime = 0;
		double avgPhase2IterCnt = 0;
		FileWriter writer = new FileWriter("parameterTest2.csv");
		writer.append("appDistance,rot90,iterCount,apprxPeriod,phase2IterCnt,"
				+ "nodeCnt,edgeCnt,avgResult,avgExecTime\n");

		for (int i = 0; i < appDistance.length; i++)
		{
			SbgnPDConstants.APPROXIMATION_DISTANCE = appDistance[i];
			System.out.println("app dist: " + appDistance[i]);

			for (int j = 0; j < iterCount.length; j++)
			{
				SbgnPDConstants.ROTATIONAL_FORCE_ITERATION_COUNT = iterCount[j];

				for (int k = 0; k < rot90.length; k++)
				{
					SbgnPDConstants.ROTATION_90_DEGREE = rot90[k];

					for (int u = 0; u < approxPeriod.length; u++)
					{
						SbgnPDConstants.APPROXIMATION_PERIOD = approxPeriod[u];

						System.out.println("running for: " + i + " " + j + " " + k + " " + u + " ");

						avgResult = 0;
						avgExecTime = 0;
						avgPhase2IterCnt = 0;

						for (int fileIndex = 0; fileIndex < fileList.size(); fileIndex++)
						{
							System.out.println(fileList.get(fileIndex));
							for (int w = 0; w < RUN_COUNT; w++)
							{
								System.out.print(w + " ");
								myLayout = new SbgnPDLayout();

								// apply layout and get the
								// resulting layout
								try
								{
									xih = new XmlIOHandler((SbgnPDLayout) myLayout);
									Layout x = xih.test(fileList.get(fileIndex));
									sLayout = (SbgnPDLayout) x;
									// avgResult +=
									// (sLayout.properlyOrientedEdgeCount /
									// sLayout.totalEdgeCount);
									avgExecTime += sLayout.executionTime;
									avgPhase2IterCnt += sLayout.phase2IterationCount;
								}
								catch (JAXBException e)
								{
									e.printStackTrace();
								}
							}
							System.out.println();
						}

						writer.append("" + SbgnPDConstants.APPROXIMATION_DISTANCE);
						writer.append(',');
						writer.append("" + SbgnPDConstants.ROTATION_90_DEGREE);
						writer.append(',');
						writer.append("" + SbgnPDConstants.ROTATIONAL_FORCE_ITERATION_COUNT);
						writer.append(',');
						writer.append("" + SbgnPDConstants.APPROXIMATION_PERIOD);
						writer.append(',');
						writer.append("" + avgPhase2IterCnt / (RUN_COUNT * fileList.size()));
						writer.append(',');
						writer.append("" + sLayout.getAllNodes().length);
						writer.append(',');
						writer.append("" + sLayout.getAllEdges().length);
						writer.append(',');
						writer.append("" + avgResult / (RUN_COUNT * fileList.size()));
						writer.append(',');
						writer.append("" + avgExecTime / (RUN_COUNT * fileList.size()));
						writer.append('\n');
						writer.flush();
					}
				}
			}
		}
		writer.close();
	}

	private double calculateEffectorAngle(Orientation orient, PointD centerPt, CoSENode eff)
	{
		double idealEdgeLength = ((CoSELayout) myLayout).idealEdgeLength;
		PointD targetPnt = new PointD();
		PointD centerPnt = centerPt;

		// find target point
		if (orient.equals(Orientation.LEFT_TO_RIGHT) || orient.equals(Orientation.RIGHT_TO_LEFT))
		{
			targetPnt.x = centerPnt.x;

			if (eff.getCenterY() > centerPnt.y)
				targetPnt.y = centerPnt.y + idealEdgeLength;
			else
				targetPnt.y = centerPnt.y - idealEdgeLength;
		}
		else if (orient.equals(Orientation.BOTTOM_TO_TOP) || orient.equals(Orientation.TOP_TO_BOTTOM))
		{
			targetPnt.y = centerPnt.y;

			if (eff.getCenterX() > centerPnt.x)
				targetPnt.x = centerPnt.x + idealEdgeLength;
			else
				targetPnt.x = centerPnt.x - idealEdgeLength;
		}

		double angle = IGeometry.calculateAngle(targetPnt, centerPnt, eff.getCenter());

		return angle;
	}

	private PointD findPortLocation(boolean isInputPort, PointD centerPoint, Orientation orientation)
	{
		if (orientation.equals(Orientation.LEFT_TO_RIGHT))
		{
			if (isInputPort)
				return new PointD((centerPoint.x - SbgnPDConstants.RIGID_EDGE_LENGTH), centerPoint.y);
			else
				return new PointD((centerPoint.x + SbgnPDConstants.RIGID_EDGE_LENGTH), centerPoint.y);
		}
		else if (orientation.equals(Orientation.RIGHT_TO_LEFT))
		{
			if (isInputPort)
				return new PointD((centerPoint.x + SbgnPDConstants.RIGID_EDGE_LENGTH), centerPoint.y);
			else
				return new PointD((centerPoint.x - SbgnPDConstants.RIGID_EDGE_LENGTH), centerPoint.y);
		}
		else if (orientation.equals(Orientation.TOP_TO_BOTTOM))
		{
			if (isInputPort)
				return new PointD(centerPoint.x, (centerPoint.y - SbgnPDConstants.RIGID_EDGE_LENGTH));
			else
				return new PointD(centerPoint.x, (centerPoint.y + SbgnPDConstants.RIGID_EDGE_LENGTH));
		}
		else if (orientation.equals(Orientation.BOTTOM_TO_TOP))
		{
			if (isInputPort)
				return new PointD(centerPoint.x, (centerPoint.y + SbgnPDConstants.RIGID_EDGE_LENGTH));
			else
				return new PointD(centerPoint.x, (centerPoint.y - SbgnPDConstants.RIGID_EDGE_LENGTH));
		}

		return null;
	}

	private PointD findPortTargetPoint(boolean isInputPort, Orientation orientation, PointD inputPort, PointD outputPort)
	{
		double idealEdgeLength = ((CoSELayout) myLayout).idealEdgeLength;
		if (orientation.equals(Orientation.LEFT_TO_RIGHT))
		{
			if (isInputPort)
				return new PointD((inputPort.x - idealEdgeLength), inputPort.y);
			else
				return new PointD((outputPort.x + idealEdgeLength), outputPort.y);
		}
		else if (orientation.equals(Orientation.RIGHT_TO_LEFT))
		{
			if (isInputPort)
				return new PointD((inputPort.x + idealEdgeLength), inputPort.y);
			else
				return new PointD((outputPort.x - idealEdgeLength), outputPort.y);
		}
		else if (orientation.equals(Orientation.TOP_TO_BOTTOM))
		{
			if (isInputPort)
				return new PointD(inputPort.x, (inputPort.y - idealEdgeLength));
			else
				return new PointD(outputPort.x, (outputPort.y + idealEdgeLength));
		}
		else if (orientation.equals(Orientation.BOTTOM_TO_TOP))
		{
			if (isInputPort)
				return new PointD(inputPort.x, (inputPort.y + idealEdgeLength));
			else
				return new PointD(outputPort.x, (outputPort.y - idealEdgeLength));
		}

		return null;
	}

	/**
	 * This method finds the best orientation for each process node by calculating the total angle
	 * between port/process and the edges for each orientation. The aim is to minimize the total
	 * angle sum.
	 */
	private void calcPropOrientedEdgesByAngleAmount()
	{
		properlyOrientedCoSEEdgeCnt = 0;
		totalEdgeCount = 0;

		PointD inputPort = new PointD();
		PointD outputPort = new PointD();
		PointD inputPortTarget = new PointD();
		PointD outputPortTarget = new PointD();

		double bestResult = Double.MIN_VALUE;
		double minAngleSum = Double.MAX_VALUE;

		double angleSum = 0;
		double angle = 0;

		double appropriateEdgeCnt = 0;
		double approprEffectorCount = 0;
		double notAppropriateEffCnt = 0;
		double notAppropriateEdgeCnt = 0;

		PointD centerPointD = new PointD();

		for (CoSENode node : cosenodes)
		{
			if (node.type.equals(SbgnPDConstants.PROCESS))
			{
				centerPointD = node.getCenter();

				prodNodes.clear();
				consNodes.clear();
				effNodes.clear();

				for (Object edgeObj : node.getEdges())
				{
					if (((CoSEEdge) edgeObj).type.equals(SbgnPDConstants.PRODUCTION))
						prodNodes.add((CoSENode) ((CoSEEdge) edgeObj).getTarget());
					else if (((CoSEEdge) edgeObj).type.equals(SbgnPDConstants.CONSUMPTION))
						consNodes.add((CoSENode) ((CoSEEdge) edgeObj).getSource());
					else if (isEffector((CoSEEdge) edgeObj))
						effNodes.add((CoSENode) ((CoSEEdge) edgeObj).getSource());
				}

				bestResult = Double.MIN_VALUE;
				double bestEffCnt = Double.MIN_VALUE;
				minAngleSum = Double.MAX_VALUE;

				// those lists are used to remember properly orientation info
				// for best orientation - for drawing
				ArrayList<Boolean> rememberPropList = new ArrayList<Boolean>();
				ArrayList<Boolean> bestPropList = new ArrayList<Boolean>();

				for (Orientation orient : orientationList)
				{
					appropriateEdgeCnt = 0;
					notAppropriateEdgeCnt = 0;
					approprEffectorCount = 0;
					notAppropriateEffCnt = 0;
					angleSum = 0;
					angle = 0;

					inputPort = findPortLocation(true, centerPointD, orient);
					outputPort = findPortLocation(false, centerPointD, orient);

					inputPortTarget = findPortTargetPoint(true, orient, inputPort, outputPort);
					outputPortTarget = findPortTargetPoint(false, orient, inputPort, outputPort);

					rememberPropList = new ArrayList<Boolean>();
					for (CoSENode node2 : consNodes)
					{
						angle = IGeometry.calculateAngle(inputPortTarget, inputPort, node2.getCenter());
						angleSum += angle;
						if (angle <= SbgnPDConstants.ANGLE_TOLERANCE)
						{
							appropriateEdgeCnt++;
							rememberPropList.add(true);
						}
						else
						{
							notAppropriateEdgeCnt++;
							rememberPropList.add(false);
						}
					}

					for (CoSENode node2 : prodNodes)
					{
						angle = IGeometry.calculateAngle(outputPortTarget, outputPort, node2.getCenter());
						angleSum += angle;

						if (angle <= SbgnPDConstants.ANGLE_TOLERANCE)
						{
							appropriateEdgeCnt++;
							rememberPropList.add(true);
						}
						else
						{
							notAppropriateEdgeCnt++;
							rememberPropList.add(false);
						}
					}

					for (CoSENode eff : effNodes) // get all effectors
					{
						angle = calculateEffectorAngle(orient, centerPointD, eff);
						angleSum += angle;

						if (angle <= SbgnPDConstants.EFFECTOR_ANGLE_TOLERANCE)
						{
							approprEffectorCount++;
							rememberPropList.add(true);
						}
						else
						{
							notAppropriateEffCnt++;
							rememberPropList.add(false);
						}
					}

					if (angleSum < minAngleSum)
					{
						minAngleSum = angleSum;
						bestResult = appropriateEdgeCnt;
						bestEffCnt = approprEffectorCount;
						bestPropList = rememberPropList;
						node.orient = orient;
						node.OKCount = bestResult;
					}
				}

				for (int i = 0; i < consNodes.size(); i++)
				{
					consNodes.get(i).isOrientationProper = bestPropList.get(i);
				}
				for (int i = 0; i < prodNodes.size(); i++)
				{
					prodNodes.get(i).isOrientationProper = bestPropList.get(i + consNodes.size());
				}
				for (int i = 0; i < effNodes.size(); i++)
				{
					effNodes.get(i).isOrientationProper = bestPropList.get(i + (consNodes.size() + prodNodes.size()));
				}

				properlyOrientedCoSEEdgeCnt += bestResult + bestEffCnt;
				totalEdgeCount += (appropriateEdgeCnt + notAppropriateEdgeCnt + approprEffectorCount + notAppropriateEffCnt);

			}
		}
	}

	/**
	 * This method finds the best orientation for each process node by calculating the number of
	 * properly oriented edges for each orientation. The aim is to maximize the number of properly
	 * oriented edges.
	 */
	private void calcPropOrientedCoSEEdgesByNumber()
	{
		properlyOrientedCoSEEdgeCnt = 0;
		totalEdgeCount = 0;

		PointD inputPort = new PointD();
		PointD outputPort = new PointD();
		PointD inputPortTarget = new PointD();
		PointD outputPortTarget = new PointD();

		double bestResult = Double.MIN_VALUE;
		double appropriateEdgeCnt = 0;
		double approprEffectorCount = 0;
		double notAppropriateEffCnt = 0;
		double notAppropriateEdgeCnt = 0;

		PointD centerPointD = new PointD();

		for (CoSENode node : cosenodes)
		{
			if (node.type.equals(SbgnPDConstants.PROCESS))
			{
				centerPointD = node.getCenter();

				prodNodes.clear();
				consNodes.clear();
				effNodes.clear();

				for (Object edgeObj : node.getEdges())
				{
					if (((CoSEEdge) edgeObj).type.equals(SbgnPDConstants.PRODUCTION))
						prodNodes.add((CoSENode) ((CoSEEdge) edgeObj).getTarget());
					else if (((CoSEEdge) edgeObj).type.equals(SbgnPDConstants.CONSUMPTION))
						consNodes.add((CoSENode) ((CoSEEdge) edgeObj).getSource());
					else if (isEffector((CoSEEdge) edgeObj))
						effNodes.add((CoSENode) ((CoSEEdge) edgeObj).getSource());
				}

				bestResult = Double.MIN_VALUE;
				double bestEffCnt = Double.MIN_VALUE;

				// those lists are used to remember properly orientation info
				// for best orientation - for drawing
				ArrayList<Boolean> rememberPropList = new ArrayList<Boolean>();
				ArrayList<Boolean> bestPropList = new ArrayList<Boolean>();

				for (Orientation orient : orientationList)
				{
					appropriateEdgeCnt = 0;
					notAppropriateEdgeCnt = 0;
					approprEffectorCount = 0;
					notAppropriateEffCnt = 0;

					inputPort = findPortLocation(true, centerPointD, orient);
					outputPort = findPortLocation(false, centerPointD, orient);

					inputPortTarget = findPortTargetPoint(true, orient, inputPort, outputPort);
					outputPortTarget = findPortTargetPoint(false, orient, inputPort, outputPort);

					rememberPropList = new ArrayList<Boolean>();
					for (CoSENode node2 : consNodes)
					{
						if (IGeometry.calculateAngle(inputPortTarget, inputPort, node2.getCenter()) <= SbgnPDConstants.ANGLE_TOLERANCE)
						{
							appropriateEdgeCnt++;
							rememberPropList.add(true);
						}
						else
						{
							notAppropriateEdgeCnt++;
							rememberPropList.add(false);
						}
					}

					for (CoSENode node2 : prodNodes)
					{
						if (IGeometry.calculateAngle(outputPortTarget, outputPort, node2.getCenter()) <= SbgnPDConstants.ANGLE_TOLERANCE)
						{
							appropriateEdgeCnt++;
							rememberPropList.add(true);
						}
						else
						{
							notAppropriateEdgeCnt++;
							rememberPropList.add(false);
						}
					}

					for (CoSENode eff : effNodes) // get all effectors
					{
						if (calculateEffectorAngle(orient, centerPointD, eff) <= SbgnPDConstants.EFFECTOR_ANGLE_TOLERANCE)
						{
							approprEffectorCount++;
							rememberPropList.add(true);
						}
						else
						{
							notAppropriateEffCnt++;
							rememberPropList.add(false);
						}
					}

					if (appropriateEdgeCnt > bestResult)
					{
						bestResult = appropriateEdgeCnt;
						bestEffCnt = approprEffectorCount;
						bestPropList = rememberPropList;
						node.orient = orient;
						node.OKCount = bestResult;
					}
				}

				for (int i = 0; i < consNodes.size(); i++)
				{
					consNodes.get(i).isOrientationProper = bestPropList.get(i);
				}
				for (int i = 0; i < prodNodes.size(); i++)
				{
					prodNodes.get(i).isOrientationProper = bestPropList.get(i + consNodes.size());
				}
				for (int i = 0; i < effNodes.size(); i++)
				{
					effNodes.get(i).isOrientationProper = bestPropList.get(i + (consNodes.size() + prodNodes.size()));
				}

				properlyOrientedCoSEEdgeCnt += bestResult + bestEffCnt;
				totalEdgeCount += (appropriateEdgeCnt + notAppropriateEdgeCnt + approprEffectorCount + notAppropriateEffCnt);

			}
		}
	}

	private boolean isEffector(CoSEEdge edge)
	{
		if (edge.type.equals(SbgnPDConstants.MODULATION) || edge.type.equals(SbgnPDConstants.STIMULATION)
				|| edge.type.equals(SbgnPDConstants.CATALYSIS) || edge.type.equals(SbgnPDConstants.INHIBITION)
				|| edge.type.equals(SbgnPDConstants.NECESSARY_STIMULATION))
			return true;

		return false;
	}

	private void reportCoSEResult()
	{
		System.out.println("CoSE: " + properlyOrientedCoSEEdgeCnt + " out of " + totalEdgeCount + ": "
				+ (properlyOrientedCoSEEdgeCnt / totalEdgeCount));
	}

	private void reportSBGNResult()
	{
		System.out.println("SBGN: " + ((SbgnPDLayout) myLayout).properlyOrientedEdgeCount + " out of "
				+ ((SbgnPDLayout) myLayout).totalEdgeCountToBeOriented + ": " + ((SbgnPDLayout) myLayout).successRatio
				+ " -> " + ((SbgnPDLayout) myLayout).enhancedRatio);
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
