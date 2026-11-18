package com.grupo3.betting.simulator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;

@EnableRabbit
@SpringBootApplication
public class MatchSimulatorApplication { public static void main(String[] args) { SpringApplication.run(MatchSimulatorApplication.class, args); } }
