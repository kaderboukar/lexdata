package com.lexdata.paiements;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class PaiementsServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(PaiementsServiceApplication.class, args);
    }
}
