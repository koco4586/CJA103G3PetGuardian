package com.petguardian.sitter.model;

import java.util.List;

/**
 * 保姆搜尋結果 DTO
 * 用於前端顯示保姆列表及篩選結果
 */
public class SitterSearchDTO {
    
    // 保姆基本資訊
    private Integer sitterId;
    private String sitterName;
    private String sitterAdd;
    
    // 評價資訊
    private Integer ratingCount;
    private Integer starCount;
    private Double averageRating; // 平均評分（計算後的值）
    
    // 服務資訊
    private List<String> serviceNames; // 服務項目名稱列表（如：遛狗、洗澡）
    private List<String> petTypes;     // 服務寵物類型列表（如：狗、貓）
    
    // 地區資訊
    private List<String> serviceAreas; // 服務地區列表（如：台北市大安區）
    
    // 價格資訊
    private Integer minPrice; // 該保姆的最低服務價格
    private Integer maxPrice; // 該保姆的最高服務價格
    
    // 建構子
    public SitterSearchDTO() {
    }
    
    // Getter 和 Setter
    public Integer getSitterId() {
        return sitterId;
    }
    
    public void setSitterId(Integer sitterId) {
        this.sitterId = sitterId;
    }
    
    public String getSitterName() {
        return sitterName;
    }
    
    public void setSitterName(String sitterName) {
        this.sitterName = sitterName;
    }
    
    public String getSitterAdd() {
        return sitterAdd;
    }
    
    public void setSitterAdd(String sitterAdd) {
        this.sitterAdd = sitterAdd;
    }
    
    public Integer getRatingCount() {
        return ratingCount;
    }
    
    public void setRatingCount(Integer ratingCount) {
        this.ratingCount = ratingCount;
    }
    
    public Integer getStarCount() {
        return starCount;
    }
    
    public void setStarCount(Integer starCount) {
        this.starCount = starCount;
    }
    
    public Double getAverageRating() {
        return averageRating;
    }
    
    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }
    
    public List<String> getServiceNames() {
        return serviceNames;
    }
    
    public void setServiceNames(List<String> serviceNames) {
        this.serviceNames = serviceNames;
    }
    
    public List<String> getPetTypes() {
        return petTypes;
    }
    
    public void setPetTypes(List<String> petTypes) {
        this.petTypes = petTypes;
    }
    
    public List<String> getServiceAreas() {
        return serviceAreas;
    }
    
    public void setServiceAreas(List<String> serviceAreas) {
        this.serviceAreas = serviceAreas;
    }
    
    public Integer getMinPrice() {
        return minPrice;
    }
    
    public void setMinPrice(Integer minPrice) {
        this.minPrice = minPrice;
    }
    
    public Integer getMaxPrice() {
        return maxPrice;
    }
    
    public void setMaxPrice(Integer maxPrice) {
        this.maxPrice = maxPrice;
    }
}
