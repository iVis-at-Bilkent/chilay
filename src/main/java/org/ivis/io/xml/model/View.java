//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.10.28 at 02:39:18 AM EET 
//


package org.ivis.io.xml.model;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{}node" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="edge" type="{}edgeComplexType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{}customData" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "node",
    "edge",
    "customData"
})
@XmlRootElement(name = "view")
public class View {

    protected List<NodeComplexType> node;
    protected List<EdgeComplexType> edge;
    protected CustomData customData;

    /**
     * Gets the value of the node property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the node property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getNode().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link NodeComplexType }
     * 
     * 
     */
    public List<NodeComplexType> getNode() {
        if (node == null) {
            node = new ArrayList<NodeComplexType>();
        }
        return this.node;
    }

    /**
     * Gets the value of the edge property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the edge property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getEdge().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link EdgeComplexType }
     * 
     * 
     */
    public List<EdgeComplexType> getEdge() {
        if (edge == null) {
            edge = new ArrayList<EdgeComplexType>();
        }
        return this.edge;
    }

    /**
     * Gets the value of the customData property.
     * 
     * @return
     *     possible object is
     *     {@link CustomData }
     *     
     */
    public CustomData getCustomData() {
        return customData;
    }

    /**
     * Sets the value of the customData property.
     * 
     * @param value
     *     allowed object is
     *     {@link CustomData }
     *     
     */
    public void setCustomData(CustomData value) {
        this.customData = value;
    }

	public void addNode(NodeComplexType n)
	{
		node.add(n);	
	}

	public void addEdge(EdgeComplexType e)
	{
		edge.add(e);	
	}
}
