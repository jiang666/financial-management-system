package com.company.financial.controller;

import com.company.financial.common.ResponseEntity;
import com.company.financial.dto.auth.*;
import com.company.financial.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 认证控制器
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "认证管理", description = "用户认证相关接口")
@Validated
public class AuthController {
    
    private final AuthService authService;
    
    /**
     * 用户登录
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "使用用户名和密码登录系统")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        log.info("用户登录请求: {}", loginRequest.getUsername());
        LoginResponseDTO response = authService.login(loginRequest);
        return ResponseEntity.success(response);
    }
    
    /**
     * 用户注册
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "新用户注册")
    public ResponseEntity<UserInfoDTO> register(@Valid @RequestBody RegisterRequestDTO registerRequest) {
        log.info("用户注册请求: {}", registerRequest.getUsername());
        UserInfoDTO userInfo = authService.register(registerRequest);
        return ResponseEntity.success(userInfo);
    }
    
    /**
     * 获取当前用户信息
     */
    @GetMapping("/me")
    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细信息")
    public ResponseEntity<UserInfoDTO> getCurrentUser() {
        UserInfoDTO userInfo = authService.getCurrentUserInfo();
        return ResponseEntity.success(userInfo);
    }
    
    /**
     * 刷新令牌
     */
    @PostMapping("/refresh")
    @Operation(summary = "刷新令牌", description = "使用刷新令牌获取新的访问令牌")
    public ResponseEntity<LoginResponseDTO> refreshToken(@RequestParam String refreshToken) {
        LoginResponseDTO response = authService.refreshToken(refreshToken);
        return ResponseEntity.success(response);
    }
    
    /**
     * 修改密码
     */
    @PostMapping("/change-password")
    @Operation(summary = "修改密码", description = "修改当前用户的密码")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordDTO changePasswordDTO) {
        authService.changePassword(changePasswordDTO);
        return ResponseEntity.success();
    }
    
    /**
     * 用户登出
     */
    @PostMapping("/logout")
    @Operation(summary = "用户登出", description = "退出登录")
    public ResponseEntity<Void> logout() {
        authService.logout();
        return ResponseEntity.success();
    }
}