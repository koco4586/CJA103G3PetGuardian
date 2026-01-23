package com.petguardian.orders.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 商品展示 DTO
 * 用於商城頁面、結帳頁面的商品顯示
 * 包含 Base64 圖片以避免 API 請求閃爍
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDisplayDTO {
    private Integer proId;
    private Integer sellerId;
    private String proName;
    private Integer proPrice;
    private Integer stockQuantity;
    private String proDescription;
    private String imageBase64;
    private boolean favorited;
    private Integer proTypeId;
    private String proTypeName;
}
