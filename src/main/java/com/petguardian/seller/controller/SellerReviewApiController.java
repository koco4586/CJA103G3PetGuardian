package com.petguardian.seller.controller;

import com.petguardian.sellerreview.service.SellerReviewService;
import com.petguardian.sellerreview.model.SellerReviewVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 賣家評價 API (用於 AJAX 請求)
 */
@RestController
@RequestMapping("/api/seller")
public class SellerReviewApiController {

    @Autowired
    private SellerReviewService reviewService;

    /**
     * 取得訂單評價資料 (用於 Modal 顯示)
     * URL: GET /api/seller/order/{orderId}/review
     */
    @GetMapping("/order/{orderId}/review")
    public ResponseEntity<Map<String, Object>> getOrderReview(@PathVariable Integer orderId) {

        SellerReviewVO review = reviewService.getReviewByOrderId(orderId)
                .orElse(null);

        if (review == null) {
            return ResponseEntity.notFound().build();
        }

        Map<String, Object> response = new HashMap<>();
        response.put("reviewId", review.getReviewId());
        response.put("orderId", review.getOrderId());
        response.put("rating", review.getRating());
        response.put("reviewContent", review.getReviewContent());
        response.put("reviewTime", review.getReviewTime().format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));

        return ResponseEntity.ok(response);
    }
}