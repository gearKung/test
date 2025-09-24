package com.example.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.example.backend")  
@EntityScan(basePackages = "com.example.backend")
public class MyBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(MyBackendApplication.class, args);
	}	

}
