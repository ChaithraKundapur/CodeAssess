package com.example.demo.service;

import com.example.demo.User;
import com.example.demo.model.CodeResult;
import lombok.RequiredArgsConstructor;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

@Service
@RequiredArgsConstructor
public class ReadXmlService {
    private final ReadFolder readFolder;

    public User parse(String path) throws GitAPIException, IOException, XmlPullParserException {
        User user = new User();

        // Clone the repository
//        Git.cloneRepository()
//                .setURI(path)
//                .setDirectory(new File("/tmp1"))
//                .call();

        // Read the pom.xml file
//        String pomXmlPath = "/tmp1/pom.xml";
//        Path pomXml = Paths.get(pomXmlPath);
//        String pomXmlContent = new String(java.nio.file.Files.readAllBytes(pomXml));

        // Parse the pom.xml file
//        org.apache.maven.model.Model model = new org.apache.maven.model.io.xpp3.MavenXpp3Reader().read(new java.io.StringReader(pomXmlContent));
//        String groupId = model.getGroupId();
//        String artifactId = model.getArtifactId();
//        String version = model.getVersion();

        // Read the application.properties file
//        String applicationPropertiesPath = "/tmp1/src/main/resources/application.properties";
//        Path applicationProperties = Paths.get(applicationPropertiesPath);
//        String applicationPropertiesContent = new String(java.nio.file.Files.readAllBytes(applicationProperties));

        // Parse the application.properties file
//        Properties properties = new Properties();
//        properties.load(new java.io.ByteArrayInputStream(applicationPropertiesContent.getBytes()));
//        String propertyValue = properties.getProperty("example.property");

            // Clone the repository
                Git.cloneRepository()
                        .setURI(path)
                        .setDirectory(new File("repository"))
                        .call();

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
//
//        // Print the project name and version
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
//        // Print the properties
            System.out.println("spring.datasource.url: " + props.getProperty("spring.datasource.url"));
            System.out.println("spring.datasource.username: " + props.getProperty("spring.datasource.username"));
            System.out.println("spring.jpa.database-platform: " + props.getProperty("spring.jpa.database-platform"));
                user.setUrl(props.getProperty("spring.datasource.url"));
                user.setUsername(props.getProperty("spring.datasource.username"));
                user.setPlatform(props.getProperty("spring.jpa.database-platform"));
            return user;


    }









 }