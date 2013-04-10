package org.ivis.io.xml;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;

import javax.naming.OperationNotSupportedException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.ivis.io.xml.model.*;
import org.ivis.io.xml.model.EdgeComplexType.BendPointList;
import org.ivis.io.xml.model.EdgeComplexType.BendPointList.BendPoint;
import org.ivis.io.xml.model.NodeComplexType.Bounds;
import org.ivis.layout.*;
import org.ivis.layout.cise.CiSELayout;
import org.ivis.layout.cose.CoSELayout;
import org.ivis.util.PointD;
import org.ivis.util.RectangleD;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;

public class XmlIOHandler 
{
	/*
	 * Layout object needed to run layout
	 */
	private Layout layout;

	/*
	 * Graph manager of the layout object
	 */
	private LGraphManager gm;

	/*
	 * Root graph of the graph manager associated with the layout object
	 */
	private LGraph rootGraph;

	/*
	 * Mapping between xml objects and L-level objects.
	 */
	private HashMap<GraphObjectComplexType, LGraphObject> xmlObjectToLayoutObject;
	private HashMap<String, GraphObjectComplexType> xmlIDToXMLObject;

	private JAXBContext jaxbContext;
	
	private ObjectFactory objectFactory;
	
	private View loadedModel;

	/*
	 * Constructor
	 */
	public XmlIOHandler(Layout layout) throws JAXBException
	{
		this.xmlObjectToLayoutObject =
			new HashMap<GraphObjectComplexType, LGraphObject>();
		this.xmlIDToXMLObject = new HashMap<String, GraphObjectComplexType>();
		
		this.jaxbContext = JAXBContext.newInstance("org.ivis.io.xml.model");
		this.objectFactory = new ObjectFactory();

		this.layout = layout;

		// create initial topology: a graph manager associated with this layout,
		// containing an empty root graph as its only graph

		this.gm = this.layout.getGraphManager();
		this.rootGraph = this.gm.addRoot();
	}
	
	/**
	 * This method reads the xml file from the given input stream and populates 
	 * given l-level model.
	 * @param inputStream
	 * @throws JAXBException
	 * @throws OperationNotSupportedException
	 */
	public void fromXML(InputStream inputStream) throws JAXBException,
		OperationNotSupportedException
	{
		Unmarshaller unmarshaller = this.jaxbContext.createUnmarshaller();

		this.loadedModel = (View) unmarshaller.unmarshal(inputStream);
		
		for (NodeComplexType nodeType : this.loadedModel.getNode())
		{
			parseNode(nodeType, null);
		}

		for (EdgeComplexType edgeType : this.loadedModel.getEdge())
		{
			parseEdge(edgeType);
		}
	}
	
	/**
	 * This method synchronizes loaded xml model with the l-level graph 
	 * structure and writes resulting xml model to the given output stream.
	 * @param outputStream
	 * @throws JAXBException
	 * @throws OperationNotSupportedException
	 */
	public void toXML(OutputStream outputStream) throws JAXBException,
		OperationNotSupportedException
	{
		if (this.loadedModel == null)
		{
			throw new OperationNotSupportedException("There is no previously" +
				"loaded xml file. Please first laod an xml file first.");
		}
		
		Marshaller marshaller = this.jaxbContext.createMarshaller();

		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,true);

		// Set prefix mapper for proper name space prefixes in the created
		// document.
		marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper",
			new EmptyNameSpacePrefixMapper());

		for (GraphObjectComplexType xmlObject :
				this.xmlObjectToLayoutObject.keySet())
		{
			LGraphObject lGraphObject = 
				this.xmlObjectToLayoutObject.get(xmlObject);

			if (lGraphObject instanceof LNode)
			{
				this.writeNodeBack(
					(LNode) lGraphObject, (NodeComplexType) xmlObject);
			}
			else if (lGraphObject instanceof LEdge)
			{
				this.writeEdgeBack(
					(LEdge) lGraphObject, (EdgeComplexType) xmlObject);
			}
		}

		marshaller.marshal(this.loadedModel, outputStream);
	}

	/**
	 * This method processes each node from XML file. It basically creates an
	 * associated l-level node and copies its geometry information. If the node
	 * has children, it recursively parses those children.
	 */
	private void parseNode(NodeComplexType xmlNode,
		NodeComplexType parentXmlNode)
		throws OperationNotSupportedException
	{
		LNode lNode = this.layout.newNode(null);
		this.xmlObjectToLayoutObject.put(xmlNode, lNode);
		this.setIdIndex(xmlNode);

		// Locate newly created node.
		if (parentXmlNode != null)
		{
			LNode parentLNode = 
				(LNode) this.xmlObjectToLayoutObject.get(parentXmlNode);
			assert parentLNode.getChild() != null : 
				"Parent node doesn't have child graph.";
			parentLNode.getChild().add(lNode);
		}
		else
		{
			this.rootGraph.add(lNode);
		}

		// Copy geometry
		Bounds bounds = xmlNode.getBounds();
		lNode.setLocation(bounds.getX(), bounds.getY());
		lNode.setWidth(bounds.getWidth());
		lNode.setHeight(bounds.getHeight());

		// Copy cluster IDs
		if (xmlNode.getClusterIDs() != null)
		{
			for (String id : xmlNode.getClusterIDs().getClusterID())
			{
				lNode.addCluster(Integer.parseInt(id));
			}
		}

		// If it has children, go and recursively parse them.
		if (xmlNode.getChildren() != null)
		{
			LGraph childGraph = this.gm.add(this.layout.newGraph(null), lNode);

			for (NodeComplexType childNode : xmlNode.getChildren().getNode())
			{
				parseNode(childNode, xmlNode);
			}
		}
	}

	/**
	 * This method process each edge from XML file. It mainly creates an
	 * associated l-level edge with the proper source and target.
	 */
	private void parseEdge(EdgeComplexType xmlEdge) 
		throws OperationNotSupportedException
	{
		LEdge lEdge = this.layout.newEdge(null); 
		
		// Find source and target nodes
		String sourceXmlNodeId = xmlEdge.getSourceNode().getId();
		String targetXmlNodeId = xmlEdge.getTargetNode().getId();
		LNode sourceLNode = (LNode) this.xmlObjectToLayoutObject.get(
			this.xmlIDToXMLObject.get(sourceXmlNodeId));
		LNode targetLNode = (LNode) this.xmlObjectToLayoutObject.get(
			this.xmlIDToXMLObject.get(targetXmlNodeId));
		
		// Throw exception if referenced nodes does not exist.
		if (sourceLNode == null)
		{
			throw new OperationNotSupportedException("Source node with the " +
					"given ID <"+sourceXmlNodeId+"> does not exist for edge " +
					"with ID <"+xmlEdge.getId()+">");
		}
		if (targetLNode == null)
		{
			throw new OperationNotSupportedException("Source node with the " +
					"given ID <"+targetXmlNodeId+"> does not exist for edge " +
					"with ID <"+xmlEdge.getId()+">");
		}

		this.gm.add(lEdge, sourceLNode, targetLNode);
		this.xmlObjectToLayoutObject.put(xmlEdge, lEdge);
		
		// TODO: Edges are not currently forced to have unique id s. However
		// we might force them to have so in the future
//		this.setIdIndex(xmlEdge);
		
		// Copy bend points, if any.
		if (xmlEdge.getBendPointList() != null)
		{
			for (BendPoint xmlBendPoint : 
				xmlEdge.getBendPointList().getBendPoint()) 
			{
				PointD bendPoint = 
					new PointD(xmlBendPoint.getX(), xmlBendPoint.getY()); 
				lEdge.getBendpoints().add(bendPoint);
			}
		}
	}
	
	/**
	 * This method writes the geometry information stored in LNode back to 
	 * xml node.
	 * @param lNode
	 * @param xmlNode
	 */
	private void writeNodeBack(LNode lNode, NodeComplexType xmlNode)
	{
		RectangleD nodeBounds = lNode.getRect();

		xmlNode.getBounds().setHeight((int) nodeBounds.height);
		xmlNode.getBounds().setWidth((int) nodeBounds.width);
		xmlNode.getBounds().setX((int) nodeBounds.x);
		xmlNode.getBounds().setY((int) nodeBounds.y);
	}
	
	/**
	 * This method writes the geometry information stored in LEdge back to 
	 * xml edge.
	 * @param lEdge
	 * @param xmlEdge
	 */
	private void writeEdgeBack(LEdge lEdge, EdgeComplexType xmlEdge) 
	{
		List<PointD> bendPoints = lEdge.getBendpoints();
		
		BendPointList xmlBendPoints = xmlEdge.getBendPointList();
		
		if (xmlBendPoints == null)
		{
			xmlEdge.setBendPointList(
				objectFactory.createEdgeComplexTypeBendPointList());
		}
		else
		{
			xmlBendPoints.getBendPoint().clear();
		}
		
		for (PointD pointD : bendPoints) 
		{
			BendPoint xmlBendPoint = objectFactory.
				createEdgeComplexTypeBendPointListBendPoint();
			
			xmlBendPoint.setX(pointD.x);
			xmlBendPoint.setY(pointD.y);
			
			xmlBendPoints.getBendPoint().add(xmlBendPoint);
		}
	}
	
	/**
	 * This method build the index to xml object mapping by ensuring that the 
	 * given xml object has unique id.
	 * @param xmlGraphObject
	 * @throws OperationNotSupportedException
	 */
	private void setIdIndex(GraphObjectComplexType xmlGraphObject) throws 
		OperationNotSupportedException
	{
		String id = xmlGraphObject.getId();
		
		// Check id, if it is used before throw an exception, otherwise index it
		if (this.xmlIDToXMLObject.get(id) != null)
		{
			throw new OperationNotSupportedException("The ID:"+id+ " is used" +
				" more than one time");
		}
		else
		{
			this.xmlIDToXMLObject.put(id, xmlGraphObject);
		}
	}
	
	/**
	 * This class provides a custom namespace mapper for empty prefixes in the
	 * created xml document
	 *
	 * @author Esat
	 */
	private class EmptyNameSpacePrefixMapper extends NamespacePrefixMapper
	{
		public String getPreferredPrefix(String namespaceUri,
			String suggestion,
			boolean requirePrefix)
		{
			if (namespaceUri.equals(""))
			{
				return null;
			}
			else
			{
				return "xsi";
			}
		}
	}
	
	public static void main(String[] args) throws Exception
	{
		Layout layout = new CiSELayout();
		XmlIOHandler handler = new XmlIOHandler(layout);

		handler.fromXML(new FileInputStream("src/org/ivis/io/xml/layout.xml"));

		layout.runLayout();

		handler.toXML(new FileOutputStream("src/org/ivis/io/xml/layout_done.xml"));

//		XmlIOHandler.generateClasses();
	}
	
	/**
	 * This method calls jaxb compiler for generating class files using the
	 * xml schemas. This method needs to be called only when the schema is changed
	 * @throws Exception
	 */
	private static void generateClasses() throws Exception
	{
		String s = null;
	
		String execName = "C:\\Program Files\\Java\\jdk1.6.0_24\\bin\\xjc.exe";
	
		System.out.println(execName);
		Process p = Runtime.getRuntime().exec(execName +
			" -p org.ivis.io.xml.model "+
			"src/org/ivis/io/xml/layout.xsd -d src");
	
		BufferedReader stdInput =
			new BufferedReader(new InputStreamReader(p.getInputStream()));
	
		// read the normal output
		while ((s = stdInput.readLine()) != null)
		{
			System.out.println(s);
		}
	
		BufferedReader errInput =
			new BufferedReader(new InputStreamReader(p.getErrorStream()));
	
		// read the error output
		while ((s = errInput.readLine()) != null)
		{
			System.out.println(s);
		}
	}
}