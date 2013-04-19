
package org.ivis.io.xml.model;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the mypackage package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _NodeComplexTypeBounds_QNAME = new QName("", "bounds");
    private final static QName _NodeComplexTypeClusterIDs_QNAME = new QName("", "clusterIDs");
    private final static QName _NodeComplexTypeChildren_QNAME = new QName("", "children");
    private final static QName _NodeComplexTypeType_QNAME = new QName("", "type");
    private final static QName _Node_QNAME = new QName("", "node");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: mypackage
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link EdgeComplexType.SourceNode }
     * 
     */
    public EdgeComplexType.SourceNode createEdgeComplexTypeSourceNode() {
        return new EdgeComplexType.SourceNode();
    }

    /**
     * Create an instance of {@link EdgeComplexType.TargetNode }
     * 
     */
    public EdgeComplexType.TargetNode createEdgeComplexTypeTargetNode() {
        return new EdgeComplexType.TargetNode();
    }

    /**
     * Create an instance of {@link View }
     * 
     */
    public View createView() {
        return new View();
    }

    /**
     * Create an instance of {@link NodeComplexType.Children }
     * 
     */
    public NodeComplexType.Children createNodeComplexTypeChildren() {
        return new NodeComplexType.Children();
    }

    /**
     * Create an instance of {@link EdgeComplexType.BendPointList.BendPoint }
     * 
     */
    public EdgeComplexType.BendPointList.BendPoint createEdgeComplexTypeBendPointListBendPoint() {
        return new EdgeComplexType.BendPointList.BendPoint();
    }

    /**
     * Create an instance of {@link CustomData }
     * 
     */
    public CustomData createCustomData() {
        return new CustomData();
    }

    /**
     * Create an instance of {@link NodeComplexType }
     * 
     */
    public NodeComplexType createNodeComplexType() {
        return new NodeComplexType();
    }

    /**
     * Create an instance of {@link NodeComplexType.Type }
     * 
     */
    public NodeComplexType.Type createNodeComplexTypeType() {
        return new NodeComplexType.Type();
    }

    /**
     * Create an instance of {@link NodeComplexType.Bounds }
     * 
     */
    public NodeComplexType.Bounds createNodeComplexTypeBounds() {
        return new NodeComplexType.Bounds();
    }

    /**
     * Create an instance of {@link EdgeComplexType.BendPointList }
     * 
     */
    public EdgeComplexType.BendPointList createEdgeComplexTypeBendPointList() {
        return new EdgeComplexType.BendPointList();
    }

    /**
     * Create an instance of {@link NodeComplexType.ClusterIDs }
     * 
     */
    public NodeComplexType.ClusterIDs createNodeComplexTypeClusterIDs() {
        return new NodeComplexType.ClusterIDs();
    }

    /**
     * Create an instance of {@link EdgeComplexType }
     * 
     */
    public EdgeComplexType createEdgeComplexType() {
        return new EdgeComplexType();
    }

    /**
     * Create an instance of {@link GraphObjectComplexType }
     * 
     */
    public GraphObjectComplexType createGraphObjectComplexType() {
        return new GraphObjectComplexType();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link NodeComplexType.Bounds }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "bounds", scope = NodeComplexType.class)
    public JAXBElement<NodeComplexType.Bounds> createNodeComplexTypeBounds(NodeComplexType.Bounds value) {
        return new JAXBElement<NodeComplexType.Bounds>(_NodeComplexTypeBounds_QNAME, NodeComplexType.Bounds.class, NodeComplexType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link NodeComplexType.ClusterIDs }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "clusterIDs", scope = NodeComplexType.class)
    public JAXBElement<NodeComplexType.ClusterIDs> createNodeComplexTypeClusterIDs(NodeComplexType.ClusterIDs value) {
        return new JAXBElement<NodeComplexType.ClusterIDs>(_NodeComplexTypeClusterIDs_QNAME, NodeComplexType.ClusterIDs.class, NodeComplexType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link NodeComplexType.Children }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "children", scope = NodeComplexType.class)
    public JAXBElement<NodeComplexType.Children> createNodeComplexTypeChildren(NodeComplexType.Children value) {
        return new JAXBElement<NodeComplexType.Children>(_NodeComplexTypeChildren_QNAME, NodeComplexType.Children.class, NodeComplexType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link NodeComplexType.Type }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "type", scope = NodeComplexType.class)
    public JAXBElement<NodeComplexType.Type> createNodeComplexTypeType(NodeComplexType.Type value) {
        return new JAXBElement<NodeComplexType.Type>(_NodeComplexTypeType_QNAME, NodeComplexType.Type.class, NodeComplexType.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link NodeComplexType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "node")
    public JAXBElement<NodeComplexType> createNode(NodeComplexType value) {
        return new JAXBElement<NodeComplexType>(_Node_QNAME, NodeComplexType.class, null, value);
    }

}
