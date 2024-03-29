package com.example.demo.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;





@CrossOrigin(origins = "*")
@RestController
@RequiredArgsConstructor
public class ReadXmlController {

    @PostMapping("/parse")
    public Object cloneGitHubPublicRepo(@RequestBody Map<String, String> request) {
        String repoPath = request.get("repoPath");

        try {

            // Parse the appropriate file depending on the project type
            String fileType = determineProjectType(Path.of(repoPath));
            if (fileType.equals("Spring Boot Maven")) {
                File pomXmlFile = new File(repoPath + "/pom.xml");
                File applicationPropertiesFile = new File(repoPath + "/src/main/resources/application.properties");
                String springBootVersion = getVersionFromPomXmlFile(pomXmlFile);
                Map<String, String> dependencies = getDependenciesFromPomXmlFile(pomXmlFile);
                Properties applicationProperties = getPropertiesFromFile(applicationPropertiesFile);
                Map<String, String> latestVersions = getDependenciesFromPomXmlFile1(pomXmlFile);
                String javaVersion = getJavaVersion(pomXmlFile);


                // delete the cloned repository from the provided path
                FileUtils.deleteDirectory(new File(repoPath));

                // return the Spring Boot project info
                return ResponseEntity.ok().body(
                        Map.of("type", fileType, "SpringBootVersion", springBootVersion, "properties", applicationProperties, "dependencies", dependencies, "LatestVersion", latestVersions,"JavaVersion",javaVersion));
            }
            else if (fileType.equals("Node.js")) {

                File packageJsonFile = new File(repoPath + "/package.json");
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

//              response.put("totalLinesOfCode", totalLinesOfCode1.get());
                response.put("type", fileType);
                response.put("name", nodeJsInfo.get("name"));
                response.put("version", nodeJsInfo.get("version"));
                response.put("description", nodeJsInfo.get("description"));
                response.put("author", nodeJsInfo.get("author"));
                response.put("license", nodeJsInfo.get("license"));
                response.put("dependencies", dependencies);


                Map<String, String> dependencies1 = getDependenciesFromPackageJsonFile(packageJsonFile);
                response.put("LatestVersion",dependencies1);


                // delete the cloned repository from the provided path
                FileUtils.deleteDirectory(new File(repoPath));

                return ResponseEntity.ok().body(response);
            }

            else if (fileType.equals("PHP")) {
                // delete the cloned repository from the provided path
                FileUtils.deleteDirectory(new File(repoPath));

                // Return the PHP project info
                return ResponseEntity.ok().body(
                        Map.of("type", fileType));
            }

            else if (fileType.equals("Spring Boot Gradle")) {
                File buildGradleFile = new File(repoPath + "/build.gradle");

                // Fetch the latest versions after adding the plugin
                Map<String, String> latestVersions = getLatestVersionsFromBuildGradleFile(buildGradleFile);
                String springBootVersion = getSpringBootVersionFromBuildGradleFile(buildGradleFile);
                Map<String, String> dependencies = getDependenciesFromBuildGradleFile(buildGradleFile);

                // Delete the cloned repository from the provided path
                FileUtils.deleteDirectory(new File(repoPath));

                return ResponseEntity.ok().body(
                        Map.of("type", fileType, "LatestVersion", latestVersions, "SpringBootVersion", springBootVersion, "dependencies", dependencies));
            }
            else {
                // unsupported project type
                // delete the cloned repository from the provided path
                FileUtils.deleteDirectory(new File(repoPath));
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                        Map.of("error", "Unsupported project type."));
            }
        }
        catch (Exception e) {
            // error occurred during parsing or deleting the cloned repository
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of("error", "An error occurred: " + e.getMessage()));
        }
    }


    private String determineProjectType(Path projectDirectory) {
        // Check if the directory contains a pom.xml file
        File pomXmlFile = new File(projectDirectory.toString() + "/pom.xml");
        if (pomXmlFile.exists()) {
            return "Spring Boot Maven";
        }

        File buildGradleFile = new File(projectDirectory.toString() + "/build.gradle");
        if (buildGradleFile.exists()) {
            return "Spring Boot Gradle";
        }

        // Check if the directory contains a package.json file
        File packageJsonFile = new File(projectDirectory.toString() + "/package.json");
        if (packageJsonFile.exists()) {
            return "Node.js";
        }

        // Check if the directory contains an index.php file
        File indexPhpFile = new File(projectDirectory.toString() + "/index.php");
        if (indexPhpFile.exists()) {
            return "PHP";
        }

        // unsupported project type
        return "Unsupported";
    }

    private String getSpringBootVersionFromBuildGradleFile(File buildGradleFile) throws IOException {
        String version = "";
        BufferedReader reader = new BufferedReader(new FileReader(buildGradleFile));
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.contains("id 'org.springframework.boot'") && line.contains("version")) {
                Pattern pattern = Pattern.compile("version\\s+['\"]([^'\"]+)['\"]");
                Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    version = matcher.group(1);
                    break;
                }
            }
        }
        reader.close();
        return version;
    }

    private static Map<String, String> getDependenciesFromBuildGradleFile(File buildGradleFile) throws IOException {
        Map<String, String> dependencies = new HashMap<>();
        BufferedReader reader = new BufferedReader(new FileReader(buildGradleFile));
        String line;
        boolean insideDependenciesBlock = false;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.equals("dependencies {")) {
                insideDependenciesBlock = true;
            } else if (line.equals("}")) {
                insideDependenciesBlock = false;
            } else if (insideDependenciesBlock) {
                String[] parts = line.split(":");
                if (parts.length >= 2) {
                    String name = parts[1].replaceAll("[\"'\\)].*$", ""); // Remove the ending " or ' or ) and any characters after
                    String version = parts.length >= 3 ? parts[2].replaceAll("[^\\d.]", "") : "latest";
                    dependencies.put(name, version);
                }
            }
        }
        reader.close();
        return dependencies;
    }

    private static Map<String, String> getLatestVersionsFromBuildGradleFile(File buildGradleFile) throws IOException {
        Map<String, String> dependencies = getDependenciesFromBuildGradleFile(buildGradleFile);
        Map<String, String> latestVersions = new HashMap<>();

        // Set "-" as the version for each dependency
        for (String dependency : dependencies.keySet()) {
            latestVersions.put(dependency, "-");
        }

        return latestVersions;
    }

    private static Map<String, String> parseLatestVersions(String output) {
        Map<String, String> latestVersions = new HashMap<>();

        // Split the output into lines
        String[] lines = output.split("\n");

        // Process each line to extract the latest versions
        for (String line : lines) {
            // Assuming the format of each line is: <dependency> -> <latestVersion>
            String[] parts = line.split(" -> ");
            if (parts.length == 2) {
                String dependency = parts[0].trim();
                String latestVersion = parts[1].trim();

                // Extract the artifact ID from the dependency string and remove the old version
                int colonIndex = dependency.lastIndexOf(':');
                if (colonIndex > 0) {
                    String artifactId = dependency.substring(colonIndex + 1).trim();
                    // Remove the old version from the artifact ID
                    int openBracketIndex = artifactId.lastIndexOf('[');
                    if (openBracketIndex > 0) {
                        artifactId = artifactId.substring(0, openBracketIndex).trim();
                    }
                    // Remove the "]" at the end of the latest version
                    if (latestVersion.endsWith("]")) {
                        latestVersion = latestVersion.substring(0, latestVersion.length() - 1).trim();
                    }

                    // Check if the artifact ID is not a plugin
                    if (!artifactId.contains(".gradle.plugin")) {
                        latestVersions.put(artifactId, latestVersion);
                    }
                }
            }
        }

        return latestVersions;
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
                    if (node.getNodeName().equals("artifactId")) {
                        artifactId = node.getTextContent();
                        break; // Stop iterating further if artifactId is found
                    }
                }
                if (!artifactId.isEmpty()) {
                    dependencies.put(artifactId, version);
                }
            }
        } catch (ParserConfigurationException | SAXException e) {
            throw new IOException("Failed to parse pom.xml file.", e);
        }
        return dependencies;
    }

    private Map<String, String> getDependenciesFromPomXmlFile1(File pomXmlFile) throws IOException {
        Map<String, String> dependencies = new HashMap<>();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(pomXmlFile);

            NodeList dependencyNodes = document.getElementsByTagName("dependency");
            for (int i = 0; i < dependencyNodes.getLength(); i++) {
                Node dependencyNode = dependencyNodes.item(i);
                if (dependencyNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element dependencyElement = (Element) dependencyNode;
                    String groupId = getElementTextByTagName(dependencyElement, "groupId");
                    String artifactId = getElementTextByTagName(dependencyElement, "artifactId");
                    String version = getElementTextByTagName(dependencyElement, "version");

                    String latestVersion = getLatestVersion1(groupId,artifactId);
                    dependencies.put(artifactId, latestVersion);
                }
            }
        } catch (ParserConfigurationException | SAXException e) {
            throw new IOException("Failed to parse pom.xml file: " + pomXmlFile.getAbsolutePath(), e);
        }
        return dependencies;
    }

    private String getElementTextByTagName(Element element, String tagName) {
        NodeList nodeList = element.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0) {
            Node node = nodeList.item(0);
            return node.getTextContent();
        }
        return null;
    }

    private String getLatestVersion1(String groupId, String artifactId) throws IOException {
        String command = "mvn versions:display-dependency-updates -Dincludes=" + groupId + ":" + artifactId;
        Process process = Runtime.getRuntime().exec(command);
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        StringBuilder outputBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            outputBuilder.append(line).append("\n");
        }

        String commandOutput = outputBuilder.toString();

        // Parse the command output and extract the latest version
        String latestVersion = null;
        String[] lines = commandOutput.split("\n");
        for (String currentLine : lines) {
            if (currentLine.contains(groupId) && currentLine.contains(artifactId)) {
                // Assuming the format of the line is: <groupId>:<artifactId> <currentVersion> -> <latestVersion>
                String[] parts = currentLine.split(" -> ");
                if (parts.length > 1) {
                    latestVersion = parts[1].trim();
                    break;
                }
            }
        }
        if (latestVersion == null) {
            latestVersion = "-";
        }

        return latestVersion;
    }

    private String getLatestVersion(String dependencyName) throws IOException {
        Process process = Runtime.getRuntime().exec("npm view " + dependencyName + " version");
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String latestVersion = reader.lines().collect(Collectors.joining());
        return latestVersion;
    }

    private Map<String, String> getDependenciesFromPackageJsonFile(File packageJsonFile) throws IOException {
        Map<String, String> dependencies = new HashMap<>();
        ObjectMapper mapper = new ObjectMapper();
        try (Reader reader = new FileReader(packageJsonFile)) {
            Map<String, Object> packageJsonContent = mapper.readValue(reader, new TypeReference<Map<String, Object>>() {});
            if (packageJsonContent.containsKey("dependencies")) {
                Map<String, Object> dependencyMap = (Map<String, Object>) packageJsonContent.get("dependencies");
                for (Map.Entry<String, Object> entry : dependencyMap.entrySet()) {
                    String dependencyName = entry.getKey();
                    String version = (String) entry.getValue();
                    String latestVersion = getLatestVersion(dependencyName);
                    dependencies.put(dependencyName, latestVersion);
                }
            }
        } catch (JsonProcessingException e) {
            throw new IOException("Failed to parse package.json file: " + packageJsonFile.getAbsolutePath(), e);
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
    private String getJavaVersion(File pomXmlFile) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(pomXmlFile);

        Element propertiesElement = (Element) document.getElementsByTagName("properties").item(0);
        String javaVersion = getElementTextByTagName(propertiesElement, "java.version");

        return javaVersion;
    }

    }