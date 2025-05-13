package com.example.dto;

import lombok.Data;

@Data
public class LoginRequest {
    //这是一个登录用的报告实体
    private String username;
    private String password;
    private String captcha;
}