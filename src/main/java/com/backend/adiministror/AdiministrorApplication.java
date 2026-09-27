package com.backend.adiministror;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AdiministrorApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdiministrorApplication.class, args);
    }

}
