package com.example.demo.controller;

import com.example.demo.User;
import com.example.demo.dto.RequestDto;
import com.example.demo.dto.ResponseDto;
import com.example.demo.service.ReadXmlService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.eclipse.jgit.api.Git;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;


@RestController
@RequiredArgsConstructor
public class ReadXmlController {
    private final ReadXmlService readXmlService;
//    private final AppProperties  myAppProperties;

    @PostMapping("/parse-file")
    public ResponseEntity<?> getPom(@RequestBody RequestDto requestDto) throws IOException {
       // User data = readXmlService.getXml(requestDto.getPath());
        User data = readXmlService.parse(requestDto.getPath());
        //List<User> data1 = readXmlService.doCodeAssessment(requestDto.getPath());
//        System.out.println(data1.getUrl()+"check2");
        ResponseDto responseDto = new ResponseDto();
        // ResponseDto responseDto1 = new ResponseDto();

//        User d = new User();
//        User dat = readXmlService.parseApplicationProperties(requestDto.getPath());
        responseDto.setData(data);
       // responseDto.setData1(data1);

//        System.out.println(dat.getUrl()+"============================");

        return ResponseEntity.ok(responseDto);


    }

    @RestController
    public class GitHubController {

        @PostMapping("/parse")
        public ResponseEntity<String> parse(@RequestBody Map<String, String> requestBody) {
            String repositoryLink = requestBody.get("repositoryLink");

            try {
                // Clone the repository
//                Git.cloneRepository()
//                        .setURI(repositoryLink)
//                        .setDirectory(new File("repository"))
//                        .call();

                // Load the pom.xml file
                File pomFile = new File("repository/pom.xml");
                MavenXpp3Reader reader = new MavenXpp3Reader();
                Model model = reader.read(new FileReader(pomFile));

                // Print the project name and version
                System.out.println("Project name: " + model.getName());
                System.out.println("Project version: " + model.getVersion());
                System.out.println("Project artifactId: " + model.getArtifactId());
                System.out.println("Project Dependencies: " + model.getDependencies());




                // Load the application.properties file
                File propsFile = new File("repository/src/main/resources/application.properties");
                Properties props = new Properties();
                props.load(new FileReader(propsFile));

                // Print the properties
                System.out.println("spring.datasource.url: " + props.getProperty("spring.datasource.url"));
                System.out.println("spring.datasource.username: " + props.getProperty("spring.datasource.username"));
                System.out.println("spring.jpa.database-platform: " + props.getProperty("spring.jpa.database-platform"));


                return ResponseEntity.ok("Parsing completed successfully.");

            } catch (Exception e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error occurred while parsing the repository.");
            }
        }
    }






}