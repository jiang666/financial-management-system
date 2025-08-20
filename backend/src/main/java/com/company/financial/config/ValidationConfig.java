package com.company.financial.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;

import javax.validation.Validator;

/**
 * 参数校验配置类
 */
@Configuration
@Slf4j
public class ValidationConfig {

    /**
     * 配置Bean校验器
     */
    @Bean
    public Validator validator() {
        log.info("Configuring Bean Validator");
        return new LocalValidatorFactoryBean();
    }

    /**
     * 配置方法级别的校验
     */
    @Bean
    public MethodValidationPostProcessor methodValidationPostProcessor() {
        log.info("Configuring Method Validation Post Processor");
        MethodValidationPostProcessor processor = new MethodValidationPostProcessor();
        processor.setValidator(validator());
        return processor;
    }
}