package com.nhom12.enggo_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.nhom12.enggo_backend.repository")
public class EnggoBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(EnggoBackendApplication.class, args);
	}

}
