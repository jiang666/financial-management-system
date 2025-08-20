package com.company.financial.common.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponsePageDataEntity<T> {
    
    private List<T> content;
    private long totalElements;
    private int totalPages;
    private int size;
    private int number;
    private int numberOfElements;
    private boolean first;
    private boolean last;
    
    // 兼容旧字段
    public Long getTotal() {
        return totalElements;
    }
    
    public List<T> getRows() {
        return content;
    }
    
    public Integer getPageNum() {
        return number;
    }
    
    public Integer getPageSize() {
        return size;
    }
}