package com.example.demo.service;

import com.example.demo.User;
import com.example.demo.model.CodeResult;
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

        List<User> userList = new ArrayList< User >();


        for (User emp: userList) {
            System.out.println(emp.toString());
        }
    } catch (SAXException | ParserConfigurationException | IOException e1) {
        e1.printStackTrace();
    }
return user;
}


    public User doCodeAssessment(final String path) {

        CodeResult codeResult = new CodeResult();

        List<File> filesArrayList = new ArrayList<File>();
        List<File> directoryArrayList = new ArrayList<File>();
        ArrayList<String> excludeArrayList = new ArrayList<String>();
        excludeArrayList.add("target");
        excludeArrayList.add(".mvn");
        excludeArrayList.add(".settings");
        excludeArrayList.add(".git");


        File[] files = new File(path).listFiles();

        User userData = new User();
        for (File file : files) {

            if (file.isFile()) {

                String fileName = file.getName();

                if ("pom.xml".equalsIgnoreCase(fileName)) {

                    codeResult.setApplicationType("SPRINGBOOT");

                } else if ("package.json".equalsIgnoreCase(fileName)) {
                    codeResult.setApplicationType("NODEJS");
                }

                filesArrayList.add(file);
            } else if (file.isDirectory()) {

                boolean isExclude = excludeArrayList.contains(file.getName());
                if (!isExclude) {
                    directoryArrayList.add(file);
                }

            }
        }

//        System.out.println("" + filesArrayList);
//        System.out.println("" + directoryArrayList);

        for (File f : directoryArrayList) {

            if(f.getName() != null && !f.getName().isEmpty()) {

                userData = processDirectory(f.getAbsolutePath(), codeResult);


//                System.out.println("check1000-->" + userData.getUrl());
                break;
            }
        }


        return userData;
    }

    public  User processDirectory (String path, CodeResult codeResult) {
        User a = new User();
        File root = new File(path);
        File[] list = root.listFiles();

//        if (list == null)
//            return;


        for (File f : list) {
            if (f.isDirectory()) {
                processDirectory(f.getAbsolutePath(), codeResult);
                //System.out.println("Dir:" + f.getAbsoluteFile());
            } else {

                if (f.getName().equalsIgnoreCase("application.properties")) {
                     a = parseApplicationProperties(f.getAbsolutePath());

//                    System.out.println("check1-->"+a.getUrl());


                }



                //System.out.println("File:" + f.getAbsoluteFile());
            }
        }
        return a;
    }

    public User parseApplicationProperties(String absolutePath) {

        User user = new User();

        String data;
        try {
            InputStream input = new FileInputStream(absolutePath);
            Properties prop = new Properties();
            prop.load(input);

//            System.out.println(absolutePath+"-----------------");

//            System.out.println(prop.getProperty("spring.datasource.url"));
//            System.out.println(prop.getProperty("spring.datasource.username"));
//            System.out.println(prop.getProperty("spring.datasource.driver-class-name"));
//            System.out.println(prop.getProperty("spring.jpa.database-platform"));
            user.setUrl(prop.getProperty("spring.datasource.url"));
            user.setUsername(prop.getProperty("spring.datasource.username"));
            user.setPlatform(prop.getProperty("spring.jpa.database-platform"));

//            System.out.println(user.getUrl()+"-------------123");
        } catch (Exception e) {

        }
//        System.out.println(user.getUrl()+"-------------123");
        return user;
    }
}
