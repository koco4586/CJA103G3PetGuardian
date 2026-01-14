package com;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({
    "com.forum",
    "com.news",
    "com.sitter"
})

public class PetGuardianApplication {

	public static void main(String[] args) {
		SpringApplication.run(PetGuardianApplication.class, args);
	}
 
}
