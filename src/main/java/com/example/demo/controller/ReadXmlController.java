package com.example.demo.controller;

import com.example.demo.User;
import com.example.demo.dto.RequestDto;
import com.example.demo.dto.ResponseDto;
import com.example.demo.service.ReadXmlService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
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

    @PostMapping("/parse-file")
    public ResponseEntity<?> getPom(@RequestBody RequestDto requestDto) throws IOException, GitAPIException, XmlPullParserException {

        User data = readXmlService.parse(requestDto.getPath());

        ResponseDto responseDto = new ResponseDto();

        responseDto.setData(data);

        return ResponseEntity.ok(responseDto);


    }

    }
