package com.company.financial.security;

import com.company.financial.entity.Role;
import com.company.financial.entity.User;
import com.company.financial.repository.RoleRepository;
import com.company.financial.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 自定义用户详情服务
 * 
 * @author System
 */
@Service
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameAndDeleted(username, 0)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        
        if (!"ACTIVE".equals(user.getStatus())) {
            throw new UsernameNotFoundException("User is not active: " + username);
        }
        
        Collection<GrantedAuthority> authorities = getUserAuthorities(user.getId());
        
        return new CustomUserDetails(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                "ACTIVE".equals(user.getStatus()),
                authorities
        );
    }
    
    /**
     * 获取用户权限
     */
    private Collection<GrantedAuthority> getUserAuthorities(String userId) {
        List<Role> roles = roleRepository.findRolesByUserId(userId);
        
        return roles.stream()
                .filter(role -> "ACTIVE".equals(role.getStatus()))
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getRoleCode()))
                .collect(Collectors.toList());
    }
    
    /**
     * 根据用户ID加载用户详情
     */
    @Transactional(readOnly = true)
    public UserDetails loadUserById(String userId) {
        User user = userRepository.findByIdAndDeleted(userId, 0)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + userId));
        
        if (!"ACTIVE".equals(user.getStatus())) {
            throw new UsernameNotFoundException("User is not active with id: " + userId);
        }
        
        Collection<GrantedAuthority> authorities = getUserAuthorities(user.getId());
        
        return new CustomUserDetails(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                "ACTIVE".equals(user.getStatus()),
                authorities
        );
    }
}