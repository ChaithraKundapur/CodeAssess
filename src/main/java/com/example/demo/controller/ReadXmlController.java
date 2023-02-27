package com.example.demo.controller;

import com.example.demo.User;
import com.example.demo.dto.RequestDto;
import com.example.demo.dto.ResponseDto;
import com.example.demo.service.ReadFileService;
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

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


    @PostMapping("/parse")
    public String cloneGitHubRepository(@RequestBody String payload) {
        try {
            // Parse the JSON payload
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(payload);

            String gitHubUrl = jsonNode.get("gitHubUrl").asText();
            String accessToken = jsonNode.get("accessToken").asText();
            String branch = jsonNode.get("branch").asText();

            // Clone the repository
            String repoName = gitHubUrl.substring(gitHubUrl.lastIndexOf("/") + 1, gitHubUrl.lastIndexOf("."));
            String command = "git clone -b " + branch + " " + gitHubUrl + " " + repoName;
            Process process = Runtime.getRuntime().exec(command, null, new File(System.getProperty("java.io.tmpdir")));
            process.waitFor();

            // Read the pom.xml file and parse the application name and version
            File pomFile = new File(System.getProperty("java.io.tmpdir"), repoName + "/pom.xml");
            if (pomFile.exists()) {
                MavenXpp3Reader reader = new MavenXpp3Reader();
                Model model = reader.read(new BufferedReader(new InputStreamReader(FileUtils.openInputStream(pomFile))));
                String appName = model.getName();
                String appVersion = model.getVersion();
                List<Dependency> dependencies = model.getDependencies();
                StringBuilder sb = new StringBuilder();
                sb.append("Spring Boot Application Name: ").append(appName)
                        .append(", Version: ").append(appVersion).append("\n");
                sb.append("Dependencies:\n");
                for (Dependency dependency : dependencies) {
                    sb.append(dependency.getGroupId()).append(":")
                            .append(dependency.getArtifactId()).append(":")
                            .append(dependency.getVersion()).append("\n");
                }
                // Read the application.properties file and add its contents to the response
                File propertiesFile = new File(System.getProperty("java.io.tmpdir"), repoName + "/src/main/resources/application.properties");
                if (propertiesFile.exists()) {
                    Properties props = new Properties();
                    props.load(new FileReader(propertiesFile));
                    sb.append("Properties:\n");
                    for (String key : props.stringPropertyNames()) {
                        String value = props.getProperty(key);
                        sb.append(key).append("=").append(value).append("\n");
                    }
                }
                return sb.toString();
            }

            // Read the package.json file and parse the application name and version
            File packageFile = new File(System.getProperty("java.io.tmpdir"), repoName + "/package.json");
            if (packageFile.exists()) {
                JsonNode packageNode = mapper.readTree(packageFile);
                String appName = packageNode.get("name").asText();
                String appVersion = packageNode.get("version").asText();
                JsonNode dependenciesNode = packageNode.get("dependencies");
                StringBuilder sb = new StringBuilder();
                sb.append("Node.js Application Name: ").append(appName)
                        .append(", Version: ").append(appVersion).append("\n");
                sb.append("Dependencies:\n");
                Iterator<String> it = dependenciesNode.fieldNames();
                while (it.hasNext()) {
                    String key = it.next();
                    String value = dependenciesNode.get(key).asText();
                    sb.append(key).append(":").append(value).append("\n");
                }
                return sb.toString();
            }

            return "Unknown application type.";

        } catch (IOException | InterruptedException | XmlPullParserException e) {
            e.printStackTrace();
            return "Error cloning repository.";
        }
    }


    

    @PostMapping("/test")
    public ResponseEntity<?> cloneGitHubRepo(@RequestBody Map<String, String> request) {
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
                Properties applicationProperties = getPropertiesFromFile(applicationPropertiesFile);
                // return the Spring Boot project info
                return ResponseEntity.ok().body(
                        Map.of("type", fileType, "version", springBootVersion, "properties", applicationProperties));
            } else if (fileType.equals("Node.js")) {
                File packageJsonFile = new File(tempDirectory.toString() + "/package.json");
                Properties packageJsonProperties = getPropertiesFromFile(packageJsonFile);
                // return the Node.js project info
                return ResponseEntity.ok().body(
                        Map.of("type", fileType, "properties", packageJsonProperties));
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
                    if (node.getNodeName().equals("version")) {
                        return node.getTextContent();
                    }
                }
            }
            NodeList nodes = doc.getElementsByTagName("properties");
            if (nodes.getLength() > 0) {
                Node properties = nodes.item(0);
                NodeList propertiesChildren = properties.getChildNodes();
                for (int i = 0; i < propertiesChildren.getLength(); i++) {
                    Node node = propertiesChildren.item(i);
                    if (node.getNodeName().equals("version.spring.boot")) {
                        return node.getTextContent();
                    }
                }
            }
        } catch (ParserConfigurationException | SAXException e) {
            throw new IOException("Failed to parse pom.xml file.", e);
        }
        return "";
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

}


