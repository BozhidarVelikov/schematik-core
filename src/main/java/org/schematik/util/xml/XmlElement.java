package org.schematik.util.xml;

import org.w3c.dom.*;

import java.util.ArrayList;
import java.util.List;

public class XmlElement {
    private Element rootElement;

    public XmlElement(Document document) {
        this.rootElement = document.getDocumentElement();
    }

    public XmlElement(Element rootElement) {
        this.rootElement = rootElement;
    }

    public boolean hasProperty(String propertyName) {
        return rootElement.hasAttribute(propertyName);
    }

    public String getProperty(String propertyName) {
        if (hasProperty(propertyName)) {
            return rootElement.getAttribute(propertyName);
        }

        return null;
    }

    public XmlElement getElement(String elementName) {
        NodeList nodes = rootElement.getElementsByTagName(elementName);

        if (nodes.getLength() == 0) {
            return null;
        }

        return new XmlElement((Element)nodes.item(0));
    }

    public List<XmlElement> getElements(String elementName) {
        List<XmlElement> elements = new ArrayList<>();

        NodeList nodes = rootElement.getElementsByTagName(elementName);
        for (int i = 0; i < nodes.getLength(); i++) {
            elements.add(new XmlElement((Element)nodes.item(i)));
        }

        return elements;
    }

    public boolean hasChildren() {
        return rootElement.hasChildNodes();
    }

    public String getValue() {
        return rootElement.getTextContent();
    }

    public String toString() {
        String xmlElement = "<" + rootElement.getTagName();
        NamedNodeMap nodeMap = rootElement.getAttributes();
        for (int i = 0; i < nodeMap.getLength(); i++) {
            xmlElement += String.format(" %s=\"%s\"", nodeMap.item(i).getNodeName(), nodeMap.item(i).getNodeValue());
        }
        xmlElement += "/>";

        return xmlElement;
    }
}
