package ru.pocgg.SNSApp;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableRabbit
public class SNSApp {
    public static void main(String[] args) {
        SpringApplication.run(SNSApp.class, args);
    }
}
