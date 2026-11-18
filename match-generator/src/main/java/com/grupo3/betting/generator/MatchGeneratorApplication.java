package com.grupo3.betting.generator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class MatchGeneratorApplication {
    public static void main(String[] args) { SpringApplication.run(MatchGeneratorApplication.class, args); }
}
