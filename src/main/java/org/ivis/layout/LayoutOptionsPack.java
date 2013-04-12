package org.ivis.layout;

import java.io.Serializable;

import org.ivis.layout.avsdf.AVSDFConstants;
import org.ivis.layout.cise.CiSEConstants;
import org.ivis.layout.cose.CoSEConstants;
import org.ivis.layout.fd.FDLayoutConstants;
import org.ivis.layout.sgym.SgymConstants;
import org.ivis.layout.spring.SpringConstants;

/**
 * This method gathers the user-customizable layout options in a package
 *
 * @author Cihan Kucukkececi
 * @author Ugur Dogrusoz
 *
 * Copyright: i-Vis Research Group, Bilkent University, 2007 - present
 */
public class LayoutOptionsPack implements Serializable
{
	private static LayoutOptionsPack instance;

	private General general;
	private CoSE coSE;
	private Cluster cluster;
	private CiSE ciSE;
	private AVSDF avsdf;
	private Spring spring;
	private Sgym sgym;

	public class General
	{
		public int layoutQuality; // proof, default, draft
		public boolean animationDuringLayout; // T-F
		public boolean animationOnLayout; // T-F
		public int animationPeriod; // 0-100
		public boolean incremental; // T-F
		public boolean createBendsAsNeeded; // T-F
		public boolean uniformLeafNodeSizes; // T-F

		public int defaultLayoutQuality = LayoutConstants.DEFAULT_QUALITY;
		public boolean defaultAnimationDuringLayout = LayoutConstants.DEFAULT_ANIMATION_DURING_LAYOUT;
		public boolean defaultAnimationOnLayout = LayoutConstants.DEFAULT_ANIMATION_ON_LAYOUT;
		public int defaultAnimationPeriod = 50;
		public boolean defaultIncremental = LayoutConstants.DEFAULT_INCREMENTAL;
		public boolean defaultCreateBendsAsNeeded = LayoutConstants.DEFAULT_CREATE_BENDS_AS_NEEDED;
		public boolean defaultUniformLeafNodeSizes = LayoutConstants.DEFAULT_UNIFORM_LEAF_NODE_SIZES;
	}

	public class CoSE
	{
		public int idealEdgeLength; // any positive int
		public int springStrength; // 0-100
		public int repulsionStrength; // 0-100
		public boolean smartRepulsionRangeCalc; // T-F
		public int gravityStrength; // 0-100
		public int gravityRange; // 0-100
		public int compoundGravityStrength; // 0-100
		public int compoundGravityRange; // 0-100
		public boolean smartEdgeLengthCalc; // T-F
		public boolean multiLevelScaling; // T-F

		public int defaultIdealEdgeLength = CoSEConstants.DEFAULT_EDGE_LENGTH;
		public int defaultSpringStrength = 50;
		public int defaultRepulsionStrength = 50;
		public boolean defaultSmartRepulsionRangeCalc = CoSEConstants.DEFAULT_USE_SMART_REPULSION_RANGE_CALCULATION;
		public int defaultGravityStrength = 50;
		public int defaultGravityRange = 50;
		public int defaultCompoundGravityStrength = 50;
		public int defaultCompoundGravityRange = 50;
		public boolean defaultSmartEdgeLengthCalc = CoSEConstants.DEFAULT_USE_SMART_IDEAL_EDGE_LENGTH_CALCULATION;
		public boolean defaultMultiLevelScaling = CoSEConstants.DEFAULT_USE_MULTI_LEVEL_SCALING;
	}

	public class Cluster
	{
		public int idealEdgeLength; // any positive int
		public int clusterSeperation; // 0-100
		public int clusterGravityStrength; // 0-100

		public int defaultIdealEdgeLength = CoSEConstants.DEFAULT_EDGE_LENGTH;
		public int defaultClusterSeperation = 50;
		public int defaultClusterGravityStrength = 50;
	}

	public class CiSE
	{
		public int nodeSeparation; // any positive int
		public int desiredEdgeLength; // any positive int
		public int interClusterEdgeLengthFactor; // 0-100
		public boolean allowNodesInsideCircle; // T-F
		public double maxRatioOfNodesInsideCircle; // 0.0-1.0

		public int defaultNodeSeparation = CiSEConstants.DEFAULT_NODE_SEPARATION;
		public int defaultDesiredEdgeLength = CiSEConstants.DEFAULT_EDGE_LENGTH;
		public int defaultInterClusterEdgeLengthFactor = 50;
		public boolean defaultAllowNodesInsideCircle = CiSEConstants.DEFAULT_ALLOW_NODES_INSIDE_CIRCLE;
		public double defaultMaxRatioOfNodesInsideCircle = CiSEConstants.DEFAULT_MAX_RATIO_OF_NODES_INSIDE_CIRCLE;
	}

	public class AVSDF
	{
		public int nodeSeparation; // any positive int

		public int defaultNodeSeparation = AVSDFConstants.DEFAULT_NODE_SEPARATION;
	}

	public class Spring
	{
		public int nodeDistanceRestLength; // any positive int
		public int disconnectedNodeDistanceSpringRestLength; // any positive int

		public int defaultNodeDistanceRestLength = (int)SpringConstants.DEFAULT_NODE_DISTANCE_REST_LENGTH_CONSTANT;
		public int defaultDisconnectedNodeDistanceSpringRestLength = (int)SpringConstants.DEFAULT_DISCONNECTED_NODE_DISTANCE_SPRING_REST_LENGTH;
	}

	public class Sgym
	{
		public int horizontalSpacing; // any positive int
		public int verticalSpacing; // any positive int
		public boolean vertical; // T-F

		public int defaultHorizontalSpacing = SgymConstants.DEFAULT_HORIZONTAL_SPACING;
		public int defaultVerticalSpacing = SgymConstants.DEFAULT_VERTICAL_SPACING;
		public boolean defaultVertical = SgymConstants.DEFAULT_VERTICAL;
	}

	private LayoutOptionsPack()
	{
		this.general = new General();
		this.coSE = new CoSE();
		this.cluster = new Cluster();
		this.ciSE = new CiSE();
		this.avsdf = new AVSDF();
		this.spring = new Spring();
		this.sgym = new Sgym();

		setDefaultLayoutProperties();
	}

	public void setDefaultLayoutProperties()
	{
		this.general.layoutQuality = this.general.defaultLayoutQuality ;
		this.general.animationDuringLayout = this.general.defaultAnimationDuringLayout ;
		this.general.animationOnLayout = this.general.defaultAnimationOnLayout ;
		this.general.animationPeriod = this.general.defaultAnimationPeriod;
		this.general.incremental = this.general.defaultIncremental ;
		this.general.createBendsAsNeeded = this.general.defaultCreateBendsAsNeeded ;
		this.general.uniformLeafNodeSizes = this.general.defaultUniformLeafNodeSizes ;

		this.coSE.idealEdgeLength = this.coSE.defaultIdealEdgeLength;
		this.coSE.springStrength = this.coSE.defaultSpringStrength ;
		this.coSE.repulsionStrength = this.coSE.defaultRepulsionStrength ;
		this.coSE.smartRepulsionRangeCalc = this.coSE.defaultSmartRepulsionRangeCalc ;
		this.coSE.gravityStrength = this.coSE.defaultGravityStrength ;
		this.coSE.gravityRange = this.coSE.defaultGravityRange ;
		this.coSE.compoundGravityStrength = this.coSE.defaultCompoundGravityStrength ;
		this.coSE.compoundGravityRange = this.coSE.defaultCompoundGravityRange ;
		this.coSE.smartEdgeLengthCalc = this.coSE.defaultSmartEdgeLengthCalc ;
		this.coSE.multiLevelScaling = this.coSE.defaultMultiLevelScaling ;

		this.cluster.idealEdgeLength = this.cluster.defaultIdealEdgeLength;
		this.cluster.clusterSeperation = this.cluster.defaultClusterSeperation;
		this.cluster.clusterGravityStrength = this.cluster.defaultClusterGravityStrength;

		this.ciSE.nodeSeparation = this.ciSE.defaultNodeSeparation;
		this.ciSE.desiredEdgeLength = this.ciSE.defaultDesiredEdgeLength;
		this.ciSE.interClusterEdgeLengthFactor = this.ciSE.defaultInterClusterEdgeLengthFactor;
		this.ciSE.allowNodesInsideCircle = this.ciSE.defaultAllowNodesInsideCircle;
		this.ciSE.maxRatioOfNodesInsideCircle = this.ciSE.defaultMaxRatioOfNodesInsideCircle;

		this.avsdf.nodeSeparation = this.avsdf.defaultNodeSeparation;

		this.spring.nodeDistanceRestLength = this.spring.defaultNodeDistanceRestLength;
		this.spring.disconnectedNodeDistanceSpringRestLength = this.spring.defaultDisconnectedNodeDistanceSpringRestLength;

		this.sgym.horizontalSpacing = this.sgym.defaultHorizontalSpacing;
		this.sgym.verticalSpacing = this.sgym.defaultVerticalSpacing;
		this.sgym.vertical = this.sgym.defaultVertical;
	}

	public static LayoutOptionsPack getInstance()
	{
		if (instance == null)
		{
			instance = new LayoutOptionsPack();
		}

		return instance;
	}

	public Sgym getSgym()
	{
		return sgym;
	}

	public CoSE getCoSE()
	{
		return coSE;
	}

	public Spring getSpring()
	{
		return spring;
	}

	public Cluster getCluster()
	{
		return cluster;
	}

	public CiSE getCiSE()
	{
		return ciSE;
	}

	public AVSDF getAVSDF()
	{
		return avsdf;
	}

	public General getGeneral()
	{
		return general;
	}
}