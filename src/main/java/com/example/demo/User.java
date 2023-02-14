package com.example.demo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include. NON_NULL)
public class User {
    private String artifactId;
    private String version;
    private List<String> dependencies;
    private String url;
    private String username;
    private String password;
    private String platform;
}
