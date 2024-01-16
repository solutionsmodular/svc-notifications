package com.solmod.notification.admin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        WebMvcConfigurer.super.addViewControllers(registry);
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/nea/**") // notification engine admin
                        .allowedOrigins("http://localhost", "http://localhost:7000")
                        .allowedMethods("GET", "PUT", "PATCH", "POST", "OPTIONS")
                        .allowedHeaders("Origin", "Access-Control-Allow-Origin", "Content-Type",
                                "Accept", "JwtToken", "Authorization", "Origin, Accept", "X-Requested-With",
                                "Access-Control-Request-Method", "Access-Control-Request-Headers")
                        .exposedHeaders("Origin", "Access-Control-Allow-Origin", "Content-Type",
                                "Accept", "JwtToken", "Authorization", "Access-Control-Allow-Credentials", "Filename")
                        .allowCredentials(false).maxAge(3600);
            }
        };
    }
}
