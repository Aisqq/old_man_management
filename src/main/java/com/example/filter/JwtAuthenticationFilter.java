package com.example.filter;

import com.example.dto.LoginRequest;
import com.example.service.CaptchaService;
import com.example.utils.JwtTokenUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.Collections;

public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private final CaptchaService captchaService;
    private final JwtTokenUtil jwtTokenUtil;
    public JwtAuthenticationFilter(AuthenticationManager authenticationManager, JwtTokenUtil jwtTokenUtil,CaptchaService captchaService) {
        this.captchaService = captchaService;
        this.jwtTokenUtil = jwtTokenUtil;
        setAuthenticationManager(authenticationManager);
        setFilterProcessesUrl("/api/auth/login");
        setPostOnly(true);

    }
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
                                                HttpServletResponse response) throws AuthenticationException {
        System.out.println("Filter processing: " + request.getRequestURI());

        // 检查请求方法
        if (!request.getMethod().equals("POST")) {
            throw new BadCredentialsException("无效请求" + request.getMethod());
        }

        try {
            LoginRequest creds = new ObjectMapper()
                    .readValue(request.getInputStream(), LoginRequest.class);
            System.out.printf(creds.toString());
            if (!captchaService.validateCaptcha(request, creds.getCaptcha())) {
                throw new BadCredentialsException("验证码错误");
            }
            return getAuthenticationManager().authenticate(
                    new UsernamePasswordAuthenticationToken(
                            creds.getUsername(),
                            creds.getPassword(),
                            Collections.emptyList())
            );
        } catch (IOException e) {
            throw new BadCredentialsException("请求体格式错误");
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain chain,
                                            Authentication authResult) throws IOException {
        User user = (User) authResult.getPrincipal();
        String token = jwtTokenUtil.generateToken(user);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        Cookie cookie = new Cookie("jwtToken",token);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setMaxAge(10);
        cookie.setPath("/");
        response.addCookie(cookie);
        response.getWriter().write(String.format(
                "{\"token\": \"%s\", \"username\": \"%s\"}",
                token, user.getUsername()
        ));
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request,
                                              HttpServletResponse response,
                                              AuthenticationException failed) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(String.format(
                "{\"error\": \"%s\", \"message\": \"%s\"}",
                "Authentication Failed",
                failed.getMessage()
        ));
    }
}
