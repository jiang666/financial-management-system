package com.company.financial.common;

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
    
    // 兼容旧方法
    public void setTotal(long total) {
        this.totalElements = total;
    }
    
    public void setRows(List<T> rows) {
        this.content = rows;
    }
    
    public List<T> getData() {
        return content;
    }
    
    public ResponsePageDataEntity(List<T> content, long totalElements, int totalPages) {
        this.content = content;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
    }
    
    public static <T> ResponsePageDataEntity<T> success(org.springframework.data.domain.Page<T> page) {
        ResponsePageDataEntity<T> response = new ResponsePageDataEntity<>();
        response.setContent(page.getContent());
        response.setTotalElements(page.getTotalElements());
        response.setTotalPages(page.getTotalPages());
        response.setSize(page.getSize());
        response.setNumber(page.getNumber());
        response.setNumberOfElements(page.getNumberOfElements());
        response.setFirst(page.isFirst());
        response.setLast(page.isLast());
        return response;
    }
}