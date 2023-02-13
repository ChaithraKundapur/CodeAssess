/*
package com.example.demo.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import com.example.demo.User;
import com.example.demo.dto.RequestDto;
import org.springframework.web.bind.annotation.RequestBody;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController
public class PomController {

    @PostMapping("/pom")
    public User getDependencies(@RequestBody RequestDto requestDto) throws IOException {
        String folderPath = requestDto.getPath();
        String fileName = "pom.xml";
        List<String> dependencies = new ArrayList<>();

        byte[] fileBytes = Files.readAllBytes(Paths.get(requestDto.getPath(), fileName));
//        String pomXml = new String(fileBytes);
//            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//            DocumentBuilder builder = factory.newDocumentBuilder();

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;
        User user = new User();
        try {
            dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(pomXml);
            doc.getDocumentElement().normalize();
            System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
            user.setArtifactId(doc.getElementsByTagName("artifactId").item(0).getChildNodes().item(0).getNodeValue());
            user.setVersion(doc.getElementsByTagName("version").item(0).getChildNodes().item(0).getNodeValue());

            user.setDependencies(Collections.singletonList(doc.getElementsByTagName("dependencies").item(0).getTextContent()));


//                Document doc = builder.parse(pomXml);
            NodeList depNodes = doc.getElementsByTagName("dependency");
            for (int i = 0; i < depNodes.getLength(); i++) {
                Node node = depNodes.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element elem = (Element) node;
                    String groupId = elem.getElementsByTagName("groupId").item(0).getTextContent();
                    String artifactId = elem.getElementsByTagName("artifactId").item(0).getTextContent();
                    String version = elem.getElementsByTagName("version").item(0).getTextContent();
                    dependencies.add(groupId + ":" + artifactId + ":" + version);
                    System.out.println(groupId+"**********");
                    System.out.println(version+"**********");
                    System.out.println(artifactId+"**********");


                }

            }

//            return dependencies;
      */
/*  } catch (Exception e) {
            e.printStackTrace();
            dependencies.add("Error parsing POM file: " + e.getMessage());
            return dependencies;
        }*//*

//    }

            return user;
        }
        catch (Exception e) {
            e.printStackTrace();
            return user;
        }
    }
}



*/
