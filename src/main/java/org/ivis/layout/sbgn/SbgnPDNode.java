package org.ivis.layout.sbgn;

import java.awt.Dimension;
import java.awt.Point;
import java.util.Iterator;

import org.ivis.layout.LGraphManager;
import org.ivis.layout.LGraphObject;
import org.ivis.layout.LNode;
import org.ivis.layout.cose.CoSENode;

/**
 * This class implements SBGN specific data and functionality for nodes.
 *
 * @author Begum Genc
 *
 * Copyright: i-Vis Research Group, Bilkent University, 2007 - present
 */
public class SbgnPDNode extends CoSENode
{	
	/**
	 *
	 */
	public double relativityConstraintX;

	/**
	 *
	 */
	public double relativityConstraintY;

    /**
     *
     */
    public double orientationX;

    /**
     *
     */
    public double orientationY;
	
    public boolean marked;
    
	/**
	 * Constructor
	 */
	public SbgnPDNode(LGraphManager gm, LNode vNode)
	{
		super(gm, vNode);
		this.marked = false;
	}

	/**
	 * Alternative constructor
	 */
	public SbgnPDNode(LGraphManager gm, Point loc, Dimension size, LNode vNode, String type)
	{
		super(gm, loc, size, vNode);
		this.type = type;
		this.marked = false;
	}
	
	public boolean isComplex()
	{
		return type.equalsIgnoreCase(SbgnPDConstants.COMPLEX);
	}

    protected void updateOrientation()
    {
        this.orientationX = 0;
        this.orientationY = 0;

        Iterator itr = this.edges.iterator();
        while (itr.hasNext())
        {
            double distanceX = 0;
            double distanceY = 0;
            double distance = 0;

            SbgnPDEdge edge = (SbgnPDEdge) itr.next();

            //edge is of type substrate
            if (edge.type.equals(SbgnPDConstants.CONSUMPTION))
            {
                distanceX += this.getCenterX() - edge.getOtherEnd(this).getCenterX();
                distanceY += this.getCenterY() - edge.getOtherEnd(this).getCenterY();
            }
            else if (edge.type.equals(SbgnPDConstants.PRODUCTION))
            {
                distanceX += edge.getOtherEnd(this).getCenterX() - this.getCenterX();
                distanceY += edge.getOtherEnd(this).getCenterY() - this.getCenterY();
            }
            distance =
                    Math.sqrt(distanceX * distanceX + distanceY * distanceY);

            this.orientationX += distanceX / distance;
            this.orientationY += distanceY / distance;
        }


    }
}
