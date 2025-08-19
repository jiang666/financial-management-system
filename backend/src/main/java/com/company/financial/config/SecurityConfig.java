package com.company.financial.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
@Slf4j
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        log.info("Configuring security with permissive access for development");
        
        http
            .authorizeRequests()
                .antMatchers("/health/**").permitAll()
                .anyRequest().permitAll()
                .and()
            .csrf().disable()
            .headers().frameOptions().disable();
    }
}