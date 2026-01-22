package com.petguardian.sellerreview.service;

import com.petguardian.sellerreview.model.SellerReviewVO;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface SellerReviewService {

    /**
     * 新增評價
     */
    SellerReviewVO createReview(Integer orderId, Integer rating, String reviewContent);

    /**
     * 根據評價ID查詢評價
     */
    Optional<SellerReviewVO> getReviewById(Integer reviewId);

    /**
     * 根據訂單ID查詢評價
     */
    Optional<SellerReviewVO> getReviewByOrderId(Integer orderId);

    /**
     * 查詢買家的所有評價
     */
    List<SellerReviewVO> getReviewsByBuyerId(Integer buyerMemId);

    /**
     * 查詢賣家的所有評價（僅顯示）
     */
    List<SellerReviewVO> getReviewsBySellerMemId(Integer sellerMemId);

    /**
     * 查詢所有評價
     */
    List<SellerReviewVO> getAllReviews();

    /**
     * 更新評價顯示狀態
     */
    SellerReviewVO updateShowStatus(Integer reviewId, Integer showStatus);

    /**
     * 獲取賣家評分統計
     */
    Map<String, Object> getSellerRatingStats(Integer sellerMemId);

    /**
     * 檢查訂單是否已評價
     */
    boolean hasReviewed(Integer orderId);
}
