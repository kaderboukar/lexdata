package com.lexdata.annuaire;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class AnnuaireServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AnnuaireServiceApplication.class, args);
    }
}
