package com.petguardian.seller.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品 DTO
 * 用於前端展示，包含商品基本資訊與圖片列表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDTO {

    private Integer proId;              // 商品ID
    private Integer proTypeId;          // 商品類別ID
    private String proTypeName;         // 商品類別名稱
    private Integer memId;              // 賣家會員ID
    private String sellerName;          // 賣家名稱
    private String proName;             // 商品名稱
    private Integer proPrice;           // 商品價格
    private String proDescription;      // 商品描述
    private Integer stockQuantity;      // 庫存數量
    private Integer proState;           // 商品狀態 (0=已售完, 1=上架中)
    private String proStateText;        // 商品狀態文字
    private LocalDateTime launchedTime; // 上架時間
    private LocalDateTime soldTime;     // 售出時間

    // 商品圖片列表
    private List<ProductPicDTO> productPics;

    // 第一張圖片 URL（用於列表顯示）
    private String mainImageUrl;
}