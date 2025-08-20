package com.company.financial.util.validation;

import org.springframework.util.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

/**
 * 会计科目编码校验器
 */
public class AccountCodeValidator implements ConstraintValidator<ValidAccountCode, String> {

    private static final Pattern ACCOUNT_CODE_PATTERN = Pattern.compile("^[0-9A-Z]{4,20}$");

    @Override
    public void initialize(ValidAccountCode constraintAnnotation) {
        // 初始化方法，可以在这里获取注解参数
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // 如果值为空，交由 @NotNull 等注解处理
        if (!StringUtils.hasText(value)) {
            return true;
        }
        
        return ACCOUNT_CODE_PATTERN.matcher(value).matches();
    }
}