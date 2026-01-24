package com.petguardian.seller.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * 訂單項目 DTO
 * 用於顯示訂單中的商品明細
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemDTO {

    private Integer orderItemId;    // 訂單項目ID
    private Integer orderId;        // 訂單ID
    private Integer proId;          // 商品ID
    private String proName;         // 商品名稱
    private Integer quantity;       // 數量
    private Integer proPrice;       // 單價
    private Integer subtotal;       // 小計 (數量 * 單價)
    private String imageBase64;     // 商品圖片 Base64
}