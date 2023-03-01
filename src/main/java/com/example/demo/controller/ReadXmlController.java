package com.example.demo.controller;

import com.example.demo.User;
import com.example.demo.dto.RequestDto;
import com.example.demo.dto.ResponseDto;
import com.example.demo.service.ReadFileService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@CrossOrigin(origins = "*")
@RestController
@RequiredArgsConstructor
public class ReadXmlController {
    private final ReadFileService readFileService;

    @PostMapping("/parse-file")
    public ResponseEntity<?> getPom(@RequestBody RequestDto requestDto) throws IOException, GitAPIException, XmlPullParserException {

        User data = readFileService.parse(requestDto.getPath());


        ResponseDto responseDto = new ResponseDto();

        responseDto.setData(data);

        return ResponseEntity.ok(responseDto);


    }

    @PostMapping("/github-public")
    public ResponseEntity<?> cloneGitHubPublicRepo(@RequestBody Map<String, String> request) {
        String gitHubUrl = request.get("gitHubUrl");

        try {
            // Clone the GitHub repository to a temporary directory
            Path tempDirectory = Files.createTempDirectory("temp-dir");
            Git.cloneRepository()
                    .setURI(gitHubUrl)
                    .setDirectory(tempDirectory.toFile())
                    .call();

            // Parse the appropriate file depending on the project type
            String fileType = determineProjectType(tempDirectory);
            if (fileType.equals("Spring Boot")) {
                File pomXmlFile = new File(tempDirectory.toString() + "/pom.xml");
                File applicationPropertiesFile = new File(tempDirectory.toString() + "/src/main/resources/application.properties");
                String springBootVersion = getVersionFromPomXmlFile(pomXmlFile);
                Map<String, String> dependencies = getDependenciesFromPomXmlFile(pomXmlFile);
                Properties applicationProperties = getPropertiesFromFile(applicationPropertiesFile);
                // return the Spring Boot project info
                return ResponseEntity.ok().body(
                        Map.of("type", fileType, "version", springBootVersion, "properties", applicationProperties, "dependencies", dependencies));
            }


//            else if (fileType.equals("Node.js")) {
//                File packageJsonFile = new File(tempDirectory.toString() + "/package.json");
//                Map<String, Object> packageJsonContent = getPackageJsonContent(packageJsonFile);
//                // return the Node.js project info
//                System.out.println(packageJsonContent);
//                return ResponseEntity.ok().body(Map.of("type", fileType, "Details", packageJsonContent));
//            }
            else if (fileType.equals("Node.js")) {
                File packageJsonFile = new File(tempDirectory.toString() + "/package.json");
                Map<String, Object> packageJsonContent = getPackageJsonContent(packageJsonFile);
                // extract the Node.js project info
                Map<String, Object> nodeJsInfo = new HashMap<>();
                nodeJsInfo.put("name", packageJsonContent.get("name"));
                nodeJsInfo.put("version", packageJsonContent.get("version"));
                nodeJsInfo.put("description", packageJsonContent.get("description"));
                nodeJsInfo.put("author", packageJsonContent.get("author"));
                nodeJsInfo.put("license", packageJsonContent.get("license"));
                // extract dependencies
                Map<String, Object> dependencies = (Map<String, Object>) packageJsonContent.get("dependencies");
                // return the Node.js project info and dependencies
                Map<String, Object> response = new HashMap<>();
                response.put("type", fileType);
                response.put("name", nodeJsInfo.get("name"));
                response.put("version", nodeJsInfo.get("version"));
                response.put("description", nodeJsInfo.get("description"));
                response.put("author", nodeJsInfo.get("author"));
                response.put("license", nodeJsInfo.get("license"));
                response.put("dependencies", dependencies);
//                System.out.println(response);
                return ResponseEntity.ok().body(response);
            } else {
                // unsupported project type
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                        Map.of("error", "Unsupported project type."));
            }
        } catch (Exception e) {
            // error occurred during cloning or parsing
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of("error", "An error occurred: " + e.getMessage()));
        }
    }

    private String determineProjectType(Path projectDirectory) {
        // Check if the directory contains a pom.xml file
        File pomXmlFile = new File(projectDirectory.toString() + "/pom.xml");
        if (pomXmlFile.exists()) {
            return "Spring Boot";
        }

        // Check if the directory contains a package.json file
        File packageJsonFile = new File(projectDirectory.toString() + "/package.json");
        if (packageJsonFile.exists()) {
            return "Node.js";
        }

        // unsupported project type
        return "Unsupported";
    }

    private String getVersionFromPomXmlFile(File pomXmlFile) throws IOException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;
        try {
            dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(pomXmlFile);
            doc.getDocumentElement().normalize();
            NodeList parentNodes = doc.getElementsByTagName("parent");
            if (parentNodes.getLength() > 0) {
                Node parent = parentNodes.item(0);
                NodeList parentChildren = parent.getChildNodes();
                for (int i = 0; i < parentChildren.getLength(); i++) {
                    Node node = parentChildren.item(i);
                    if (node != null && node.getNodeName().equals("version") && node.getTextContent() != null) {
                        return node.getTextContent();
                    }
                }
            }
            NodeList nodes = doc.getElementsByTagName("properties");
            if (nodes.getLength() > 0) {
                Node properties = nodes.item(0);
                if (properties != null) {
                    NodeList propertiesChildren = properties.getChildNodes();
                    for (int i = 0; i < propertiesChildren.getLength(); i++) {
                        Node node = propertiesChildren.item(i);
                        if (node != null && node.getNodeName().equals("version.spring.boot") && node.getTextContent() != null) {
                            return node.getTextContent();
                        }
                    }
                }
            }
        } catch (ParserConfigurationException | SAXException e) {
            throw new IOException("Failed to parse pom.xml file.", e);
        }
        return "";
    }

    private Map<String, String> getDependenciesFromPomXmlFile(File pomXmlFile) throws IOException {
        Map<String, String> dependencies = new HashMap<>();
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;
        try {
            dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(pomXmlFile);
            doc.getDocumentElement().normalize();
            NodeList dependencyNodes = doc.getElementsByTagName("dependency");
            for (int i = 0; i < dependencyNodes.getLength(); i++) {
                Node dependencyNode = dependencyNodes.item(i);
                NodeList dependencyChildren = dependencyNode.getChildNodes();
                String groupId = "";
                String artifactId = "";
                String version = "LATEST";
                for (int j = 0; j < dependencyChildren.getLength(); j++) {
                    Node node = dependencyChildren.item(j);
                    if (node.getNodeName().equals("groupId")) {
                        groupId = node.getTextContent();
                    } else if (node.getNodeName().equals("artifactId")) {
                        artifactId = node.getTextContent();
                    } else if (node.getNodeName().equals("version")) {
                        version = node.getTextContent();
                    }
                }
                if (!groupId.isEmpty() && !artifactId.isEmpty()) {
                    dependencies.put(groupId + ":" + artifactId, version);
                }
            }
        } catch (ParserConfigurationException | SAXException e) {
            throw new IOException("Failed to parse pom.xml file.", e);
        }
        return dependencies;
    }


    private Properties getPropertiesFromFile(File file) throws IOException {
        Properties properties = new Properties();
        FileInputStream fis = new FileInputStream(file);
        try {
            properties.load(fis);

        } catch (IOException e) {
            throw new IOException("Failed to load properties from file: " + file.getAbsolutePath(), e);
        } finally {
            fis.close();
        }
        return properties;
    }

    private Map<String, Object> getPackageJsonContent(File file) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        try (Reader reader = new FileReader(file)) {
            return mapper.readValue(reader, new TypeReference<Map<String, Object>>() {
            });
        } catch (JsonProcessingException e) {
            throw new IOException("Failed to parse package.json file: " + file.getAbsolutePath(), e);
        }
    }


    @PostMapping("/github-private")
    public ResponseEntity<?> cloneGitHubPrivateRepo(@RequestBody Map<String, String> request) {
        String gitHubUrl = request.get("gitHubUrl");
        String accessToken = request.get("accessToken");
        String branch = request.get("branch");

        try {
            // Clone the GitHub repository to a temporary directory
            Path tempDirectory = Files.createTempDirectory("temp-dir");
            Git.cloneRepository()
                    .setURI(gitHubUrl)
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider(accessToken, ""))
                    .setDirectory(tempDirectory.toFile())
                    .setBranch(branch)
                    .call();

            String repoName = gitHubUrl.substring(gitHubUrl.lastIndexOf("/") + 1, gitHubUrl.lastIndexOf("."));
            File tempDir = new File(System.getProperty("java.io.tmpdir"));
            File repoDir = new File(tempDir, repoName);
            String command = "git clone -b " + branch + " " + gitHubUrl + " " + repoDir.getAbsolutePath();
            Process process = Runtime.getRuntime().exec(command, null, tempDir);
            process.waitFor();

            // Parse the appropriate file depending on the project type
            String fileType = determineAppType(tempDirectory);
            if (fileType.equals("Spring Boot")) {
                File pomXmlFile = new File(tempDirectory.toString() + "/pom.xml");
                File applicationPropertiesFile = new File(tempDirectory.toString() + "/src/main/resources/application.properties");
                String springBootVersion = getVersionFromPom(pomXmlFile);
                Map<String, String> dependencies = getDependenciesFromPomXmlFileinfo(pomXmlFile);
                Properties applicationProperties = getPropertiesFromFileinfo(applicationPropertiesFile);
                // return the Spring Boot project info
                return ResponseEntity.ok().body(
                        Map.of("type", fileType, "version", springBootVersion, "properties", applicationProperties, "dependencies", dependencies));
            } else if (fileType.equals("Node.js")) {
                File packageJsonFile = new File(tempDirectory.toString() + "/package.json");
                Map<String, Object> packageJsonContent = getPackageJsonDetails(packageJsonFile);
                // extract the Node.js project info
                Map<String, Object> nodeJsInfo = new HashMap<>();
                nodeJsInfo.put("name", packageJsonContent.get("name"));
                nodeJsInfo.put("version", packageJsonContent.get("version"));
                nodeJsInfo.put("description", packageJsonContent.get("description"));
                nodeJsInfo.put("author", packageJsonContent.get("author"));
                nodeJsInfo.put("license", packageJsonContent.get("license"));
                // extract dependencies
                Map<String, Object> dependencies = (Map<String, Object>) packageJsonContent.get("dependencies");
                // return the Node.js project info and dependencies
                Map<String, Object> response = new HashMap<>();
                response.put("type", fileType);
                response.put("name", nodeJsInfo.get("name"));
                response.put("version", nodeJsInfo.get("version"));
                response.put("description", nodeJsInfo.get("description"));
                response.put("author", nodeJsInfo.get("author"));
                response.put("license", nodeJsInfo.get("license"));
                response.put("dependencies", dependencies);
                System.out.println(response);
                return ResponseEntity.ok().body(response);
            } else {
                // unsupported project type
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                        Map.of("error", "Unsupported project type."));
            }
        } catch (Exception e) {
            // error occurred during cloning or parsing
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of("error", "An error occurred: " + e.getMessage()));
        }
    }

    private String determineAppType(Path projectDirectory) {
        // Check if the directory contains a pom.xml file
        File pomXmlFile = new File(projectDirectory.toString() + "/pom.xml");
        if (pomXmlFile.exists()) {
            return "Spring Boot";
        }

        // Check if the directory contains a package.json file
        File packageJsonFile = new File(projectDirectory.toString() + "/package.json");
        if (packageJsonFile.exists()) {
            return "Node.js";
        }

        // unsupported project type
        return "Unsupported";
    }

    private String getVersionFromPom(File pomXmlFile) throws IOException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;
        try {
            dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(pomXmlFile);
            doc.getDocumentElement().normalize();
            NodeList parentNodes = doc.getElementsByTagName("parent");
            if (parentNodes.getLength() > 0) {
                Node parent = parentNodes.item(0);
                NodeList parentChildren = parent.getChildNodes();
                for (int i = 0; i < parentChildren.getLength(); i++) {
                    Node node = parentChildren.item(i);
                    if (node != null && node.getNodeName().equals("version") && node.getTextContent() != null) {
                        return node.getTextContent();
                    }
                }
            }
            NodeList nodes = doc.getElementsByTagName("properties");
            if (nodes.getLength() > 0) {
                Node properties = nodes.item(0);
                if (properties != null) {
                    NodeList propertiesChildren = properties.getChildNodes();
                    for (int i = 0; i < propertiesChildren.getLength(); i++) {
                        Node node = propertiesChildren.item(i);
                        if (node != null && node.getNodeName().equals("version.spring.boot") && node.getTextContent() != null) {
                            return node.getTextContent();
                        }
                    }
                }
            }
        } catch (ParserConfigurationException | SAXException e) {
            throw new IOException("Failed to parse pom.xml file.", e);
        }
        return "";
    }

    private Map<String, String> getDependenciesFromPomXmlFileinfo(File pomXmlFile) throws IOException {
        Map<String, String> dependencies = new HashMap<>();
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;
        try {
            dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(pomXmlFile);
            doc.getDocumentElement().normalize();
            NodeList dependencyNodes = doc.getElementsByTagName("dependency");
            for (int i = 0; i < dependencyNodes.getLength(); i++) {
                Node dependencyNode = dependencyNodes.item(i);
                NodeList dependencyChildren = dependencyNode.getChildNodes();
                String groupId = "";
                String artifactId = "";
                String version = "LATEST";
                for (int j = 0; j < dependencyChildren.getLength(); j++) {
                    Node node = dependencyChildren.item(j);
                    if (node.getNodeName().equals("groupId")) {
                        groupId = node.getTextContent();
                    } else if (node.getNodeName().equals("artifactId")) {
                        artifactId = node.getTextContent();
                    } else if (node.getNodeName().equals("version")) {
                        version = node.getTextContent();
                    }
                }
                if (!groupId.isEmpty() && !artifactId.isEmpty()) {
                    dependencies.put(groupId + ":" + artifactId, version);
                }
            }
        } catch (ParserConfigurationException | SAXException e) {
            throw new IOException("Failed to parse pom.xml file.", e);
        }
        return dependencies;
    }


    private Properties getPropertiesFromFileinfo(File file) throws IOException {
        Properties properties = new Properties();
        FileInputStream fis = new FileInputStream(file);
        try {
            properties.load(fis);

        } catch (IOException e) {
            throw new IOException("Failed to load properties from file: " + file.getAbsolutePath(), e);
        } finally {
            fis.close();
        }
        return properties;
    }

    private Map<String, Object> getPackageJsonDetails(File file) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        try (Reader reader = new FileReader(file)) {
            return mapper.readValue(reader, new TypeReference<Map<String, Object>>() {
            });
        } catch (JsonProcessingException e) {
            throw new IOException("Failed to parse package.json file: " + file.getAbsolutePath(), e);
        }
    }

}


