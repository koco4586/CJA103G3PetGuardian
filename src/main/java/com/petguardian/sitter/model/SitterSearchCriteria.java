package com.petguardian.sitter.model;

import java.util.List;

/**
 * 保姆搜尋條件 DTO
 * 用於接收前端的篩選參數
 */
public class SitterSearchCriteria {

    // 地區篩選（可選多個區域 ID）
    private List<Integer> areaIds;

    // 服務項目篩選（可選多個服務項目 ID）
    private List<Integer> serviceItemIds;

    // 寵物類型篩選（可選多個寵物類型 ID）
    private List<Integer> petTypeIds;

    // 價格範圍篩選
    private Integer minPrice;
    private Integer maxPrice;

    // 排序選項（可選：price_asc, price_desc, rating_desc）
    private String sortBy;

    // 分頁參數
    private Integer page; // 頁碼（從 0 開始）
    private Integer pageSize; // 每頁筆數

    // 地區篩選（使用名稱）
    private String cityName; // 縣市名稱（例如："台北市"）
    private String district; // 區域名稱（例如："大安區"），可為 null

    // 建構子
    public SitterSearchCriteria() {
        // 設定預設值
        this.page = 0;
        this.pageSize = 10;
        this.sortBy = "rating_desc"; // 預設按評分降序
    }

    // Getter 和 Setter
    public List<Integer> getAreaIds() {
        return areaIds;
    }

    public void setAreaIds(List<Integer> areaIds) {
        this.areaIds = areaIds;
    }

    public List<Integer> getServiceItemIds() {
        return serviceItemIds;
    }

    public void setServiceItemIds(List<Integer> serviceItemIds) {
        this.serviceItemIds = serviceItemIds;
    }

    public List<Integer> getPetTypeIds() {
        return petTypeIds;
    }

    public void setPetTypeIds(List<Integer> petTypeIds) {
        this.petTypeIds = petTypeIds;
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

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    // 輔助方法：檢查是否有任何篩選條件
    public boolean hasFilters() {
        return (areaIds != null && !areaIds.isEmpty()) ||
                (serviceItemIds != null && !serviceItemIds.isEmpty()) ||
                (petTypeIds != null && !petTypeIds.isEmpty()) ||
                minPrice != null ||
                maxPrice != null ||
                (cityName != null && !cityName.trim().isEmpty()) ||
                (district != null && !district.trim().isEmpty());
    }
}
