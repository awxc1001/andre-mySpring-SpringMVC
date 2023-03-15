package com.andre.test;

import com.andre.andrespringmvc.context.AndreWebApplicationContext;
import com.andre.xml.AndreXMLParser;
import org.junit.Test;

/**
 * @author Andre Wang
 * @version 1.0
 */
public class AndreSpringMVCTest {

    //test for dom4j XML parsing scan
    @Test
    public void readXML(){
        String componentScanPackage = AndreXMLParser.getComponentScanPackage("andrespringmvc.xml");
        System.out.println("componentScanPackage is: "+ componentScanPackage);
    }

    @Test
    public void testApplicationContext(){
        AndreWebApplicationContext andreWebApplicationContext = new AndreWebApplicationContext();
        andreWebApplicationContext.scanBasePackage("asasd");
    }

}
