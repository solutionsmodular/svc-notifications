package com.solmod.notification.admin.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig  {

    @Bean
    protected SecurityFilterChain configure(HttpSecurity http) throws Exception {
//        http.cors().and().csrf().disable();

        http.authorizeHttpRequests((authz) -> authz.requestMatchers(
                        "/actuator/**",
                        "/open/**",
                        "/ne/**"
                ).permitAll()
                .requestMatchers("/high_level_url_A/sub_level_2").hasRole("USER2")
                .requestMatchers("/admin/**").authenticated()
                .requestMatchers("/assets/**", "/index.html", "login.html").permitAll());

        return http.build();
    }


    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth
                .inMemoryAuthentication()
                .withUser("user").password("{noop}pass").roles("USER");
    }
}