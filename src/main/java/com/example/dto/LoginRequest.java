package com.example.dto;

import lombok.Data;

@Data
public class LoginRequest {
    //这是一个登录用的报告实体
    //这是userName
    private String username;
    //这是密码
    private String password;
    private String captcha;
}