package com.retrouvtout.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Réponse paginée EXACTEMENT conforme au frontend ListingsResponse
 */
public class PagedResponse<T> {
    
    private List<T> items;
    private long total;
    private int page;
    
    @JsonProperty("totalPages")
    private int totalPages;
    
    // Constructeurs
    public PagedResponse() {}
    
    public PagedResponse(List<T> items, long total, int page, int totalPages) {
        this.items = items;
        this.total = total;
        this.page = page;
        this.totalPages = totalPages;
    }
    
    // Getters et Setters
    public List<T> getItems() { return items; }
    public void setItems(List<T> items) { this.items = items; }
    
    public long getTotal() { return total; }
    public void setTotal(long total) { this.total = total; }
    
    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }
    
    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
}