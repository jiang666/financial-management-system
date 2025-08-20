package com.company.financial.util;

import com.company.financial.exception.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

/**
 * 数据校验工具类
 */
@Slf4j
public class ValidationUtils {

    // 常用正则表达式
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");
    private static final Pattern ACCOUNT_CODE_PATTERN = Pattern.compile("^[0-9A-Z]{4,20}$");
    private static final Pattern UUID_PATTERN = Pattern.compile("^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$");

    /**
     * 校验字符串不为空
     */
    public static void validateNotEmpty(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            throw new ValidationException(fieldName, "不能为空");
        }
    }

    /**
     * 校验字符串长度
     */
    public static void validateLength(String value, String fieldName, int minLength, int maxLength) {
        if (value == null) {
            throw new ValidationException(fieldName, "不能为空");
        }
        if (value.length() < minLength || value.length() > maxLength) {
            throw new ValidationException(fieldName, 
                String.format("长度必须在 %d 到 %d 之间", minLength, maxLength));
        }
    }

    /**
     * 校验邮箱格式
     */
    public static void validateEmail(String email, String fieldName) {
        if (StringUtils.hasText(email) && !EMAIL_PATTERN.matcher(email).matches()) {
            throw new ValidationException(fieldName, "邮箱格式不正确");
        }
    }

    /**
     * 校验手机号格式
     */
    public static void validatePhone(String phone, String fieldName) {
        if (StringUtils.hasText(phone) && !PHONE_PATTERN.matcher(phone).matches()) {
            throw new ValidationException(fieldName, "手机号格式不正确");
        }
    }

    /**
     * 校验会计科目编码格式
     */
    public static void validateAccountCode(String code, String fieldName) {
        validateNotEmpty(code, fieldName);
        if (!ACCOUNT_CODE_PATTERN.matcher(code).matches()) {
            throw new ValidationException(fieldName, "科目编码只能包含数字和大写字母，长度4-20位");
        }
    }

    /**
     * 校验UUID格式
     */
    public static void validateUUID(String uuid, String fieldName) {
        validateNotEmpty(uuid, fieldName);
        if (!UUID_PATTERN.matcher(uuid.toLowerCase()).matches()) {
            throw new ValidationException(fieldName, "UUID格式不正确");
        }
    }

    /**
     * 校验金额
     */
    public static void validateAmount(BigDecimal amount, String fieldName) {
        if (amount == null) {
            throw new ValidationException(fieldName, "不能为空");
        }
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException(fieldName, "不能为负数");
        }
        // 检查小数位数不超过2位
        if (amount.scale() > 2) {
            throw new ValidationException(fieldName, "小数位数不能超过2位");
        }
    }

    /**
     * 校验正数
     */
    public static void validatePositiveAmount(BigDecimal amount, String fieldName) {
        validateAmount(amount, fieldName);
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException(fieldName, "必须大于0");
        }
    }

    /**
     * 校验日期格式
     */
    public static LocalDate validateAndParseDate(String dateStr, String fieldName) {
        validateNotEmpty(dateStr, fieldName);
        try {
            return LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException e) {
            throw new ValidationException(fieldName, "日期格式不正确，应为 yyyy-MM-dd");
        }
    }

    /**
     * 校验日期范围
     */
    public static void validateDateRange(LocalDate startDate, LocalDate endDate, String fieldName) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new ValidationException(fieldName, "开始日期不能晚于结束日期");
        }
    }

    /**
     * 校验会计期间格式 (YYYY-MM)
     */
    public static void validateFiscalPeriod(String period, String fieldName) {
        validateNotEmpty(period, fieldName);
        if (!Pattern.matches("^\\d{4}-(0[1-9]|1[0-2])$", period)) {
            throw new ValidationException(fieldName, "会计期间格式不正确，应为 YYYY-MM");
        }
    }

    /**
     * 校验枚举值
     */
    public static void validateEnum(String value, String[] allowedValues, String fieldName) {
        validateNotEmpty(value, fieldName);
        for (String allowedValue : allowedValues) {
            if (allowedValue.equals(value)) {
                return;
            }
        }
        throw new ValidationException(fieldName, 
            String.format("值必须为以下之一: %s", String.join(", ", allowedValues)));
    }

    /**
     * 校验分页参数
     */
    public static void validatePageParams(Integer pageNum, Integer pageSize) {
        if (pageNum != null && pageNum < 1) {
            throw new ValidationException("pageNum", "页码必须大于0");
        }
        if (pageSize != null && (pageSize < 1 || pageSize > 1000)) {
            throw new ValidationException("pageSize", "页面大小必须在1-1000之间");
        }
    }

    /**
     * 记录校验日志
     */
    public static void logValidation(String operation, String entity, String result) {
        log.debug("数据校验 - 操作: {}, 实体: {}, 结果: {}", operation, entity, result);
    }
}