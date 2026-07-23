package com.jpmns.task;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.jpmns.task.configuration.security.SecurityConfigProperties;

@SpringBootApplication
@EnableConfigurationProperties(SecurityConfigProperties.class)
public class TaskApplication {

    public static void main(String[] args) {
        SpringApplication.run(TaskApplication.class, args);
    }

}
