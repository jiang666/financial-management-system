package com.company.financial.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * JWT配置属性
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    
    /**
     * JWT密钥
     */
    private String secret = "defaultSecretKey";
    
    /**
     * 访问令牌过期时间（秒）
     */
    private Long expiration = 86400L;
    
    /**
     * 刷新令牌过期时间（秒）
     */
    private Long refreshExpiration = 604800L;
    
    /**
     * 请求头名称
     */
    private String header = "Authorization";
    
    /**
     * 令牌前缀
     */
    private String prefix = "Bearer";
    
    /**
     * 签发者
     */
    private String issuer = "financial-management-system";
}