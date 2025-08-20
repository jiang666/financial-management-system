package com.company.financial.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * 用户创建DTO
 */
@Data
@Schema(description = "用户创建请求")
public class UserCreateDTO {
    
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50个字符之间")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
    @Schema(description = "用户名", example = "john_doe")
    private String username;
    
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 32, message = "密码长度必须在6-32个字符之间")
    @Schema(description = "密码", example = "123456")
    private String password;
    
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
    private Integer status = 1;
    
    @Schema(description = "角色ID列表")
    private List<String> roleIds;
}