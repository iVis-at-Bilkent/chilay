package org.ivis.layout.sbgn;

import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;

import org.ivis.layout.LGraphManager;
import org.ivis.layout.LNode;
import org.ivis.util.PointD;

public class SbgnProcessNode extends SbgnPDNode
{
	boolean isDebugON = false;
	public boolean isHighlighted = false;
	protected SbgnPDNode parentCompound;
	private SbgnPDNode inputPort;
	private SbgnPDNode outputPort;
	int rotationPriority = 0; // 1:90degree, 2:180 degree, 0:no rotation

	public double idealEdgeLength = 0;
	
	ArrayList<SbgnPDNode> inputNeighborNodeList;
	ArrayList<SbgnPDNode> outputNeighborNodeList;
	
	ArrayList<SbgnPDEdge> inputNeighborEdgeList;
	ArrayList<SbgnPDEdge> outputNeighborEdgeList;
	

	/**
	 * This variable stores the values of the perpendicular components of the
	 * forces acting on port nodes. (90-degree rotation)
	 */
	protected double netRotationalForce;
	protected Orientation orientation;

	public SbgnProcessNode(LGraphManager gm, Object vNode)
	{
		super(gm, vNode);
		this.netRotationalForce = 0;
		inputNeighborNodeList = new ArrayList<SbgnPDNode>();
		outputNeighborNodeList = new ArrayList<SbgnPDNode>();
		inputNeighborEdgeList = new ArrayList<SbgnPDEdge>();
		outputNeighborEdgeList = new ArrayList<SbgnPDEdge>();
	}

	public SbgnProcessNode(LGraphManager gm, Point loc, Dimension size,
			LNode vNode, String type)
	{
		super(gm, loc, size, vNode, type);
		this.netRotationalForce = 0;
		inputNeighborNodeList = new ArrayList<SbgnPDNode>();
		outputNeighborNodeList = new ArrayList<SbgnPDNode>();
		inputNeighborEdgeList = new ArrayList<SbgnPDEdge>();
		outputNeighborEdgeList = new ArrayList<SbgnPDEdge>();
	}

	public void copyNode(SbgnProcessNode s, LGraphManager graphManager)
	{
		this.type = s.type;
		this.label = s.label;
		this.isHighlighted = s.isHighlighted;
		this.parentCompound = s.parentCompound;
		this.setInputPort(s.getInputPort());
		this.setOutputPort(s.getOutputPort());
		this.setCenter(s.getCenterX(), s.getCenterY());
		this.setChild(s.getChild());
		this.setHeight(s.getHeight());
		this.setLocation(s.getLocation().x, s.getLocation().y);
		this.setNext(s.getNext());
		this.setOwner(s.getOwner());
		this.setPred1(s.getPred1());
		this.setPred2(s.getPred2());
		this.setWidth(s.getWidth());
	}

	public void copyFromSBGNPDNode(SbgnPDNode s, LGraphManager graphManager)
	{
		this.type = s.type;
		this.label = s.label;
		this.setCenter(s.getCenterX(), s.getCenterY());
		this.setChild(s.getChild());
		this.setHeight(s.getHeight());
		this.setLocation(s.getLocation().x, s.getLocation().y);
		this.setNext(s.getNext());
		this.setOwner(s.getOwner());
		this.setPred1(s.getPred1());
		this.setPred2(s.getPred2());
		this.setWidth(s.getWidth());

		// copy edges
		for (Object o : s.getEdges())
		{
			SbgnPDEdge edge = (SbgnPDEdge) o;
			SbgnPDEdge newEdge = new SbgnPDEdge((SbgnPDNode) edge.getSource(),
					(SbgnPDNode) edge.getTarget(), null, edge.type);

			newEdge.copy(edge);
			
			if (edge.getSource().equals(s))
			{
				newEdge.setSource(this);
			}
			else if (edge.getTarget().equals(s))
			{
				newEdge.setTarget(this);
			}

			// add new edge to the graph manager.
			graphManager.add(newEdge, newEdge.getSource(), newEdge.getTarget());
		}
	}

	public void setConnectedNodes(SbgnPDNode parentCompound,
			SbgnPDNode inputPort, SbgnPDNode outputPort)
	{
		this.parentCompound = parentCompound;
		this.parentCompound.isDummyCompound = true;
		this.setInputPort(inputPort);
		this.setOutputPort(outputPort);
		this.orientation = Orientation.LEFT_TO_RIGHT;

		// initial placement. place input to the left of the process node,
		// output to the right
		outputPort.setCenter(this.getCenterX()
				+ SbgnPDConstants.RIGID_EDGE_LENGTH, this.getCenterY());
		inputPort.setCenter(this.getCenterX()
				- SbgnPDConstants.RIGID_EDGE_LENGTH, this.getCenterY());
	}

	public boolean checkRotationAvailability()
	{
		// first check if a 180-degree is possible. if you rotate 180-degree, do
		// not rotate 90-degree.
		netRotationalForce /= (SbgnPDConstants.ROTATIONAL_FORCE_ITERATION_COUNT
				* (this.inputNeighborNodeList.size() + this.outputNeighborNodeList.size()));

		if (isSwapAvailable())
			rotationPriority = 2;
		else if (Math.abs(netRotationalForce) > SbgnPDConstants.ROTATION_90_DEGREE)
			rotationPriority = 1;
		else
			rotationPriority = 0;
		
		if(rotationPriority == 0)
			return false;
		else
			return true;
	}
	
	/**
	 * This method rotates the associated compound of the given process node.
	 */
	public void applyRotation()
	{
		if (isHighlighted && isDebugON)
		{
			System.out.println("****VALUES for " + label + " ****");
			System.out.println("netRotationalForce: " + netRotationalForce);
			System.out.println("prev orientation: " + orientation);
		}

		if (rotationPriority == 1)
		{
			
			if (orientation.equals(Orientation.TOP_TO_BOTTOM))
			{
				if (netRotationalForce > SbgnPDConstants.ROTATION_90_DEGREE)
				{
					rotateCompound(90);
					orientation = Orientation.RIGHT_TO_LEFT;
				}
				else if (netRotationalForce < -SbgnPDConstants.ROTATION_90_DEGREE)
				{
					rotateCompound(-90);
					orientation = Orientation.LEFT_TO_RIGHT;
				}
			}
			else if (orientation.equals(Orientation.BOTTOM_TO_TOP))
			{
				if (netRotationalForce < -SbgnPDConstants.ROTATION_90_DEGREE)
				{
					rotateCompound(90);
					orientation = Orientation.LEFT_TO_RIGHT;
				}
				else if (netRotationalForce > SbgnPDConstants.ROTATION_90_DEGREE)
				{
					rotateCompound(-90);
					orientation = Orientation.RIGHT_TO_LEFT;
				}
			}
			else if (orientation.equals(Orientation.RIGHT_TO_LEFT))
			{
				if (netRotationalForce > SbgnPDConstants.ROTATION_90_DEGREE)
				{
					rotateCompound(90);
					orientation = Orientation.BOTTOM_TO_TOP;
				}
				else if (netRotationalForce < -SbgnPDConstants.ROTATION_90_DEGREE)
				{
					rotateCompound(-90);
					orientation = Orientation.TOP_TO_BOTTOM;
				}
			}
			else if (orientation.equals(Orientation.LEFT_TO_RIGHT))
			{
				if (netRotationalForce < -SbgnPDConstants.ROTATION_90_DEGREE)
				{
					rotateCompound(-90);
					orientation = Orientation.BOTTOM_TO_TOP;
				}
				else if (netRotationalForce > SbgnPDConstants.ROTATION_90_DEGREE)
				{
					rotateCompound(90);
					orientation = Orientation.TOP_TO_BOTTOM;
				}
			}
		}

		else if (rotationPriority == 2)
		{
			if (isHighlighted && isDebugON)
			{
				System.out.println("SWAPPED");
			}

			PointD tempCenter = inputPort.getCenter();
			inputPort.setCenter(getOutputPort().getCenterX(),
					outputPort.getCenterY());
			outputPort.setCenter(tempCenter.x, tempCenter.y);

			if (orientation.equals(Orientation.TOP_TO_BOTTOM))
				orientation = Orientation.BOTTOM_TO_TOP;
			else if (orientation.equals(Orientation.BOTTOM_TO_TOP))
				orientation = Orientation.TOP_TO_BOTTOM;
			else if (orientation.equals(Orientation.LEFT_TO_RIGHT))
				orientation = Orientation.RIGHT_TO_LEFT;
			else if (orientation.equals(Orientation.RIGHT_TO_LEFT))
				orientation = Orientation.LEFT_TO_RIGHT;
		}
//		if (isHighlighted && isDebugON)
//		{
//			System.out.println("last orientation: " + orientation);
//			System.out.println();
//		}

		this.calculateRotationalForces(idealEdgeLength);
		
		this.netRotationalForce = 0;
	}

	private boolean isSwapAvailable()
	{
		double obtuseAngleCnt = 0;
		double acuteAngleCnt = 0;
//		double averageAngle = 0.0;
		
		for(SbgnPDEdge edge : inputNeighborEdgeList)
		{
			if(Math.abs(edge.correspondingAngle) > 90)
				obtuseAngleCnt++;
			else
				acuteAngleCnt++;
		}
		
		for(SbgnPDEdge edge : outputNeighborEdgeList)
		{
			if(Math.abs(edge.correspondingAngle) > 90)
				obtuseAngleCnt++;
			else
				acuteAngleCnt++;
		}
		
		if(obtuseAngleCnt / (obtuseAngleCnt + acuteAngleCnt) > SbgnPDConstants.ROTATION_180_DEGREE)
			return true;
		else
			return false;
		
//		for(SbgnPDEdge edge : outputNeighborEdgeList)
//			averageAngle += Math.abs(edge.correspondingAngle);
//		
//		averageAngle /= (inputNeighborEdgeList.size() + outputNeighborEdgeList.size());
//		
//		if(this.isHighlighted)
//			System.out.println("averageangle: " + averageAngle);
//		if(averageAngle > SbgnPDConstants.ROTATION_180_DEGREE)
//			return true;
//		else
//			return false;
	}

	/**
	 * Given a compound node, this method recursively rotates the compound node
	 * and its members.
	 */
	private void rotateCompound(int rotationDegree)
	{
		if (this.isHighlighted && isDebugON)
		{
			System.out.println("ROTATED");
		}

		this.rotateNode(this.getCenter(), rotationDegree);
		inputPort.rotateNode(this.getCenter(), rotationDegree);
		outputPort.rotateNode(this.getCenter(), rotationDegree);
		this.parentCompound.updateBounds();
		
		if (isHighlighted && isDebugON)
		{
			System.out.println("new Orientation: " + this.orientation);
		}
	}

	public void transferForces()
	{
		parentCompound.springForceX += this.springForceX
				+ inputPort.springForceX + outputPort.springForceX;
		parentCompound.springForceY += this.springForceY
				+ inputPort.springForceY + outputPort.springForceY;
	}

	public double calculateRotationalForces(double idealEdgeLength)
	{
		this.idealEdgeLength = idealEdgeLength;
		double inputRotSum = 0;
		double outputRotSum = 0;
		double result;
		double count = 0;
		
		// if the neighbors of port nodes have not been found, find them.
		if (inputNeighborNodeList.size() == 0 && outputNeighborNodeList.size() == 0)
		{
			for (Object o : inputPort.getEdges())
			{
				SbgnPDEdge edge = (SbgnPDEdge) o;
				if (!(edge.type.equals(SbgnPDConstants.RIGID_EDGE)))
				{
					inputNeighborNodeList.add((SbgnPDNode) edge.getSource());
					inputNeighborEdgeList.add(edge);
				}
			}

			for (Object o : outputPort.getEdges())
			{
				SbgnPDEdge edge = (SbgnPDEdge) o;
				if (!(edge.type.equals(SbgnPDConstants.RIGID_EDGE)))
				{
					outputNeighborNodeList.add((SbgnPDNode) edge.getTarget());
					outputNeighborEdgeList.add(edge);
				}
			}
		}

		PointD inputPortTarget = findPortTargetPoint(true, idealEdgeLength);
		PointD outputPortTarget = findPortTargetPoint(false, idealEdgeLength);
		
		for (int nodeIndex = 0; nodeIndex < inputNeighborNodeList.size(); nodeIndex++)
		{			
			result = calculateRotationalForce(true, nodeIndex, inputPortTarget);
			if(Math.abs(result) < SbgnPDConstants.ANGLE_TOLERANCE)
				count++;
			inputRotSum += result;
		}
		for (int nodeIndex = 0; nodeIndex < outputNeighborNodeList.size(); nodeIndex++)
		{		
			result = calculateRotationalForce(false, nodeIndex, outputPortTarget);
			if(Math.abs(result) < SbgnPDConstants.ANGLE_TOLERANCE)
				count++;
			outputRotSum += result;
		}

//		if(isHighlighted && isDebugON)
//			System.out.println("inputRot: " + inputRotSum + " outputSum: " + outputRotSum);
		this.netRotationalForce += (inputRotSum - outputRotSum);
		
		return count;
	}
	
	private double calculateRotationalForce(boolean isInputPort,
			int nodeIndex, PointD targetPoint)
	{
		SbgnPDNode node;
		double angleValue = 0.0;
		
		if(isInputPort)
			node = inputNeighborNodeList.get(nodeIndex);
		else
			node = outputNeighborNodeList.get(nodeIndex);
		
		PointD centerPoint;
		PointD point1, point2;
		if (isInputPort)
			centerPoint = inputPort.getCenter();
		else
			centerPoint = outputPort.getCenter();

		point1 = new PointD(targetPoint.x - centerPoint.x, targetPoint.y
				- centerPoint.y);
		point2 = new PointD(node.getCenterX() - centerPoint.x,
				node.getCenterY() - centerPoint.y);

		if(Math.abs(point1.x) < 0)
			point1.x = 0.0001;
		if(Math.abs(point1.y) < 0)
			point1.y = 0.0001;				
		
		angleValue = (point1.x * point2.x + point1.y * point2.y)
						/ (Math.sqrt(point1.x * point1.x + point1.y * point1.y)
						   * Math.sqrt(point2.x * point2.x + point2.y * point2.y));
				
		double angle = Math.abs(Math.toDegrees(Math.acos(angleValue)));

		if (isInputPort)
			angle *= isLeft(targetPoint, centerPoint, node.getCenter(),
					SbgnPDConstants.INPUT_PORT);
		else
			angle *= isLeft(targetPoint, centerPoint, node.getCenter(),
					SbgnPDConstants.OUTPUT_PORT);

//		 if(this.isHighlighted)
//		 {
//			 System.out.println("angle between " + this.label + "," + node.label+
//			 " : " + angle);
//		 }
		 	
		// do not add rotational force for perfectly fine located nodes
		if(isInputPort)
			inputNeighborEdgeList.get(nodeIndex).correspondingAngle = (int)angle;
		else
			outputNeighborEdgeList.get(nodeIndex).correspondingAngle = (int)angle;
		
		
		if (Math.abs(angle) < SbgnPDConstants.ANGLE_TOLERANCE)
		{			
			if(isInputPort)
				inputNeighborEdgeList.get(nodeIndex).isProperlyOriented = true;
			else
				outputNeighborEdgeList.get(nodeIndex).isProperlyOriented = true;
			
			return angle;
		}
		else
		{
			if(isInputPort)
				inputNeighborEdgeList.get(nodeIndex).isProperlyOriented = false;
			else
				outputNeighborEdgeList.get(nodeIndex).isProperlyOriented = false;
			
			return angle;
		}
	}

	public int isLeft(PointD a, PointD b, PointD c, String type)
	{
		if (((b.x - a.x) * (c.y - a.y) - (b.y - a.y) * (c.x - a.x)) > 0)
		{
			// left turn
			if (this.orientation.equals(Orientation.TOP_TO_BOTTOM))
			{
				if(type.equals(SbgnPDConstants.INPUT_PORT))
					return -1;
				else if(type.equals(SbgnPDConstants.OUTPUT_PORT))
					return 1;
			}
			else if (this.orientation.equals(Orientation.BOTTOM_TO_TOP))
			{
				if(type.equals(SbgnPDConstants.INPUT_PORT))
					return 1;
				else if(type.equals(SbgnPDConstants.OUTPUT_PORT))
					return -1;
			}
			else if (this.orientation.equals(Orientation.LEFT_TO_RIGHT))
			{
				if(type.equals(SbgnPDConstants.INPUT_PORT))
					return 1;
				else if(type.equals(SbgnPDConstants.OUTPUT_PORT))
					return -1;
				
			}
			else if (this.orientation.equals(Orientation.RIGHT_TO_LEFT))
			{
				if(type.equals(SbgnPDConstants.INPUT_PORT))
					return -1;
				else if(type.equals(SbgnPDConstants.OUTPUT_PORT))
					return 1;
			}
		}
		else
		{
			// right turn
			if (this.orientation.equals(Orientation.TOP_TO_BOTTOM))
			{
				if(type.equals(SbgnPDConstants.INPUT_PORT))
					return 1;
				else if(type.equals(SbgnPDConstants.OUTPUT_PORT))
					return -1;
			}
			else if (this.orientation.equals(Orientation.BOTTOM_TO_TOP))
			{
				if(type.equals(SbgnPDConstants.INPUT_PORT))
					return -1;
				else if(type.equals(SbgnPDConstants.OUTPUT_PORT))
					return 1;
			}
			else if (this.orientation.equals(Orientation.LEFT_TO_RIGHT))
			{
				if(type.equals(SbgnPDConstants.INPUT_PORT))
					return -1;
				else if(type.equals(SbgnPDConstants.OUTPUT_PORT))
					return 1;
				
			}
			else if (this.orientation.equals(Orientation.RIGHT_TO_LEFT))
			{
				if(type.equals(SbgnPDConstants.INPUT_PORT))
					return 1;
				else if(type.equals(SbgnPDConstants.OUTPUT_PORT))
					return -1;
			}
		}
		return 0;
	}

	private PointD findPortTargetPoint(boolean isInputPort,
			double idealEdgeLength)
	{
		if (this.orientation.equals(Orientation.LEFT_TO_RIGHT))
		{
			if (isInputPort)
				return new PointD((inputPort.getCenterX() - idealEdgeLength),
						inputPort.getCenterY());
			else
				return new PointD((outputPort.getCenterX() + idealEdgeLength),
						outputPort.getCenterY());
		}
		else if (this.orientation.equals(Orientation.RIGHT_TO_LEFT))
		{
			if (isInputPort)
				return new PointD((inputPort.getCenterX() + idealEdgeLength),
						inputPort.getCenterY());
			else
				return new PointD((outputPort.getCenterX() - idealEdgeLength),
						outputPort.getCenterY());
		}
		else if (this.orientation.equals(Orientation.TOP_TO_BOTTOM))
		{
			if (isInputPort)
				return new PointD(inputPort.getCenterX(),
						(inputPort.getCenterY() - idealEdgeLength));
			else
				return new PointD(outputPort.getCenterX(),
						(outputPort.getCenterY() + idealEdgeLength));
		}
		else if (this.orientation.equals(Orientation.BOTTOM_TO_TOP))
		{
			if (isInputPort)
				return new PointD(inputPort.getCenterX(),
						(inputPort.getCenterY() + idealEdgeLength));
			else
				return new PointD(outputPort.getCenterX(),
						(outputPort.getCenterY() - idealEdgeLength));
		}

		return null;
	}

	public enum Orientation
	{
		BOTTOM_TO_TOP, TOP_TO_BOTTOM, LEFT_TO_RIGHT, RIGHT_TO_LEFT
	};

	public SbgnPDNode getParentCompound()
	{
		return parentCompound;
	}

	public SbgnPDNode getInputPort()
	{
		return inputPort;
	}

	public void setInputPort(SbgnPDNode inputPort)
	{
		this.inputPort = inputPort;
	}

	public SbgnPDNode getOutputPort()
	{
		return outputPort;
	}

	public void setOutputPort(SbgnPDNode outputPort)
	{
		this.outputPort = outputPort;
	}

}
