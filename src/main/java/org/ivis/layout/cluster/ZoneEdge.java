package org.ivis.layout.cluster;

import org.ivis.layout.cose.CoSEEdge;
import org.ivis.util.IGeometry;
import org.ivis.util.IMath;


/**
 * This class implements Zone Graph specific data and functionality for edges.
 *
 * @author Can Cagdas Cengiz
 *
 * Copyright: i-Vis Research Group, Bilkent University, 2007 - present
 */
public class ZoneEdge extends CoSEEdge
{
// -----------------------------------------------------------------------------
// Section: Constructors and initializations
// -----------------------------------------------------------------------------
	/**
	 * Constructor
	 */
	public ZoneEdge(ZoneNode source, ZoneNode target, Object vEdge)
	{
		super(source, target, vEdge);
	}
	
	public ZoneEdge()
	{
		this(null, null, null);
		//this.idealLength = this.idealLength * 5;
	}
	
	public void updateLength()
	{
		double[] clipPointCoordinates = new double[4];

		this.isOverlapingSourceAndTarget =
			IGeometry.getPolygonIntersection(((ZoneNode) (this.target)).polygon,
					((ZoneNode) (this.source)).polygon,
					clipPointCoordinates);

		if (!this.isOverlapingSourceAndTarget)
		{
			// target clip point minus source clip point gives us length

			this.lengthX = clipPointCoordinates[0] - clipPointCoordinates[2];
			this.lengthY = clipPointCoordinates[1] - clipPointCoordinates[3];

			if (Math.abs(this.lengthX) < 1.0)
			{
				this.lengthX = IMath.sign(this.lengthX);
			}

			if (Math.abs(this.lengthY) < 1.0)
			{
				this.lengthY = IMath.sign(this.lengthY);
			}

			this.length = Math.sqrt(
				this.lengthX * this.lengthX + this.lengthY * this.lengthY);
			//System.out.println("Here");
		}	
		
		//System.out.println("Zone Edge Source " + this.getSource().label + ", Target " 
			//	+ this.getTarget().label + " Lenght" + this.length); // test
	}
	
	public void updateLengthSimple()
	{
		this.updateLength();
	}
}