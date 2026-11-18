package com.grupo3.betting.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class BettingApiApplication { public static void main(String[] args) { SpringApplication.run(BettingApiApplication.class, args); } }
