package org.schematik.util.xml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.StringReader;

public class XMLParser {

    static Logger logger = LoggerFactory.getLogger(XMLParser.class);
    static DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

    public static XmlElement parse(File file) throws Exception {
        documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.parse(file);
        document.getDocumentElement().normalize();

        return new XmlElement(document);
    }

    public static XmlElement parse(String xml) throws Exception {
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        InputSource inputSource = new InputSource(new StringReader(xml));

        return new XmlElement(documentBuilder.parse(inputSource));
    }
}
