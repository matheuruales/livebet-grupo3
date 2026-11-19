package com.grupo3.betting.engine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableKafka
@EnableScheduling
@SpringBootApplication
public class ResultsEngineApplication { public static void main(String[] args) { SpringApplication.run(ResultsEngineApplication.class, args); } }
