package com.nhom12.enggo_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class EnggoBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(EnggoBackendApplication.class, args);
	}

}
