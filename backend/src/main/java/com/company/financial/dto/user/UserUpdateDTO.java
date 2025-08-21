package com.company.financial.dto.user;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * 用户更新DTO
 * 
 * @author System
 */
@Data
public class UserUpdateDTO {
    
    private String id;
    
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
    
    @NotBlank(message = "状态不能为空")
    private String status;
    
    private List<String> roleIds;
}