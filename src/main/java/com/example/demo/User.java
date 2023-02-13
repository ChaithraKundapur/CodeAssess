package com.example.demo;

import lombok.*;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private String artifactId;
    private String version;
    private List<String> dependencies;
}
