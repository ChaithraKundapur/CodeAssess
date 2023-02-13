package com.example.demo.dto;

import com.example.demo.User;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ResponseDto {
    private User data;
   // private String AppData;
}
