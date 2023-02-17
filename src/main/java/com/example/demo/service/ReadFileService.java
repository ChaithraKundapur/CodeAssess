package com.example.demo.service;

import com.example.demo.User;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.Properties;

@Service
@RequiredArgsConstructor
public class ReadFileService {

    public User parse(String path) throws GitAPIException, IOException, XmlPullParserException {
        User user = new User();
        System.out.println("path:---->" + path);

        // Clone the repository
        Git.cloneRepository()
                .setURI(path)
                .setDirectory(new File("repository"))
                .call();


        // Check if the repository is a Spring Boot application
        File pomFile = new File("repository/pom.xml");
        boolean isSpringBootApp = isSpringBootApp(pomFile);

        if (isSpringBootApp) {
            // Read the pom.xml file
            InputStream pomStream = new FileInputStream(pomFile);
            Model pomModel = new MavenXpp3Reader().read(pomStream);
            // Parse the pom.xml file
            // ...

            // Read the application.properties file
            InputStream appPropertiesStream = new FileInputStream(new File("repository/src/main/resources/application.properties"));
            Properties appProperties = new Properties();
            appProperties.load(appPropertiesStream);
            // Print the details
            System.out.println(appProperties.toString());
        } else {
           // System.out.println("The repository is not a Spring Boot application.");
            user.setApplication_Type("Spring-Boot Application");
        }


            // Load the pom.xml file
        pomFile = new File("repository/pom.xml");
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
            //System.out.println("Project name: " + model.getName());
            //System.out.println("Project version: " + model.getVersion());
            //System.out.println("Project artifactId: " + model.getArtifactId());
            //System.out.println("Project Dependencies: " + model.getDependencies());

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
            //System.out.println("spring.datasource.url: " + props.getProperty("spring.datasource.url"));
            //System.out.println("spring.datasource.username: " + props.getProperty("spring.datasource.username"));
            //System.out.println("spring.jpa.database-platform: " + props.getProperty("spring.jpa.database-platform"));

            user.setUrl(props.getProperty("spring.datasource.url"));
            user.setUsername(props.getProperty("spring.datasource.username"));
            user.setPlatform(props.getProperty("spring.jpa.database-platform"));


            // Clean up cloned repository
            FileUtils.deleteDirectory(new File("repository"));


            return user;

        }

    private boolean isSpringBootApp(File pomFile) throws IOException, XmlPullParserException {
        Model pomModel = new MavenXpp3Reader().read(new FileInputStream(pomFile));
        return pomModel.getPackaging().equals("jar") && pomModel.getDependencies().stream()
                .anyMatch(dep -> dep.getGroupId().equals("org.springframework.boot") && dep.getArtifactId().equals("spring-boot-starter"));
    }

    }



