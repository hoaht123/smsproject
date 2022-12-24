package com.example.smsapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;

@SpringBootApplication
public class SmsapiApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmsapiApplication.class, args);
    }

}
