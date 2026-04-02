package com.lexdata.tribune;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class TribuneServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(TribuneServiceApplication.class, args);
    }
}
