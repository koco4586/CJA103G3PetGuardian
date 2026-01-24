package com.petguardian.sellerreview.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SellerReviewRepository extends JpaRepository<SellerReviewVO, Integer> {

    // 根據訂單ID查詢評價
    Optional<SellerReviewVO> findByOrderId(Integer orderId);

    // 檢查訂單是否已有評價
    boolean existsByOrderId(Integer orderId);

    // 查詢所有評價（依評價時間降序）
    List<SellerReviewVO> findAllByOrderByReviewTimeDesc();

    // 查詢指定顯示狀態的評價
    List<SellerReviewVO> findByShowStatusOrderByReviewTimeDesc(Integer showStatus);

    // 根據賣家ID查詢評價（透過 JOIN Orders）
    @Query("SELECT sr FROM SellerReviewVO sr JOIN OrdersVO o ON sr.orderId = o.orderId WHERE o.sellerMemId = :sellerMemId AND sr.showStatus = :showStatus ORDER BY sr.reviewTime DESC")
    List<SellerReviewVO> findBySellerMemIdAndShowStatus(@Param("sellerMemId") Integer sellerMemId, @Param("showStatus") Integer showStatus);

    // 根據買家ID查詢評價（透過 JOIN Orders）
    @Query("SELECT sr FROM SellerReviewVO sr JOIN OrdersVO o ON sr.orderId = o.orderId WHERE o.buyerMemId = :buyerMemId ORDER BY sr.reviewTime DESC")
    List<SellerReviewVO> findByBuyerMemId(@Param("buyerMemId") Integer buyerMemId);

    // 計算賣家的平均評分
    @Query("SELECT AVG(sr.rating) FROM SellerReviewVO sr JOIN OrdersVO o ON sr.orderId = o.orderId WHERE o.sellerMemId = :sellerMemId AND sr.showStatus = 0")
    Double calculateAverageRatingBySellerMemId(@Param("sellerMemId") Integer sellerMemId);

    // 統計賣家的評價數量
    @Query("SELECT COUNT(sr) FROM SellerReviewVO sr JOIN OrdersVO o ON sr.orderId = o.orderId WHERE o.sellerMemId = :sellerMemId AND sr.showStatus = 0")
    Long countBySellerMemIdAndShowStatus(@Param("sellerMemId") Integer sellerMemId);

    // 根據訂單ID列表查詢評價
    List<SellerReviewVO> findByOrderIdIn(List<Integer> orderIds);
}