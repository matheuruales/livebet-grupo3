package com.grupo3.betting.engine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@EnableKafka
@SpringBootApplication
public class ResultsEngineApplication { public static void main(String[] args) { SpringApplication.run(ResultsEngineApplication.class, args); } }
