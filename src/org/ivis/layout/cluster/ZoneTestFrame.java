package org.ivis.layout.cluster;

import javax.swing.*;

import org.ivis.layout.Cluster;
import org.ivis.util.IGeometry;
import org.ivis.util.PointD;

import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

class ZoneTestFrame extends JFrame
{
	ZoneGraph zoneGraph;
	ArrayList<Cluster> clusters;
	Color [] colors;
    public ZoneTestFrame(ArrayList<Cluster> c, ZoneGraph zoneGraph)
    {
        JPanel panel=new JPanel();
        getContentPane().add(panel);
        setSize(1280,960);
        this.setResizable(true);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.zoneGraph = zoneGraph;
        this.clusters = c;
        colors = new Color [this.clusters.size()];
        Random rnd = new Random();
        for (int i = 0; i < this.clusters.size(); i++)
        {
        	colors[i] = new Color(rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
        }
    }

    public void paint(Graphics g) 
    {
        super.paint(g); 
        Graphics2D g2 = (Graphics2D) g;
        //Random rnd = new Random();
        
        double min_x = Double.POSITIVE_INFINITY;
        double min_y = Double.POSITIVE_INFINITY;
        
        double max_x = Double.NEGATIVE_INFINITY;
        double max_y = Double.NEGATIVE_INFINITY;
        
        double drawConstant = 1.0;

        for (Object o : this.clusters)
        {
        	Cluster c = (Cluster) o;
        	ArrayList<PointD> polygon = c.getPolygon();
        	for (int i = 1; i < polygon.size(); i++)
        	{ 
        		PointD pt1 = polygon.get(i-1);
        		PointD pt2 = polygon.get(i); 
        		if(pt1.x < min_x)
        			min_x = pt1.x;
        		if(pt2.x < min_x)
        			min_x = pt2.x;
        		
        		if(pt1.y < min_y)
        			min_y = pt1.y;
        		if(pt2.y < min_y)
        			min_y = pt2.y; 
        		
        		if(pt1.x > max_x)
        			max_x = pt1.x;
        		if(pt2.x > max_x)
        			max_x = pt2.x;
        		
        		if(pt1.y > max_y)
        			max_y = pt1.y;
        		if(pt2.y > max_y)
        			max_y = pt2.y; 
        	}  
        }
        
        if ((max_x-min_x+40 > 1280) || (max_y-min_y+40 > 960))
        {
        	double propX = 1280 / (max_x-min_x+40);
        	double propY = 960 / (max_y-min_y+40);
        	
        	if (propX > propY)
        	{
        		drawConstant = propY;
        	}
        	else
        	{
        		drawConstant = propX;
        	}
        }
        
        g2.setStroke(new BasicStroke(3));
        int count = 0;
        for (Object o : this.clusters)
        {
        	g2.setColor(this.colors[count]);
        	Cluster c = (Cluster) o;
        	ArrayList<PointD> polygon = c.getPolygon();
        	for (int i = 1; i < polygon.size(); i++)
        	{
        		PointD pt1 = polygon.get(i-1);
        		PointD pt2 = polygon.get(i);
        		//System.out.println("Pt1: " + (pt1.x-min_x+40) + " " + (pt1.y-min_y+40));
        		//System.out.println("Pt2: " + (pt2.x-min_x+40) + " " + (pt2.y-min_y+40));
        		double p1x = drawConstant * (pt1.x-min_x+60);
        		double p1y = drawConstant * (pt1.y-min_y+60);
        		double p2x = drawConstant * (pt2.x-min_x+60);
        		double p2y = drawConstant * (pt2.y-min_y+60);
        		
        		Line2D lin = new Line2D.Double(p1x, p1y, p2x, p2y);
                g2.draw(lin);       		
        	}  
        	count++;
        }
        
        for (Object o : this.zoneGraph.getEdges())
        {
        	ZoneEdge edge = (ZoneEdge) o;
        	ZoneNode target = (ZoneNode) edge.getTarget();
        	ZoneNode source = (ZoneNode) edge.getSource();
        	double [] result = {0, 0, 0, 0};
        	IGeometry.getPolygonIntersection(target.polygon, source.polygon, result);
        	
    		double p1x = drawConstant * (result[0]-min_x+60);
    		double p1y = drawConstant * (result[1]-min_y+60);
    		double p2x = drawConstant * (result[2]-min_x+60);
    		double p2y = drawConstant * (result[3]-min_y+60);
        	Line2D line = new Line2D.Double(p1x, p1y, p2x, p2y);
        	g2.setColor(new Color(0, 0, 0));
        	g2.draw(line); 
        	//System.out.println("Resulting edge drawn is between " + result[0] + "," + result[1]+ " & " +result[2]+ "," +result[3]);
        }        
    }
}