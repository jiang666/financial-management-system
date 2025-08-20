package com.company.financial.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.company.financial.common.ResponseEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * JWT访问拒绝处理器
 * 处理已认证但无权限的请求
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAccessDeniedHandler implements AccessDeniedHandler {
    
    private final ObjectMapper objectMapper;
    
    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {
        log.error("Responding with access denied error. Message - {}", accessDeniedException.getMessage());
        
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        
        ResponseEntity<Object> errorResponse = ResponseEntity.error(
                HttpStatus.FORBIDDEN.value(),
                "访问被拒绝：没有权限访问该资源"
        );
        
        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }
}