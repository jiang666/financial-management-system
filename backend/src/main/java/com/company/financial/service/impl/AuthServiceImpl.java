package com.company.financial.service.impl;

import com.company.financial.dto.auth.*;
import com.company.financial.entity.Resource;
import com.company.financial.entity.Role;
import com.company.financial.entity.User;
import com.company.financial.repository.ResourceRepository;
import com.company.financial.repository.RoleRepository;
import com.company.financial.repository.UserRepository;
import com.company.financial.security.CustomUserDetails;
import com.company.financial.security.jwt.JwtTokenProvider;
import com.company.financial.service.AuthService;
import com.company.financial.util.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 认证服务实现
 * 
 * @author System
 */
@Service
@Slf4j
public class AuthServiceImpl implements AuthService {
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private JwtTokenProvider tokenProvider;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private ResourceRepository resourceRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Value("${jwt.expiration}")
    private int jwtExpiration;
    
    @Override
    @Transactional
    public LoginResponseDTO login(LoginRequestDTO loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );
        
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        
        // 生成Token
        String token = tokenProvider.generateToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken(userDetails.getUsername());
        
        // 更新最后登录时间
        updateLastLoginTime(userDetails.getUserId());
        
        // 获取用户信息
        UserInfoDTO userInfo = getUserInfo(userDetails.getUserId());
        
        // 获取用户权限
        List<String> authorities = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        
        // 获取用户资源权限
        List<ResourceDTO> resources = getUserResources(userDetails.getUserId());
        
        LoginResponseDTO response = new LoginResponseDTO();
        response.setToken(token);
        response.setRefreshToken(refreshToken);
        response.setExpiresIn((long) jwtExpiration);
        response.setUserInfo(userInfo);
        response.setAuthorities(authorities);
        response.setResources(resources);
        
        log.info("User {} logged in successfully", loginRequest.getUsername());
        return response;
    }
    
    @Override
    public void logout(String token) {
        // TODO: 实现Token黑名单机制
        String username = tokenProvider.getUsernameFromToken(token);
        log.info("User {} logged out", username);
    }
    
    @Override
    public LoginResponseDTO refreshToken(String refreshToken) {
        if (!tokenProvider.validateToken(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }
        
        String username = tokenProvider.getUsernameFromToken(refreshToken);
        User user = userRepository.findByUsernameAndDeleted(username, 0)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // 重新生成Token
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                username, null, null);
        String newToken = tokenProvider.generateToken(authentication);
        String newRefreshToken = tokenProvider.generateRefreshToken(username);
        
        LoginResponseDTO response = new LoginResponseDTO();
        response.setToken(newToken);
        response.setRefreshToken(newRefreshToken);
        response.setExpiresIn((long) jwtExpiration);
        
        return response;
    }
    
    @Override
    @Transactional
    public void changePassword(ChangePasswordDTO changePasswordDTO) {
        String currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            throw new RuntimeException("User not authenticated");
        }
        
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // 验证原密码
        if (!passwordEncoder.matches(changePasswordDTO.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("原密码不正确");
        }
        
        // 验证新密码和确认密码
        if (!changePasswordDTO.getNewPassword().equals(changePasswordDTO.getConfirmPassword())) {
            throw new RuntimeException("新密码和确认密码不一致");
        }
        
        // 更新密码
        user.setPassword(passwordEncoder.encode(changePasswordDTO.getNewPassword()));
        user.setUpdateTime(System.currentTimeMillis());
        user.setUpdateBy(currentUserId);
        userRepository.save(user);
        
        log.info("User {} changed password", user.getUsername());
    }
    
    @Override
    public UserInfoDTO getCurrentUserInfo() {
        String currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            throw new RuntimeException("User not authenticated");
        }
        
        return getUserInfo(currentUserId);
    }
    
    @Override
    public ResourceDTO getCurrentUserResources() {
        String currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            throw new RuntimeException("User not authenticated");
        }
        
        List<ResourceDTO> resources = getUserResources(currentUserId);
        return buildResourceTree(resources);
    }
    
    /**
     * 更新最后登录时间
     */
    private void updateLastLoginTime(String userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            user.setLastLoginTime(System.currentTimeMillis());
            userRepository.save(user);
        }
    }
    
    /**
     * 获取用户信息
     */
    private UserInfoDTO getUserInfo(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        UserInfoDTO userInfo = new UserInfoDTO();
        BeanUtils.copyProperties(user, userInfo);
        return userInfo;
    }
    
    /**
     * 获取用户资源权限
     */
    private List<ResourceDTO> getUserResources(String userId) {
        List<Resource> resources = resourceRepository.findResourcesByUserId(userId);
        
        return resources.stream().map(resource -> {
            ResourceDTO dto = new ResourceDTO();
            BeanUtils.copyProperties(resource, dto);
            return dto;
        }).collect(Collectors.toList());
    }
    
    /**
     * 构建资源树
     */
    private ResourceDTO buildResourceTree(List<ResourceDTO> resources) {
        if (resources.isEmpty()) {
            return null;
        }
        
        Map<String, ResourceDTO> resourceMap = resources.stream()
                .collect(Collectors.toMap(ResourceDTO::getId, r -> r));
        
        ResourceDTO root = null;
        
        for (ResourceDTO resource : resources) {
            if (resource.getParentId() == null || "0".equals(resource.getParentId())) {
                root = resource;
            } else {
                ResourceDTO parent = resourceMap.get(resource.getParentId());
                if (parent != null) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new ArrayList<>());
                    }
                    parent.getChildren().add(resource);
                }
            }
        }
        
        return root;
    }
}