package com.vilda.mancala.mancalaapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@Configuration
@EnableJpaRepositories(basePackages = {
		"com.vilda.mancala.mancalaapp.repository"
})
@EntityScan(basePackages = {
		"com.vilda.mancala.mancalaapp.domain"
})
public class MancalaAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(MancalaAppApplication.class, args);
	}

}
