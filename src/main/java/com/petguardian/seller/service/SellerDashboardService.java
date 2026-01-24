package com.petguardian.seller.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

/**
 * 賣家管理中心 Service Interface
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

    // ==================== 商品圖片管理 ====================

    /**
     * 取得商品的所有圖片（Base64 格式）
     * 回傳 List<Map>，每個 Map 包含：productPicId, imageBase64
     */
    List<Map<String, Object>> getProductImages(Integer proId);

    /**
     * 取得商品的主圖（第一張圖）Base64
     * 若無圖片則回傳預設圖
     */
    String getProductMainImage(Integer proId);

    /**
     * 儲存商品圖片（可多張）
     */
    void saveProductImages(Integer proId, List<MultipartFile> images);

    /**
     * 刪除指定的商品圖片
     */
    void deleteProductImage(Integer productPicId);

    // ==================== 訂單操作 ====================

    /**
     * 賣家出貨（將訂單狀態從 0 改為 1）
     * 回傳 true 表示成功，false 表示失敗
     */
    boolean shipOrder(Integer sellerId, Integer orderId);

    /**
     * 賣家取消訂單並退款給買家
     * 回傳退款金額，失敗回傳 null
     */
    Integer cancelOrderWithRefund(Integer sellerId, Integer orderId);

    // ==================== 評價查詢 ====================

    /**
     * 取得賣家的所有評價（含買家名稱、訂單資訊）
     * 回傳 List<Map>，每個 Map 包含：
     * orderId, buyerName, rating, reviewContent, reviewTime
     */
    List<Map<String, Object>> getSellerReviews(Integer sellerId);
}