package com.company.financial.controller;

import com.company.financial.common.response.ResponseData;
import com.company.financial.dto.auth.*;
import com.company.financial.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * 认证控制器
 * 
 * @author System
 */
@RestController
@RequestMapping("/v1/auth")
@Slf4j
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    @Value("${jwt.header}")
    private String tokenHeader;
    
    @Value("${jwt.prefix}")
    private String tokenPrefix;
    
    /**
     * 用户登录
     * 
     * @param loginRequest 登录请求
     * @return 登录响应
     */
    @PostMapping("/login")
    public ResponseEntity<ResponseData<LoginResponseDTO>> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        try {
            LoginResponseDTO response = authService.login(loginRequest);
            return ResponseEntity.ok(ResponseData.success("登录成功", response));
        } catch (Exception e) {
            log.error("登录失败", e);
            return ResponseEntity.ok(ResponseData.error("登录失败: " + e.getMessage()));
        }
    }
    
    /**
     * 用户登出
     * 
     * @param request HTTP请求
     * @return 响应结果
     */
    @PostMapping("/logout")
    public ResponseEntity<ResponseData<Object>> logout(HttpServletRequest request) {
        try {
            String token = getJwtFromRequest(request);
            if (StringUtils.hasText(token)) {
                authService.logout(token);
            }
            return ResponseEntity.ok(ResponseData.success("登出成功"));
        } catch (Exception e) {
            log.error("登出失败", e);
            return ResponseEntity.ok(ResponseData.error("登出失败: " + e.getMessage()));
        }
    }
    
    /**
     * 刷新Token
     * 
     * @param request 刷新Token请求
     * @return 新的Token信息
     */
    @PostMapping("/refresh")
    public ResponseEntity<ResponseData<LoginResponseDTO>> refreshToken(@RequestBody RefreshTokenRequest request) {
        try {
            LoginResponseDTO response = authService.refreshToken(request.getRefreshToken());
            return ResponseEntity.ok(ResponseData.success("Token刷新成功", response));
        } catch (Exception e) {
            log.error("Token刷新失败", e);
            return ResponseEntity.ok(ResponseData.error("Token刷新失败: " + e.getMessage()));
        }
    }
    
    /**
     * 修改密码
     * 
     * @param changePasswordDTO 修改密码请求
     * @return 响应结果
     */
    @PutMapping("/password")
    public ResponseEntity<ResponseData<Object>> changePassword(@Valid @RequestBody ChangePasswordDTO changePasswordDTO) {
        try {
            authService.changePassword(changePasswordDTO);
            return ResponseEntity.ok(ResponseData.success("密码修改成功"));
        } catch (Exception e) {
            log.error("修改密码失败", e);
            return ResponseEntity.ok(ResponseData.error("修改密码失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取当前用户信息
     * 
     * @return 用户信息
     */
    @GetMapping("/me")
    public ResponseEntity<ResponseData<UserInfoDTO>> getCurrentUser() {
        try {
            UserInfoDTO userInfo = authService.getCurrentUserInfo();
            return ResponseEntity.ok(ResponseData.success(userInfo));
        } catch (Exception e) {
            log.error("获取用户信息失败", e);
            return ResponseEntity.ok(ResponseData.error("获取用户信息失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取当前用户权限资源
     * 
     * @return 权限资源树
     */
    @GetMapping("/resources")
    public ResponseEntity<ResponseData<ResourceDTO>> getCurrentUserResources() {
        try {
            ResourceDTO resources = authService.getCurrentUserResources();
            return ResponseEntity.ok(ResponseData.success(resources));
        } catch (Exception e) {
            log.error("获取用户权限资源失败", e);
            return ResponseEntity.ok(ResponseData.error("获取权限资源失败: " + e.getMessage()));
        }
    }
    
    /**
     * 验证Token是否有效
     * 
     * @param request HTTP请求
     * @return 验证结果
     */
    @GetMapping("/validate")
    public ResponseEntity<ResponseData<Object>> validateToken(HttpServletRequest request) {
        try {
            String token = getJwtFromRequest(request);
            if (StringUtils.hasText(token)) {
                return ResponseEntity.ok(ResponseData.success("Token有效"));
            } else {
                return ResponseEntity.ok(ResponseData.error("Token无效"));
            }
        } catch (Exception e) {
            log.error("Token验证失败", e);
            return ResponseEntity.ok(ResponseData.error("Token验证失败"));
        }
    }
    
    /**
     * 从请求中提取JWT Token
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(tokenHeader);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(tokenPrefix + " ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
    
    /**
     * 刷新Token请求DTO
     */
    public static class RefreshTokenRequest {
        private String refreshToken;
        
        public String getRefreshToken() {
            return refreshToken;
        }
        
        public void setRefreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
        }
    }
}