package com.example.demo.dto;

import com.example.demo.User;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ResponseDto {
    private User data;
//    private List<User> data1;
//    private User data3;
}
