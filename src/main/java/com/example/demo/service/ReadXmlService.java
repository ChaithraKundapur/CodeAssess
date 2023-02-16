package com.example.demo.service;

import com.example.demo.User;
import com.example.demo.model.CodeResult;
import lombok.RequiredArgsConstructor;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
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
          //  user.setDependencies(Collections.singletonList(doc.getElementsByTagName("dependencies").item(0).getTextContent()));

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

    public User parse(String path){
        User user = new User();


            // Clone the repository
//                Git.cloneRepository()
//                        .setURI(repositoryLink)
//                        .setDirectory(new File("repository"))
//                        .call();

            // Load the pom.xml file
            File pomFile = new File("repository/pom.xml");
            MavenXpp3Reader reader = new MavenXpp3Reader();
        Model model = null;
        try {
            model = reader.read(new FileReader(pomFile));
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (XmlPullParserException e) {
            throw new RuntimeException(e);
        }

        // Print the project name and version
            System.out.println("Project name: " + model.getName());
            System.out.println("Project version: " + model.getVersion());
            System.out.println("Project artifactId: " + model.getArtifactId());
            System.out.println("Project Dependencies: " + model.getDependencies());
            user.setName(model.getName());
            user.setArtifactId(model.getName());
            user.setVersion(model.getVersion());
            user.setDependencies(model.getDependencies());

            // Load the application.properties file
            File propsFile = new File("repository/src/main/resources/application.properties");
            Properties props = new Properties();
        try {
            props.load(new FileReader(propsFile));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // Print the properties
            System.out.println("spring.datasource.url: " + props.getProperty("spring.datasource.url"));
            System.out.println("spring.datasource.username: " + props.getProperty("spring.datasource.username"));
            System.out.println("spring.jpa.database-platform: " + props.getProperty("spring.jpa.database-platform"));
                user.setUrl(props.getProperty("spring.datasource.url"));
                user.setUsername(props.getProperty("spring.datasource.username"));
                user.setPlatform(props.getProperty("spring.jpa.database-platform"));
            return user;
    }
 }