// PagedResponse.java
package com.retrouvtout.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class PagedResponse<T> {
    
    private List<T> items;
    private int page;
    
    @JsonProperty("page_size")
    private int pageSize;
    
    @JsonProperty("total_elements")
    private long totalElements;
    
    @JsonProperty("total_pages")
    private int totalPages;
    
    @JsonProperty("is_last")
    private boolean isLast;
    
    // Constructeurs
    public PagedResponse() {}
    
    public PagedResponse(List<T> items, int page, int pageSize, long totalElements, int totalPages, boolean isLast) {
        this.items = items;
        this.page = page;
        this.pageSize = pageSize;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.isLast = isLast;
    }
    
    // Getters et Setters
    public List<T> getItems() { return items; }
    public void setItems(List<T> items) { this.items = items; }
    
    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }
    
    public int getPageSize() { return pageSize; }
    public void setPageSize(int pageSize) { this.pageSize = pageSize; }
    
    public long getTotalElements() { return totalElements; }
    public void setTotalElements(long totalElements) { this.totalElements = totalElements; }
    
    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
    
    public boolean isLast() { return isLast; }
    public void setLast(boolean last) { isLast = last; }
}
