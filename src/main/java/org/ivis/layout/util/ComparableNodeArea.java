package org.ivis.layout.util;

import org.ivis.layout.sbgn.SbgnPDNode;

/**
 * Sort the given nodes according to their area calculations.
 * 
 */
public class ComparableNodeArea implements Comparable
{
	private SbgnPDNode node;

	public ComparableNodeArea(SbgnPDNode node)
	{
		this.node = node;
	}

	public SbgnPDNode getNode()
	{
		return node;
	}

	/**
	 * Descending order of areas
	 */
	public int compareTo(Object o)
	{
		return (new Double(((ComparableNodeArea) o).getNode().getWidth()
				* ((ComparableNodeArea) o).getNode().getHeight())
				.compareTo(node.getWidth() * node.getHeight()));
	}
}