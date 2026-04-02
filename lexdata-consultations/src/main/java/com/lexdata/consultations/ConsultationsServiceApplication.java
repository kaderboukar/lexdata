package com.lexdata.consultations;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class ConsultationsServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ConsultationsServiceApplication.class, args);
    }
}
