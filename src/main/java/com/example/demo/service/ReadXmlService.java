package com.example.demo.service;

import com.example.demo.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

@Service
@RequiredArgsConstructor
public class ReadXmlService {
    private final ReadFolder readFolder;

    public User getXml(final String path) throws IOException {
      String folderData = path+"/pom.xml";


       // System.out.println(folderData);


    File xmlFile = new File(folderData);
    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder dBuilder;
        User user = new User();
        try {
        dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(xmlFile);
        doc.getDocumentElement().normalize();
        System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
            user.setArtifactId(doc.getElementsByTagName("artifactId").item(0).getChildNodes().item(0).getNodeValue());
            user.setVersion(doc.getElementsByTagName("version").item(0).getChildNodes().item(0).getNodeValue());
            user.setDependencies(Collections.singletonList(doc.getElementsByTagName("dependencies").item(0).getTextContent()));
    //user.setDependencies(Collections.singletonList(doc.getElementsByTagName("dependencies").item(0).getTextContent()));

        List<User> userList = new ArrayList< User >();


        for (User emp: userList) {
            System.out.println(emp.toString());
        }
    } catch (SAXException | ParserConfigurationException | IOException e1) {
        e1.printStackTrace();
    }
return user;
}



    public  User getUser(Node node) {
        // XMLReaderDOM domReader = new XMLReaderDOM();
        User user = new User();
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            Element element = (Element) node;

            user.setArtifactId(getTagValue("artifactId", element));


        }
        return user;
    }

    public  String getTagValue(String tag, Element element) {
        NodeList nodeList = element.getElementsByTagName(tag).item(0).getChildNodes();
        Node node = (Node) nodeList.item(0);
        return node.getNodeValue();
    }
}
