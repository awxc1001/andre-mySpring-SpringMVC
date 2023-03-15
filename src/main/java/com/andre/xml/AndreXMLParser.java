package com.andre.xml;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.InputStream;

/**
 * @author Andre Wang
 * @version 1.0
 */
public class AndreXMLParser {

    public static String getComponentScanPackage(String xmlFile){

        SAXReader saxReader = new SAXReader();

        //因为是static，不能用this.getclass,只能用.class
        InputStream resourceAsStream = AndreXMLParser.class.getClassLoader().getResourceAsStream(xmlFile);

        try {
            Document document = saxReader.read(resourceAsStream);
            Element rootElement = document.getRootElement();// <beans> 根目录
            Element componentScanElement = rootElement.element("component-scan");
            String basePackage = componentScanElement.attributeValue("base-package");
            return basePackage;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "XML SCAN FAILED!";
    }
}
