package com.example.demo.controller;

import com.example.demo.User;
import com.example.demo.dto.RequestDto;
import com.example.demo.dto.ResponseDto;
import com.example.demo.service.AppProperties;
import com.example.demo.service.ReadXmlService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;


@RestController
@RequiredArgsConstructor
public class ReadXmlController {
    private final ReadXmlService readXmlService;
    private final AppProperties  myAppProperties;
   /* @GetMapping("/artifactid")
    public ResponseEntity<?>getArtifactid() throws IOException {

//    String data = String.valueOf(readXmlService.getXml());

        return ResponseEntity.ok(data);
    }*/


    @PostMapping("/get-pom")
    public ResponseEntity<?> getPom(@RequestBody RequestDto requestDto) throws IOException {
        User data = readXmlService.getXml(requestDto.getPath());
        ResponseDto responseDto = new ResponseDto();

        responseDto.setData(data);
       // responseDto.setAppData(myAppProperties.getTitle());
        return ResponseEntity.ok(responseDto);
    }





}
