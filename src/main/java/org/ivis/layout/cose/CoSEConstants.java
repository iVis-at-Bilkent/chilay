package org.ivis.layout.cose;

import org.ivis.layout.fd.FDLayoutConstants;

/**
 * This class maintains the constants used by CoSE layout.
 *
 * @author: Ugur Dogrusoz
 *
 * Copyright: i-Vis Research Group, Bilkent University, 2007 - present
 */
public class CoSEConstants extends FDLayoutConstants
{
// -----------------------------------------------------------------------------
// Section: CoSE layout user options
// -----------------------------------------------------------------------------
	public static final boolean DEFAULT_USE_MULTI_LEVEL_SCALING = false;
    public static final boolean DEFAULT_USE_TWO_PHASE_GRADUAL_SIZE_INCREASE = true;
	
// -----------------------------------------------------------------------------
// Section: CoSE layout remaining contants
// -----------------------------------------------------------------------------
	/**
	 * Default distance between each level in radial layout
	 */
	public static final double DEFAULT_RADIAL_SEPARATION =
		FDLayoutConstants.DEFAULT_EDGE_LENGTH;

	/**
	 * Default separation of trees in a forest when tiled to a grid
	 */
	public static final int DEFAULT_COMPONENT_SEPERATION = 60;

    /**
     * Default size increase check period for two phase gradual size increase
     * method in CoSE.
     */
    public static final int DEFAULT_TWO_PHASE_SIZE_INCREASE_CHECK_PERIOD = 100;

    /**
     * Enumeration types for the phase one and phase two of two phase
     * gradual size increase method.
     *
     */
    public static enum Phase
    {
        FIRST,
        SECOND
    };
}