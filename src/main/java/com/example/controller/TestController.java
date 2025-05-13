package com.example.controller;

import com.example.dto.LoginRequest;
import com.example.pojo.Result;
import com.example.pojo.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import jakarta.websocket.server.PathParam;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/test")
public class TestController {
    private static final ObjectMapper mapper = new ObjectMapper();
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @GetMapping("/test1")
    public Result test1(@PathParam("name") String name){
        stringRedisTemplate.opsForValue().set("name",name);
        return new Result(true,"添加成功");
    }
    @GetMapping("/test2")
    public Result test1(@PathParam("name") String name,@PathParam("password")String password) throws JsonProcessingException {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setPassword(password);
        loginRequest.setUsername(name);
        String json = mapper.writeValueAsString(loginRequest);
        stringRedisTemplate.opsForValue().set("name",json,30, TimeUnit.SECONDS);
        String getUserJson = stringRedisTemplate.opsForValue().get("name");
        LoginRequest getUser = mapper.readValue(getUserJson,LoginRequest.class);
        return new Result(true,"添加成功",getUser);
    }

}
