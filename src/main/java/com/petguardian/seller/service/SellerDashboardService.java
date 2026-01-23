package com.petguardian.seller.service;

import java.util.List;
import java.util.Map;

/**
 * 賣家管理中心 - 營運概況 Service Interface
 *
 * 負責：賣家資訊、評價統計
 */
public interface SellerDashboardService {

    // ==================== 賣家資訊 ====================

    /**
     * 取得賣家基本資訊（memId, memName, memImage）
     */
    Map<String, Object> getSellerBasicInfo(Integer memId);

    /**
     * 取得賣家評價統計（平均分、總評價數）
     * 回傳 Map 包含：averageRating, totalRatingCount
     */
    Map<String, Object> getSellerRatingStats(Integer memId);

    /**
     * 取得賣家的所有評價（含買家名稱、訂單資訊）
     * 回傳 List<Map>，每個 Map 包含：
     * orderId, buyerName, rating, reviewContent, reviewTime
     */
    List<Map<String, Object>> getSellerReviews(Integer sellerId);

    // ==================== 整合查詢（給 Dashboard 用） ====================

    /**
     * 取得營運概況所有資料
     * 回傳 Map 包含：
     * - sellerInfo: 賣家基本資訊
     * - totalProducts: 商品總數
     * - activeProducts: 上架中商品數
     * - totalOrders: 訂單總數
     * - pendingShipment: 待出貨數量
     * - averageRating: 平均評分
     * - totalRatingCount: 總評價數量
     * - totalRevenue: 總營收
     * - allReviews: 評價列表
     */
    Map<String, Object> getDashboardData(Integer sellerId);
}