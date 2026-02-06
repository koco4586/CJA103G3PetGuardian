package com.petguardian.seller.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * 商品圖片 DTO
 * 用於前端展示與圖片管理
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductPicDTO {

    private Integer productPicId;   // 圖片ID
    private Integer proId;          // 商品ID
    private String imageUrl;        // 圖片 URL 字串
}