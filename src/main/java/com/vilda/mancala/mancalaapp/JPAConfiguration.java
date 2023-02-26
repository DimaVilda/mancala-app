package com.vilda.mancala.mancalaapp;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = {
        "com.vilda.mancala.mancalaapp.repository"
})
@EntityScan(basePackages = {
        "com.vilda.mancala.mancalaapp.domain"
})
public class JPAConfiguration {
}
