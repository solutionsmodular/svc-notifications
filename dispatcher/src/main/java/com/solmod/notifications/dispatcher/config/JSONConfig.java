package com.solmod.notifications.dispatcher.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JSONConfig {

    @Bean
    ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
