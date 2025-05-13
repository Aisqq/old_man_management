package com.example.service;

import com.example.pojo.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public interface CaptchaService {
    void generateCaptcha(HttpServletRequest request, HttpServletResponse response) throws IOException;
    boolean validateCaptcha(HttpServletRequest request, String userInput);
    Result sendCode(String phone);


    Result verifyCode(String phone, String code,HttpServletResponse response) throws IOException;
}
