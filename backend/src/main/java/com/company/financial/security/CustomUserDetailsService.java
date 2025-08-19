package com.company.financial.security;

import com.company.financial.entity.User;
import com.company.financial.repository.PermissionRepository;
import com.company.financial.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * 自定义用户详情服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    
    private final UserRepository userRepository;
    private final PermissionRepository permissionRepository;
    
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading user by username: {}", username);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + username));
        
        if (user.getStatus() == 0) {
            throw new UsernameNotFoundException("用户已被禁用: " + username);
        }
        
        // 获取用户的所有权限
        Set<String> permissions = permissionRepository.findPermissionCodesByUserId(user.getId());
        
        log.debug("User {} has {} permissions", username, permissions.size());
        
        return new CustomUserDetails(user, permissions);
    }
    
    /**
     * 根据用户ID加载用户
     */
    @Transactional(readOnly = true)
    public UserDetails loadUserById(String userId) {
        log.debug("Loading user by id: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + userId));
        
        if (user.getStatus() == 0) {
            throw new UsernameNotFoundException("用户已被禁用: " + userId);
        }
        
        Set<String> permissions = permissionRepository.findPermissionCodesByUserId(user.getId());
        
        return new CustomUserDetails(user, permissions);
    }
}