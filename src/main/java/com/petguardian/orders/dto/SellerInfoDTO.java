package com.petguardian.orders.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * 賣家資訊 DTO
 * 包含賣家基本資訊、評分統計與評價列表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SellerInfoDTO {
    private Integer sellerId;
    private String sellerName;
    private Double averageRating;
    private Long reviewCount;
    private List<ReviewDisplayDTO> reviews;

    /**
     * 評價展示 DTO（內部類別）
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReviewDisplayDTO {
        private Integer reviewId;
        private Integer orderId;
        private Integer rating;
        private String reviewContent;
        private String reviewTime;
        private String buyerName;
    }
}
