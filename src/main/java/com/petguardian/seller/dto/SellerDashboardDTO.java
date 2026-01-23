package com.petguardian.seller.dto;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;
import java.util.Map;

/**
 * 賣家管理中心 Dashboard DTO
 * 整合賣家個人資訊與營運統計數據
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SellerDashboardDTO {

    // ==================== 賣家個人資訊 ====================
    private Integer memId;          // 會員ID
    private String memName;         // 會員名稱
    private String memImage;        // 會員頭像 (Base64 或 URL)

    // ==================== 營運概況統計 ====================
    private Long totalProducts;     // 總商品數
    private Long activeProducts;    // 上架中商品數
    private Long totalOrders;       // 總訂單數
    private Long pendingShipment;   // 待出貨訂單數 (已付款狀態)

    // ==================== 評價統計 ====================
    private Double averageRating;   // 平均評分 (總星星數 / 總評論數)
    private Integer totalRatingCount; // 總評價數量 (mem_shop_rating_count)
    private Integer totalRatingScore; // 總星星數 (mem_shop_rating_score)

    // ==================== 財務統計 ====================
    private Integer walletBalance;  // 錢包餘額
    private Integer totalRevenue;   // 總營收 (累計訂單金額)

    // ==================== 評價列表 ====================
    private List<Map<String, Object>> allReviews; // 所有評價 (含訂單資訊)
}