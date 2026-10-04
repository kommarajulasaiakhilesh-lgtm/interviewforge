package com.interviewforge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(excludeName = "org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration")
public class InterviewForgeApplication {

    public static void main(String[] args) {
        SpringApplication.run(InterviewForgeApplication.class, args);
    }
}
