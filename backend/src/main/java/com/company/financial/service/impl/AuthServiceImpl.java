package com.company.financial.service.impl;

import com.company.financial.dto.auth.*;
import com.company.financial.entity.Role;
import com.company.financial.entity.User;
import com.company.financial.exception.BadRequestException;
import com.company.financial.exception.ResourceNotFoundException;
import com.company.financial.repository.PermissionRepository;
import com.company.financial.repository.RoleRepository;
import com.company.financial.repository.UserRepository;
import com.company.financial.security.CustomUserDetails;
import com.company.financial.security.jwt.JwtTokenProvider;
import com.company.financial.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 认证服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    @Transactional
    public LoginResponseDTO login(LoginRequestDTO loginRequest) {
        log.info("用户登录尝试: {}", loginRequest.getUsername());
        
        // 认证用户
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        // 生成令牌
        String accessToken = jwtTokenProvider.generateAccessToken(authentication);
        String refreshToken = jwtTokenProvider.generateRefreshToken(loginRequest.getUsername());
        
        // 更新最后登录时间
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
        
        // 获取用户权限
        Set<String> permissions = permissionRepository.findPermissionCodesByUserId(user.getId());
        
        // 构建响应
        return LoginResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(86400L) // 24小时
                .userInfo(convertToUserInfo(user))
                .permissions(permissions)
                .build();
    }
    
    @Override
    @Transactional
    public UserInfoDTO register(RegisterRequestDTO registerRequest) {
        log.info("用户注册: {}", registerRequest.getUsername());
        
        // 检查用户名是否存在
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new BadRequestException("用户名已存在");
        }
        
        // 检查邮箱是否存在
        if (registerRequest.getEmail() != null && 
            userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new BadRequestException("邮箱已被使用");
        }
        
        // 创建新用户
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setEmail(registerRequest.getEmail());
        user.setRealName(registerRequest.getRealName());
        user.setPhone(registerRequest.getPhone());
        user.setStatus(1); // 默认启用
        
        // 分配默认角色（查看者）
        Role viewerRole = roleRepository.findByCode("VIEWER")
                .orElseThrow(() -> new ResourceNotFoundException("默认角色不存在"));
        user.addRole(viewerRole);
        
        User savedUser = userRepository.save(user);
        
        return convertToUserInfo(savedUser);
    }
    
    @Override
    @Transactional(readOnly = true)
    public UserInfoDTO getCurrentUserInfo() {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
        
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
        
        return convertToUserInfo(user);
    }
    
    @Override
    @Transactional
    public LoginResponseDTO refreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new BadRequestException("无效的刷新令牌");
        }
        
        String username = jwtTokenProvider.getUsernameFromToken(refreshToken);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
        
        // 创建新的认证对象
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                username, null, null);
        
        // 生成新的访问令牌
        String newAccessToken = jwtTokenProvider.generateAccessToken(authentication);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(username);
        
        // 获取用户权限
        Set<String> permissions = permissionRepository.findPermissionCodesByUserId(user.getId());
        
        return LoginResponseDTO.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(86400L)
                .userInfo(convertToUserInfo(user))
                .permissions(permissions)
                .build();
    }
    
    @Override
    @Transactional
    public void changePassword(ChangePasswordDTO changePasswordDTO) {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
        
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
        
        // 验证原密码
        if (!passwordEncoder.matches(changePasswordDTO.getOldPassword(), user.getPassword())) {
            throw new BadRequestException("原密码错误");
        }
        
        // 验证新密码和确认密码
        if (!changePasswordDTO.getNewPassword().equals(changePasswordDTO.getConfirmPassword())) {
            throw new BadRequestException("新密码和确认密码不一致");
        }
        
        // 更新密码
        user.setPassword(passwordEncoder.encode(changePasswordDTO.getNewPassword()));
        userRepository.save(user);
        
        log.info("用户 {} 修改密码成功", user.getUsername());
    }
    
    @Override
    public void logout() {
        SecurityContextHolder.clearContext();
        log.info("用户登出成功");
    }
    
    /**
     * 转换为用户信息DTO
     */
    private UserInfoDTO convertToUserInfo(User user) {
        return UserInfoDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .realName(user.getRealName())
                .phone(user.getPhone())
                .departmentId(user.getDepartmentId())
                .status(user.getStatus())
                .roles(user.getRoles().stream()
                        .map(role -> UserInfoDTO.RoleDTO.builder()
                                .id(role.getId())
                                .code(role.getCode())
                                .name(role.getName())
                                .build())
                        .collect(Collectors.toSet()))
                .lastLoginAt(user.getLastLoginAt())
                .build();
    }
}