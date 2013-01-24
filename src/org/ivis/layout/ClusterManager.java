package org.ivis.layout;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.ivis.util.PointD;

/**
 * This class represents a cluster manager for layout purposes. A cluster manager
 * maintains a collection of clusters.
 *
 * @author Shatlyk Ashyralyyev 
 * @author Can Cagdas Cengiz
 *
 * Copyright: i-Vis Research Group, Bilkent University, 2007 - present
 */
public class ClusterManager
{
// -----------------------------------------------------------------------------
// Section: Instance variables
// -----------------------------------------------------------------------------
	/*
	 * Clusters maintained by this cluster manager.
	 */
	protected ArrayList clusters;
	
	/*
	 * Boolean variable used for storing whether polygons are used during layout
	 */
	protected boolean polygonUsed;
	
// -----------------------------------------------------------------------------
// Section: Constructors
// -----------------------------------------------------------------------------
	/**
	 * Constructor
	 */
	public ClusterManager()
	{
		this.clusters = new ArrayList<Cluster>();
		
		// default is false
		this.polygonUsed = false;
	}
	
// -----------------------------------------------------------------------------
// Section: Getters and Setters
// -----------------------------------------------------------------------------
	/**
	 * This method returns the list of clusters maintained by this 
	 * cluster manager.
	 */
	public ArrayList<Cluster> getClusters()
	{
		return this.clusters;
	}
	
	/**
	 * This method sets the polygonUsed variable
	 */
	public void setPolygonUsed(boolean polygonUsed)
	{
		this.polygonUsed = polygonUsed;
	}
	
// -----------------------------------------------------------------------------
// Section: Remaining Methods
// -----------------------------------------------------------------------------
	/**
	 * This method creates a new cluster from given clusterID and clusterName.
	 * New cluster is maintained by this cluster manager.
	 */
	public void createCluster(int clusterID, String clusterName)
	{
		// allocate new empty LCluster instance
		Cluster cluster = new Cluster(this, clusterID, clusterName);
		
		// add the cluster into cluster list of this cluster manager
		this.clusters.add(cluster);
	}
	
	/**
	 * This method creates a new cluster from given clusterName.
	 * New cluster is maintained by this cluster manager.
	 */
	public void createCluster(String clusterName)
	{
		// allocate new empty LCluster instance
		Cluster lCluster = new Cluster(this, clusterName);
		
		// add the cluster into cluster list of this cluster manager
		this.clusters.add(lCluster);
	}	
	
	/**
	 * This method adds the given cluster into cluster manager of the graph.
	 */
	public void addCluster(Cluster cluster)
	{
		cluster.setClusterManager(this);
		
		// add the cluster into cluster list of this cluster manager
		this.clusters.add(cluster);
	}
	
	/**
	 * Removes the given cluster from the graph.
	 */
	public void removeCluster(Cluster cluster)
	{	
		// deletes the cluster information from graph
		cluster.delete();
	}
	
	/**
	 * This method checks if the given cluster ID is used before.
	 * If same ID is used before, it returns true, otherwise it returns false.
	 */
	public boolean isClusterIDUsed(int clusterID)
	{
		// get an iterator for cluster list
		Iterator<Cluster> itr = this.clusters.iterator();
		
		// iterate over all clusters and check if clusterID is used before
		while (itr.hasNext())
		{
			Cluster cluster = itr.next();
			
			if (cluster.getClusterID() == clusterID)
			{
				return true;
			}
		}
		
		// not used before
		return false;
	}

	/**
	 * This method returns the cluster with given cluster ID, if no such cluster
	 * it returns null;
	 */
	public Cluster getClusterByID(int clusterID)
	{
		// get an iterator for cluster list
		Iterator<Cluster> itr = this.clusters.iterator();
		
		// iterate over all clusters and check if clusterID is same
		while (itr.hasNext())
		{
			Cluster cluster = itr.next();
			
			if (cluster.getClusterID() == clusterID)
			{
				return cluster;
			}
		}
		
		// no such cluster
		return null;
	}
// -----------------------------------------------------------------------------
// Section: Class variables
// -----------------------------------------------------------------------------
	/*
	 * idCounter is used to set the ID's of clusters. Each time when some
	 * cluster ID is set, it should incremented by 1.
	 */
	public static int idCounter = 1;
	
	/**
	 * This method finds the overlapping clusters. The calculation uses 
	 * the points of cluster polygons. The overlap information is returned
	 * in arraylist of object arrays. An object array has the following 
	 * elements.
	 * [0] = Id of the first cluster
	 * [1] = Id of the second cluster
	 * [2] = Overlap in x-axis
	 * [3] = Overlap in y-axis  
	 */
	public ArrayList<Object []> getOverlapInformation()
	{
		Object [] overlap;
		ArrayList<Object []> overlapInfo;
		Cluster c1;
		Cluster c2;
		
		ArrayList<PointD> p1;
		ArrayList<PointD> p2;
		
		int numberOfClusters;
		
		numberOfClusters = clusters.size();
		overlapInfo = new ArrayList<Object []>();
		
		// loop is optimized such that each pair is compared only once
		
		for (int i = 0; i < numberOfClusters; i++)
		{
			c1 = (Cluster) clusters.get(i);
			p1 = c1.getPolygon();
			
			for (int j = i + 1; j < numberOfClusters; j++)
			{
				c2 = (Cluster) clusters.get(j);
				p2 = c2.getPolygon();
				
				// System.out.println("Checking clusters "+c1.clusterID+ " and "+ c2.clusterID); //test
				
				if ( (p1.size() > 3) && (p2.size() > 3) )
				{
					overlap = convexPolygonOverlap(p1,p2);
					if ((double) overlap[0] != 0.0)
					{
						/*System.out.println("The clusters " + c1.clusterID + 
								" and " + c2.clusterID + " overlap."); // test
						*/
						Object [] newOverlap = new Object[4];
						
						newOverlap[0] = c1.clusterID;
						newOverlap[1] = c2.clusterID;
						newOverlap[2] = overlap[0]; // overlap amount
						newOverlap[3] = overlap[1]; // overlap direction
						
						PointD temp;
						temp = this.getXYProjection(((double) overlap[0]), ((PointD) overlap[1]));
						newOverlap[2] = temp.x; // overlap in x
						newOverlap[3] = temp.y; // overlap in y
						overlapInfo.add(newOverlap);
					}	
				}
			}
			
		}
		return overlapInfo;
	}
	

	
	/**
	 * This method is a dot product operator
	 */
	private double dot(PointD p1, PointD p2)
	{
	    return p1.x*p2.x + p1.y*p2.y;
	}
	
	/**
	 * This method returns false if there is a separating axis 
	 * between the polygons. If there is no separating axis, 
	 * method returns true. 
	 */	
	private Object [] convexPolygonOverlap (ArrayList<PointD> p1, 
			ArrayList<PointD> p2) 
	{
		
		Object [] overlapInfo1;
		Object [] overlapInfo2;
		
		// Using P1's edges for a separating axis
		overlapInfo1 = findSeparatingAxis(p1, p2);
		if ((double) overlapInfo1[0] == 0.0)
			return overlapInfo1;
		
		// Now swap roles, and use P2's edges
		overlapInfo2 = findSeparatingAxis(p2, p1);
		if ((double) overlapInfo2[0] == 0.0)
			return overlapInfo2;
		
		// No separating axis found.  They must overlap.
		// Return the minimum magnitude vector.
		
		if (Math.abs((double) overlapInfo1[0]) < Math.abs((double) overlapInfo2[0]))
		{		
			overlapInfo1[0] = -((double) overlapInfo1[0]);
			return overlapInfo1;
		}
		else
		{			
			return overlapInfo2;
		}
		
	}

	/**
	 * This method tests if two convex polygons overlap. 
	 * Method is based on the separating axis theorem. It only
	 * uses only the edges of the first polygon (polygon "p1")
	 * to build the list of candidate separating axes.
	*/
	private Object [] findSeparatingAxis (ArrayList<PointD> p1, 
										ArrayList<PointD> p2) 
	{
		Object [] overlapInfo;
		
		int aVertCount;
		int bVertCount;
		
		double minOverlap = Double.NEGATIVE_INFINITY;
		PointD minVector = new PointD(0,0);
		overlapInfo = new Object [2];
		
		aVertCount = p1.size();
		bVertCount = p2.size();
		
		
	    // Iterate over all the edges
		int prev = aVertCount-1;
	    
		for (int cur = 0 ; cur < aVertCount ; ++cur)
	    {
	 
	        // Get edge vector.  
	        PointD edge; 
	        double ex = p1.get(cur).x - p1.get(prev).x;
	        double ey = p1.get(cur).y - p1.get(prev).y;
	        edge = new PointD(ex, ey);
	       
	        // Rotate vector 90 degrees (doesn't matter which way) to get
	        // candidate separating axis.
	        PointD v;
	        double vx = edge.y;
	        double vy = -edge.x;
	        v = this.normalizeVector(new PointD(vx,vy));
			
	        if ((v.y < 0.0))
			{
				
				v.x = -(v.x);
				v.y = -(v.y);
				
			}
	        
	        
	        // Gather extents of both polygons projected onto this axis
	        double [] p1Bounds; 
	        double [] p2Bounds;
	        p1Bounds = gatherPolygonProjectionExtents(p1, v);
	        p2Bounds = gatherPolygonProjectionExtents(p2, v);
	 
	        
	        // Is this a separating axis?
	        if (p1Bounds[1] < p2Bounds[0]) 
	        {
	        	
	        	overlapInfo[0] = 0.0;
	        	overlapInfo[1] = minVector;
	        	return overlapInfo;
	        }	        	
	        else
	        {
	        	// negative overlap
	        	double overlap;
	        	overlap = p2Bounds[0] - p1Bounds[1] ; 
	        	if (Math.abs(overlap) < Math.abs(minOverlap))
	        	{
	        		minVector = v;
	        		minOverlap = overlap;
	        		
	        	}
	        }
	        
	        if (p2Bounds[1] < p1Bounds[0])
	        {	
	        	
	        	overlapInfo[0] = 0.0;
	        	overlapInfo[1] = minVector;
	        	return overlapInfo;        	
	        }
	        else
	        {
	        	// positive overlap
	        	double overlap;
	        	overlap = p2Bounds[1] - p1Bounds[0]; 
	        	if (Math.abs(overlap) < Math.abs(minOverlap))
	        	{
	        		minVector = v;
	        		minOverlap = overlap;
	        	}
	        	
	        }
	        // Next edge
	        prev = cur;
	    }
		
		// Failed to find a separating axis
		overlapInfo[0] = minOverlap;
    	overlapInfo[1] = minVector;
    	
    	return overlapInfo;
	    
	}
	
	/**
	* Gather up one-dimensional extents of the projection of the polygon
	* onto this axis.
	*/
	private double [] gatherPolygonProjectionExtents( ArrayList<PointD> p, PointD v) 
	{
	 
		double [] out = new double [2];
		
	    // Initialize extents to a single point, the first vertex
	    out[0] = dot(v, p.get(0));	// min
	    out[1] = dot(v, p.get(0));	// max
	    
	    // Now scan all the rest, growing extents to include them
	    for (int i = 1; i < p.size(); ++i)
	    {
	        double d = dot(v, p.get(i));
	        
	        if (d < out[0]) 
	        	out[0] = d;
	        else if (d > out[1]) 
	        	out[1] = d;
	    }
	    
	    
	    return out;
	}
	
	/**
	* Returns the projection of a given vector on the x-y 
	* cartesian plane. 
	*/
	private PointD getXYProjection(double magnitude, PointD direction)
	{
		double sin = direction.y/(Math.sqrt((direction.x*direction.x)+(direction.y*direction.y)));
		double cos = direction.x/(Math.sqrt((direction.x*direction.x)+(direction.y*direction.y)));
		double x = magnitude * cos;
		double y = magnitude * sin;
		return new PointD(x,y);
	}

	

	/**
	* Given a vector as a PointD object, returns the normalized form of
	* the vector in [-1, 1] scale.  
	*/
	private PointD normalizeVector(PointD v)
	{
		double denom = (v.x * v.x) + (v.y * v.y);
		denom = Math.sqrt(denom);
		double x = v.x / denom;
		double y = v.y / denom;
		return new PointD(x, y);		
	}
	
	
} //end of class


