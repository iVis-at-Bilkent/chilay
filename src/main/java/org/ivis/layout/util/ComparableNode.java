package org.ivis.layout.util;

import org.ivis.layout.sbgn.SbgnPDNode;

/**
 * Sort the given nodes according to their widths.
 * 
 */
public class ComparableNode implements Comparable
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
		return (new Double(((ComparableNode) o).getNode().getWidth()))
				.compareTo(node.getWidth());
	}
}