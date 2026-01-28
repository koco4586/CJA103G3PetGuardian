package com.petguardian;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PetGuardianApplication {

    public static void main(String[] args) {
        SpringApplication.run(PetGuardianApplication.class, args);
    }
}
