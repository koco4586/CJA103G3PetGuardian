package com.petguardian.seller.service;

import com.petguardian.member.model.Member;
import com.petguardian.member.repository.register.MemberRegisterRepository;
import com.petguardian.orders.model.OrdersRepository;
import com.petguardian.orders.model.OrdersVO;
import com.petguardian.orders.model.StoreMemberRepository;
import com.petguardian.orders.model.StoreMemberVO;
import com.petguardian.sellerreview.model.SellerReviewRepository;
import com.petguardian.sellerreview.model.SellerReviewVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 賣家管理中心 - 營運概況 Service 實作
 */
@Service
public class SellerDashboardServiceImpl implements SellerDashboardService {

    @Autowired
    private MemberRegisterRepository memberRepository;

    @Autowired
    private StoreMemberRepository storeMemberRepository;

    @Autowired
    private SellerReviewRepository sellerReviewRepository;

    @Autowired
    private OrdersRepository ordersRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private SellerOrderService sellerOrderService;

    // ==================== 賣家資訊 ====================

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getSellerBasicInfo(Integer memId) {
        Map<String, Object> result = new HashMap<>();

        if (memId == null) {
            return result;
        }

        // 從 Member 表取得完整資料（包含 memImage）
        Optional<Member> memberOpt = memberRepository.findById(memId);
        if (memberOpt.isPresent()) {
            Member member = memberOpt.get();
            result.put("memId", member.getMemId());
            result.put("memName", member.getMemName());
            result.put("memImage", member.getMemImage());
        } else {
            // 嘗試從 StoreMemberVO 取得基本資料
            Optional<StoreMemberVO> storeMemberOpt = storeMemberRepository.findById(memId);
            if (storeMemberOpt.isPresent()) {
                StoreMemberVO storeMember = storeMemberOpt.get();
                result.put("memId", storeMember.getMemId());
                result.put("memName", storeMember.getMemName());
                result.put("memImage", storeMember.getMemImage());
            }
        }

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getSellerRatingStats(Integer memId) {
        Map<String, Object> result = new HashMap<>();

        if (memId == null) {
            result.put("averageRating", 0.0);
            result.put("totalRatingCount", 0);
            return result;
        }

        // 使用 SellerReviewRepository 的查詢方法
        Double avgRating = sellerReviewRepository.calculateAverageRatingBySellerMemId(memId);
        Long reviewCount = sellerReviewRepository.countBySellerMemIdAndShowStatus(memId);

        // 處理 null 值
        if (avgRating == null) avgRating = 0.0;
        if (reviewCount == null) reviewCount = 0L;

        // 四捨五入到小數點後一位
        avgRating = Math.round(avgRating * 10.0) / 10.0;

        result.put("averageRating", avgRating);
        result.put("totalRatingCount", reviewCount.intValue());

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getSellerReviews(Integer sellerId) {
        List<Map<String, Object>> result = new ArrayList<>();

        if (sellerId == null) {
            return result;
        }

        // 使用 Repository 的現有方法查詢賣家評價（showStatus = 0 表示顯示）
        List<SellerReviewVO> reviews = sellerReviewRepository.findBySellerMemIdAndShowStatus(sellerId, 0);

        if (reviews == null || reviews.isEmpty()) {
            return result;
        }

        // 組裝結果
        for (SellerReviewVO review : reviews) {
            Map<String, Object> reviewData = new HashMap<>();

            reviewData.put("orderId", review.getOrderId());
            reviewData.put("rating", review.getRating());
            reviewData.put("reviewContent", review.getReviewContent());
            reviewData.put("reviewTime", review.getReviewTime());

            // 取得訂單和買家名稱
            Optional<OrdersVO> orderOpt = ordersRepository.findById(review.getOrderId());
            if (orderOpt.isPresent()) {
                OrdersVO order = orderOpt.get();
                Integer buyerMemId = order.getBuyerMemId();

                // 取得買家名稱
                Optional<StoreMemberVO> buyerOpt = storeMemberRepository.findById(buyerMemId);
                if (buyerOpt.isPresent() && buyerOpt.get().getMemName() != null) {
                    reviewData.put("buyerName", buyerOpt.get().getMemName());
                } else {
                    reviewData.put("buyerName", "買家 #" + buyerMemId);
                }
            } else {
                reviewData.put("buyerName", "未知買家");
            }

            result.add(reviewData);
        }

        return result;
    }

    // ==================== 整合查詢 ====================

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getDashboardData(Integer sellerId) {
        Map<String, Object> result = new HashMap<>();

        // 賣家基本資訊
        result.put("sellerInfo", getSellerBasicInfo(sellerId));

        // 商品統計
        result.put("totalProducts", productService.countSellerProducts(sellerId));
        result.put("activeProducts", productService.countActiveProducts(sellerId));

        // 訂單統計
        List<OrdersVO> allOrders = sellerOrderService.getSellerOrders(sellerId);
        result.put("totalOrders", allOrders.size());
        result.put("pendingShipment", sellerOrderService.countPendingShipment(sellerId));
        result.put("totalRevenue", sellerOrderService.calculateTotalRevenue(sellerId));

        // 評價統計
        Map<String, Object> ratingStats = getSellerRatingStats(sellerId);
        result.put("averageRating", ratingStats.get("averageRating"));
        result.put("totalRatingCount", ratingStats.get("totalRatingCount"));

        // 評價列表
        result.put("allReviews", getSellerReviews(sellerId));

        return result;
    }
}