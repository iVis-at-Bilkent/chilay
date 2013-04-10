package org.ivis.layout;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;
import org.ivis.layout.LGraphManager;
import org.ivis.layout.cose.CoSELayout;
import org.junit.Ignore;

/**
 * LGraphManager tester
 *
 * @author Ugur Dogrusoz
 */
@Ignore
public class LGraphManagerTest extends TestCase
{
	public void setUp() throws Exception
	{
		super.setUp();
	}

	public void tearDown() throws Exception
	{
		super.tearDown();
	}

	public void testTopology() throws Exception
	{
		Layout layout = new CoSELayout();
		LGraphManager gm = layout.getGraphManager();
		LGraph g1 = gm.addRoot();
		LNode n1 = g1.add(layout.newNode(new String("n1")));
		LNode n2 = g1.add(layout.newNode(new String("n2")));
		LGraph g2 = gm.add(layout.newGraph("G2"), n2);
		LNode n3 = g2.add(layout.newNode(new String("n3")));
		LNode n4 = g2.add(layout.newNode(new String("n4")));
		LEdge e1 = gm.add(layout.newEdge(new String("e1-2")), n1, n2);
		LEdge e2 = gm.add(layout.newEdge(new String("e1-3")), n1, n3);
		gm.printTopology();

		g1.remove(n1);
		gm.printTopology();

		g1.add(n1);
		gm.printTopology();

		gm.add(e1, n1, n2);
		gm.printTopology();

		gm.add(e2, n1, n3);
		gm.printTopology();

		LGraph g3 = layout.newGraph("G3");
		gm.add(g3, n3);
		LNode n5 = g3.add(layout.newNode(new String("n5")));
		LNode n6 = g3.add(layout.newNode(new String("n6")));
		LEdge e3 = g3.add(layout.newEdge(new String("e5-6")), n5, n6);
		LEdge e4 = gm.add(layout.newEdge(new String("e4-6")), n4, n6);
		gm.printTopology();

		gm.remove(g3);
		gm.printTopology();

		gm.add(g3, n3);
		gm.printTopology();
	}

	public static Test suite()
	{
		return new TestSuite(LGraphManagerTest.class);
	}
}