package com.petguardian.sellerreview.service;

import com.petguardian.orders.model.OrdersVO;
import com.petguardian.orders.model.OrdersRepository;
import com.petguardian.sellerreview.model.SellerReviewRepository;
import com.petguardian.sellerreview.model.SellerReviewVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class SellerReviewServiceImpl implements SellerReviewService {

    @Autowired
    private SellerReviewRepository sellerReviewDAO;

    @Autowired
    private OrdersRepository ordersDAO;

    // 顯示狀態常數
    public static final Integer SHOW_STATUS_VISIBLE = 0;    // 顯示
    public static final Integer SHOW_STATUS_HIDDEN = 1;     // 不顯示

    // 訂單狀態常數
    private static final Integer ORDER_STATUS_COMPLETED = 2;  // 已完成

    @Override
    public SellerReviewVO createReview(Integer orderId, Integer rating, String reviewContent) {
        if (orderId == null) {
            throw new IllegalArgumentException("訂單ID不能為 null");
        }
        if (rating == null || rating < 1 || rating > 5) {
            throw new IllegalArgumentException("評分必須在1-5星之間");
        }

        // 查詢訂單
        OrdersVO order = ordersDAO.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("訂單不存在: " + orderId));

        // 檢查訂單狀態（可以是已完成或退貨完成）
        if (!order.getOrderStatus().equals(ORDER_STATUS_COMPLETED) && !order.getOrderStatus().equals(5)) {
            throw new IllegalArgumentException("只有已完成的訂單才能評價");
        }

        // 檢查是否已評價
        if (sellerReviewDAO.existsByOrderId(orderId)) {
            throw new IllegalArgumentException("此訂單已評價過");
        }

        // 建立評價
        SellerReviewVO review = new SellerReviewVO();
        review.setOrderId(orderId);
        review.setRating(rating);
        review.setReviewContent(reviewContent);
        review.setShowStatus(SHOW_STATUS_VISIBLE);

        return sellerReviewDAO.save(review);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SellerReviewVO> getReviewById(Integer reviewId) {
        if (reviewId == null) {
            throw new IllegalArgumentException("評價ID不能為 null");
        }
        return sellerReviewDAO.findById(reviewId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SellerReviewVO> getReviewByOrderId(Integer orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException("訂單ID不能為 null");
        }
        return sellerReviewDAO.findByOrderId(orderId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SellerReviewVO> getReviewsByBuyerId(Integer buyerMemId) {
        if (buyerMemId == null) {
            throw new IllegalArgumentException("買家會員ID不能為 null");
        }
        return sellerReviewDAO.findByBuyerMemId(buyerMemId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SellerReviewVO> getReviewsBySellerMemId(Integer sellerMemId) {
        if (sellerMemId == null) {
            throw new IllegalArgumentException("賣家會員ID不能為 null");
        }
        return sellerReviewDAO.findBySellerMemIdAndShowStatus(sellerMemId, SHOW_STATUS_VISIBLE);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SellerReviewVO> getAllReviews() {
        return sellerReviewDAO.findAllByOrderByReviewTimeDesc();
    }

    @Override
    public SellerReviewVO updateShowStatus(Integer reviewId, Integer showStatus) {
        if (reviewId == null) {
            throw new IllegalArgumentException("評價ID不能為 null");
        }
        if (showStatus == null || (showStatus != 0 && showStatus != 1)) {
            throw new IllegalArgumentException("顯示狀態必須是0或1");
        }

        SellerReviewVO review = sellerReviewDAO.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("評價不存在: " + reviewId));

        review.setShowStatus(showStatus);
        return sellerReviewDAO.save(review);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getSellerRatingStats(Integer sellerMemId) {
        if (sellerMemId == null) {
            throw new IllegalArgumentException("賣家會員ID不能為 null");
        }

        Map<String, Object> stats = new HashMap<>();

        // 計算平均評分
        Double avgRating = sellerReviewDAO.calculateAverageRatingBySellerMemId(sellerMemId);
        stats.put("averageRating", avgRating != null ? avgRating : 0.0);

        // 統計評價數量
        Long reviewCount = sellerReviewDAO.countBySellerMemIdAndShowStatus(sellerMemId);
        stats.put("reviewCount", reviewCount != null ? reviewCount : 0L);

        // 獲取評價列表
        List<SellerReviewVO> reviews = sellerReviewDAO.findBySellerMemIdAndShowStatus(sellerMemId, SHOW_STATUS_VISIBLE);
        stats.put("reviews", reviews);

        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasReviewed(Integer orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException("訂單ID不能為 null");
        }
        return sellerReviewDAO.existsByOrderId(orderId);
    }
}
