package com.company.financial;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class FinancialManagementSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(FinancialManagementSystemApplication.class, args);
        log.info("Financial Management System Started Successfully!");
    }

}