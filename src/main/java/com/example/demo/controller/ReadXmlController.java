package com.example.demo.controller;

import com.example.demo.User;
import com.example.demo.dto.RequestDto;
import com.example.demo.dto.ResponseDto;
import com.example.demo.service.ReadFileService;
import lombok.RequiredArgsConstructor;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;

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

}


