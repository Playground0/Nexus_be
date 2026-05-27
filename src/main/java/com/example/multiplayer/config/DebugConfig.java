package com.example.multiplayer.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DebugConfig {

    @Bean
    CommandLineRunner logDb(@Value("${spring.datasource.url}") String url) {
        return args -> System.out.println("DB URL = " + url);
    }
}