package com.example.demo.service;

import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Paths;
@Service
public class ReadFolder {
    public String getXmlFile(String folderPath, String fileName) {
        try {
            byte[] fileBytes = Files.readAllBytes(Paths.get(folderPath, fileName));
            return new String(fileBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return "Error reading XML file: " + e.getMessage();
        }
    }
}
