package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
@EnableCaching
@SpringBootApplication
public class testMainApplication {
    public static void main(String[] args) {
        SpringApplication.run(testMainApplication.class,args);
    }
}
