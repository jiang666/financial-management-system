package com.company.financial.dto.user;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * 用户创建DTO
 * 
 * @author System
 */
@Data
public class UserCreateDTO {
    
    @NotBlank(message = "用户名不能为空")
    @Size(max = 50, message = "用户名长度不能超过50个字符")
    private String username;
    
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20个字符之间")
    private String password;
    
    @Size(max = 50, message = "真实姓名长度不能超过50个字符")
    private String realName;
    
    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过100个字符")
    private String email;
    
    @Size(max = 20, message = "手机号长度不能超过20个字符")
    private String phone;
    
    @Size(max = 100, message = "部门名称长度不能超过100个字符")
    private String department;
    
    private String departmentId;
    
    @Size(max = 100, message = "职位长度不能超过100个字符")
    private String position;
    
    private String status;
    
    private List<String> roleIds;
}