package com.company.financial.common.response;

import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 分页响应格式
 * 
 * @author System
 */
@Data
public class ResponsePageData<T> {
    
    private Integer code;
    
    private String message;
    
    private PageData<T> data;
    
    private Long timestamp;
    
    public ResponsePageData() {
        this.timestamp = System.currentTimeMillis();
    }
    
    public ResponsePageData(Integer code, String message, PageData<T> data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }
    
    public static <T> ResponsePageData<T> success(Page<T> page) {
        PageData<T> pageData = new PageData<>();
        pageData.setContent(page.getContent());
        pageData.setTotalElements(page.getTotalElements());
        pageData.setTotalPages(page.getTotalPages());
        pageData.setSize(page.getSize());
        pageData.setNumber(page.getNumber());
        pageData.setFirst(page.isFirst());
        pageData.setLast(page.isLast());
        
        return new ResponsePageData<>(200, "查询成功", pageData);
    }
    
    public static <T> ResponsePageData<T> success(String message, Page<T> page) {
        PageData<T> pageData = new PageData<>();
        pageData.setContent(page.getContent());
        pageData.setTotalElements(page.getTotalElements());
        pageData.setTotalPages(page.getTotalPages());
        pageData.setSize(page.getSize());
        pageData.setNumber(page.getNumber());
        pageData.setFirst(page.isFirst());
        pageData.setLast(page.isLast());
        
        return new ResponsePageData<>(200, message, pageData);
    }
    
    @Data
    public static class PageData<T> {
        private List<T> content;
        private Long totalElements;
        private Integer totalPages;
        private Integer size;
        private Integer number;
        private Boolean first;
        private Boolean last;
    }
}