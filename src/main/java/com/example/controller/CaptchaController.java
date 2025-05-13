package com.example.controller;


import com.example.service.CaptchaService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/captcha")
public class CaptchaController {
    @Autowired
    private CaptchaService captchaService;
    @GetMapping("/pic")
    public void getCaptcha(HttpServletRequest request, HttpServletResponse response) throws IOException {
        captchaService.generateCaptcha(request,response);
    }
}
