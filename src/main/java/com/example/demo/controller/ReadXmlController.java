package com.example.demo.controller;

import com.example.demo.User;
import com.example.demo.dto.RequestDto;
import com.example.demo.dto.ResponseDto;
import com.example.demo.service.ReadXmlService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;


@RestController
@RequiredArgsConstructor
public class ReadXmlController {
    private final ReadXmlService readXmlService;
//    private final AppProperties  myAppProperties;

    @PostMapping("/get-pom")
    public ResponseEntity<?> getPom(@RequestBody RequestDto requestDto) throws IOException {
        User data = readXmlService.getXml(requestDto.getPath());
        List<User> data1 = readXmlService.doCodeAssessment(requestDto.getPath());
//        System.out.println(data1.getUrl()+"check2");
        ResponseDto responseDto = new ResponseDto();
       // ResponseDto responseDto1 = new ResponseDto();

//        User d = new User();
//        User dat = readXmlService.parseApplicationProperties(requestDto.getPath());
        responseDto.setData(data);
        responseDto.setData1(data1);

//        System.out.println(dat.getUrl()+"============================");

        return ResponseEntity.ok(responseDto);


    }





}
