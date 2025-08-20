package com.company.financial.dto.user;

import com.company.financial.dto.auth.RoleDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户详细信息DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户详细信息")
public class UserDetailDTO {
    
    @Schema(description = "用户ID")
    private String id;
    
    @Schema(description = "用户名")
    private String username;
    
    @Schema(description = "邮箱")
    private String email;
    
    @Schema(description = "真实姓名")
    private String realName;
    
    @Schema(description = "手机号")
    private String phone;
    
    @Schema(description = "部门ID")
    private String departmentId;
    
    @Schema(description = "用户状态：1-启用 0-禁用")
    private Integer status;
    
    @Schema(description = "最后登录时间")
    private LocalDateTime lastLoginAt;
    
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
    
    @Schema(description = "创建人")
    private String createdBy;
    
    @Schema(description = "更新人")
    private String updatedBy;
    
    @Schema(description = "版本号")
    private Integer version;
    
    @Schema(description = "用户角色列表")
    private List<RoleDTO> roles;
}