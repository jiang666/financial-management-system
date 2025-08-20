package com.company.financial.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * 用户更新DTO
 */
@Data
@Schema(description = "用户更新请求")
public class UserUpdateDTO {
    
    @Email(message = "邮箱格式不正确")
    @Schema(description = "邮箱", example = "john.doe@company.com")
    private String email;
    
    @Size(max = 50, message = "真实姓名长度不能超过50个字符")
    @Schema(description = "真实姓名", example = "张三")
    private String realName;
    
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "手机号", example = "13800138000")
    private String phone;
    
    @Schema(description = "部门ID")
    private String departmentId;
    
    @Schema(description = "用户状态：1-启用 0-禁用", example = "1")
    private Integer status;
    
    @Schema(description = "角色ID列表")
    private List<String> roleIds;
    
    @Schema(description = "版本号，用于乐观锁", example = "0")
    private Integer version;
}