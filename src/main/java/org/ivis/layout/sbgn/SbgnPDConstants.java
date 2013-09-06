package org.ivis.layout.sbgn;

import org.ivis.layout.cose.CoSEConstants;

/**
 * This class maintains the constants used by CoSE layout.
 * 
 * @author: Begum Genc, Istemi Bahceci
 * 
 *          Copyright: i-Vis Research Group, Bilkent University, 2007 - present
 */
public class SbgnPDConstants extends CoSEConstants
{
	// Below are the SBGN glyph specific types.
	public static final String MACROMOLECULE = "macromolecule";
	public static final String UNIT_OF_INFORMATION = "unit of information";
	public static final String STATE_VARIABLE = "state variable";
	public static final String SOURCE_AND_SINK = "source and sink";
	public static final String ASSOCIATION = "association";
	public static final String DISSOCIATION = "dissociation";
	public static final String OMITTED_PROCESS = "omitted process";
	public static final String UNCERTAIN_PROCESS = "uncertain process";
	public static final String SIMPLE_CHEMICAL = "simple chemical";
	public static final String PROCESS = "process";
	public static final String COMPLEX = "complex";
	public static final String AND = "and";
	public static final String OR = "or";
	public static final String NOT = "not";
	public static final String PHENOTYPE = "phenotype";
	public static final String PERTURBING_AGENT = "perturbing agent";
	public static final String TAG = "tag";
	public static final String NUCLEIC_ACID_FEATURE = "nucleic acid feature";
	public static final String UNSPECIFIED_ENTITY = "unspecified entity";

	// Below are the SBGN Arc specific types.
	public static final String PRODUCTION = "production";
	public static final String CONSUMPTION = "consumption";
	public static final String INHIBITION = "inhibition";
	public static final String CATALYSIS = "catalysis";
	public static final String MODULATION = "modulation";
	public static final String STIMULATION = "stimulation";
	public static final String NECESSARY_STIMULATION = "necessary stimulation";

	public static final int COMPLEX_MEM_HORIZONTAL_BUFFER = 5;
	public static final int COMPLEX_MEM_VERTICAL_BUFFER = 5;
	public static final int COMPLEX_MEM_MARGIN = 5;
	public static final double COMPLEX_MIN_WIDTH = COMPLEX_MEM_MARGIN * 2;
	
	/**
	 * Relativity constraint factor
	 */
	public static double RELATIVITY_CONSTRAINT_CONSTANT = 0.2;
	public static double RELATIVITY_DEVATION_DISTANCE = 10;
}
