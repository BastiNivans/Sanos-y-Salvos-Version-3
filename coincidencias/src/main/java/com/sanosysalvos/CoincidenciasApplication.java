package com.sanosysalvos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CoincidenciasApplication {

    public static void main(String[] args) {
        SpringApplication.run(CoincidenciasApplication.class, args);
    }
}