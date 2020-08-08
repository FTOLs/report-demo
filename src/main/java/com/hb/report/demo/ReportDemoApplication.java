package com.hb.report.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@SpringBootApplication
@EnableWebMvc
public class ReportDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReportDemoApplication.class, args);
    }

}
