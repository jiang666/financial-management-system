package com.company.financial.dto.position;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Min;
import java.math.BigDecimal;

@Data
public class UpdatePositionDTO {
    @NotBlank(message = "岗位名称不能为空")
    private String name;
    
    @NotBlank(message = "岗位等级不能为空")
    private String level;
    
    @NotNull(message = "编制人数不能为空")
    @Min(value = 1, message = "编制人数必须大于0")
    private Integer count;
    
    @NotNull(message = "基础薪资不能为空")
    @Min(value = 0, message = "基础薪资不能为负数")
    private BigDecimal salary;
    
    private String status;
    
    private String description;
}