package com.smartenergy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.smartenergy"})
public class AEOSApplication {

    public static void main(String[] args) {
        SpringApplication.run(AEOSApplication.class, args);
        System.out.println("=== AEOS - Smart Energy System (Web) ===");
        System.out.println("Access the application at: http://localhost:8080");
    }
}
