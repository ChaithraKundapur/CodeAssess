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

    public List<User> doCodeAssessment(final String path) {

        CodeResult codeResult = new CodeResult();
        List<User> userData = new ArrayList<User>();

        List<File> filesArrayList = new ArrayList<File>();
        List<File> directoryArrayList = new ArrayList<File>();
        ArrayList<String> excludeArrayList = new ArrayList<String>();
        excludeArrayList.add("target");
        excludeArrayList.add(".mvn");
        excludeArrayList.add(".settings");
        excludeArrayList.add(".git");


        File[] files = new File(path).listFiles();
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

        System.out.println("" + filesArrayList);
        System.out.println("" + directoryArrayList);

        for (File f : directoryArrayList) {
            System.out.println("check11---->"+f.getAbsolutePath());
            if(f.getName() != null && !f.getName().isEmpty()) {
                System.out.println("check4---->" + f.getAbsolutePath());
                userData = processDirectory(f.getAbsolutePath(), codeResult,userData);

            }
        }
        return userData;
    }

    public  List<User> processDirectory (String path, CodeResult codeResult,List<User> userData) {
        User a = new User();
        File root = new File(path);
        File[] list = root.listFiles();

        for (File f : list) {
            System.out.println("File :" + f);
            System.out.println("File Directory :" + f.isDirectory());

            if (f.isDirectory()) {
                processDirectory(f.getAbsolutePath(), codeResult,userData);
            }
            else {
                System.out.println("Dir1:" + f.getAbsoluteFile());

                if (f.getName().equalsIgnoreCase("application.properties")) {
                    userData.add(parseApplicationProperties(f.getAbsolutePath()));
                }
            }
        }
        System.out.println("user data 1:" + userData.size());

        return userData;
    }

    public User parseApplicationProperties(String absolutePath) {

        User user = new User();

        String data;
        try {
            InputStream input = new FileInputStream(absolutePath);
            Properties prop = new Properties();
            prop.load(input);

            user.setUrl(prop.getProperty("spring.datasource.url"));
            user.setUsername(prop.getProperty("spring.datasource.username"));
            user.setPlatform(prop.getProperty("spring.jpa.database-platform"));

        } catch (Exception e) {

        }
        return user;
    }
}
