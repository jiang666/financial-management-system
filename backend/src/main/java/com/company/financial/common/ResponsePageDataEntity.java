package com.company.financial.common;

import lombok.Data;
import java.util.List;

@Data
public class ResponsePageDataEntity<T> {
    
    private Long total;
    private List<T> rows;
    private Integer pageNum;
    private Integer pageSize;
    
    public ResponsePageDataEntity() {
    }
    
    public ResponsePageDataEntity(Long total, List<T> rows) {
        this.total = total;
        this.rows = rows;
    }
    
    public ResponsePageDataEntity(Long total, List<T> rows, Integer pageNum, Integer pageSize) {
        this.total = total;
        this.rows = rows;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
    }
}