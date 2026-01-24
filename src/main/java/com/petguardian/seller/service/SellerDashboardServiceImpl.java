package com.petguardian.seller.service;

import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.petguardian.member.model.Member;
import com.petguardian.member.repository.register.MemberRegisterRepository;
import com.petguardian.orders.model.OrdersRepository;
import com.petguardian.orders.model.OrdersVO;
import com.petguardian.orders.model.StoreMemberRepository;
import com.petguardian.orders.model.StoreMemberVO;
import com.petguardian.seller.model.Product;
import com.petguardian.seller.model.ProductPic;
import com.petguardian.seller.model.ProductPicRepository;
import com.petguardian.seller.model.ProductRepository;
import com.petguardian.sellerreview.model.SellerReviewRepository;
import com.petguardian.sellerreview.model.SellerReviewVO;
import com.petguardian.wallet.model.Wallet;
import com.petguardian.wallet.model.WalletRepository;

/**
 * 賣家管理中心 Service 實作
 */
@Service
public class SellerDashboardServiceImpl implements SellerDashboardService {

    @Autowired
    private StoreMemberRepository storeMemberRepository;

    @Autowired
    private MemberRegisterRepository memberRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductPicRepository productPicRepository;

    @Autowired
    private OrdersRepository ordersRepository;

    @Autowired
    private SellerReviewRepository sellerReviewRepository;

    @Autowired
    private WalletRepository walletRepository;

    // 預設圖片（1x1 灰色像素）
    private static final String DEFAULT_IMAGE = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8/5+hHgAHggJ/PchI7wAAAABJRU5ErkJggg==";

    // ==================== 賣家資訊 ====================

    @Override
    public Map<String, Object> getSellerBasicInfo(Integer memId) {
        Map<String, Object> result = new HashMap<>();

        // 從 Member 表取得完整資料（包含 memImage）
        Optional<Member> memberOpt = memberRepository.findById(memId);
        if (memberOpt.isPresent()) {
            Member member = memberOpt.get();
            result.put("memId", member.getMemId());
            result.put("memName", member.getMemName());
            result.put("memImage", member.getMemImage()); // memImage 是 String 類型
        } else {
            // 嘗試從 StoreMemberVO 取得基本資料
            Optional<StoreMemberVO> storeMemberOpt = storeMemberRepository.findById(memId);
            if (storeMemberOpt.isPresent()) {
                StoreMemberVO storeMember = storeMemberOpt.get();
                result.put("memId", storeMember.getMemId());
                result.put("memName", storeMember.getMemName());
                result.put("memImage", null);
            }
        }

        return result;
    }

    @Override
    public Map<String, Object> getSellerRatingStats(Integer memId) {
        Map<String, Object> result = new HashMap<>();

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

    // ==================== 商品圖片管理 ====================

    @Override
    public List<Map<String, Object>> getProductImages(Integer proId) {
        List<Map<String, Object>> result = new ArrayList<>();

        List<ProductPic> pics = productPicRepository.findByProduct_ProId(proId);
        for (ProductPic pic : pics) {
            Map<String, Object> picData = new HashMap<>();
            picData.put("productPicId", pic.getProductPicId());

            // 轉成 Base64
            if (pic.getProPic() != null) {
                String base64 = "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(pic.getProPic());
                picData.put("imageBase64", base64);
            } else {
                picData.put("imageBase64", DEFAULT_IMAGE);
            }

            result.add(picData);
        }

        return result;
    }

    @Override
    public String getProductMainImage(Integer proId) {
        List<ProductPic> pics = productPicRepository.findByProduct_ProId(proId);

        if (pics != null && !pics.isEmpty()) {
            ProductPic firstPic = pics.get(0);
            if (firstPic.getProPic() != null) {
                return "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(firstPic.getProPic());
            }
        }

        return DEFAULT_IMAGE;
    }

    @Override
    @Transactional
    public void saveProductImages(Integer proId, List<MultipartFile> images) {
        if (images == null || images.isEmpty()) {
            return;
        }

        // 取得商品
        Optional<Product> productOpt = productRepository.findById(proId);
        if (!productOpt.isPresent()) {
            return;
        }
        Product product = productOpt.get();

        // 儲存每張圖片
        for (MultipartFile image : images) {
            try {
                if (image != null && !image.isEmpty()) {
                    ProductPic pic = new ProductPic();
                    pic.setProduct(product);
                    pic.setProPic(image.getBytes());
                    productPicRepository.save(pic);
                }
            } catch (Exception e) {
                // 記錄錯誤但繼續處理其他圖片
                System.err.println("儲存圖片失敗: " + e.getMessage());
            }
        }
    }

    @Override
    @Transactional
    public void deleteProductImage(Integer productPicId) {
        if (productPicId != null) {
            productPicRepository.deleteById(productPicId);
        }
    }

    // ==================== 訂單操作 ====================

    @Override
    @Transactional
    public boolean shipOrder(Integer sellerId, Integer orderId) {
        // 取得訂單
        Optional<OrdersVO> orderOpt = ordersRepository.findById(orderId);
        if (!orderOpt.isPresent()) {
            return false;
        }

        OrdersVO order = orderOpt.get();

        // 檢查是否為該賣家的訂單
        if (!order.getSellerMemId().equals(sellerId)) {
            return false;
        }

        // 檢查訂單狀態是否為「已付款(0)」
        if (order.getOrderStatus() == null || order.getOrderStatus() != 0) {
            return false;
        }

        // 更新為「已出貨(1)」
        order.setOrderStatus(1);
        ordersRepository.save(order);

        return true;
    }

    @Override
    @Transactional
    public Integer cancelOrderWithRefund(Integer sellerId, Integer orderId) {
        // 取得訂單
        Optional<OrdersVO> orderOpt = ordersRepository.findById(orderId);
        if (!orderOpt.isPresent()) {
            return null;
        }

        OrdersVO order = orderOpt.get();

        // 檢查是否為該賣家的訂單
        if (!order.getSellerMemId().equals(sellerId)) {
            return null;
        }

        // 檢查訂單狀態是否為「已付款(0)」
        if (order.getOrderStatus() == null || order.getOrderStatus() != 0) {
            return null;
        }

        // 取得買家錢包（使用 Optional）
        Optional<Wallet> buyerWalletOpt = walletRepository.findByMemId(order.getBuyerMemId());
        if (!buyerWalletOpt.isPresent()) {
            return null;
        }
        Wallet buyerWallet = buyerWalletOpt.get();

        // 退款金額
        Integer refundAmount = order.getOrderTotal();
        if (refundAmount == null) {
            refundAmount = 0;
        }

        // 執行退款
        buyerWallet.setBalance(buyerWallet.getBalance() + refundAmount);
        walletRepository.save(buyerWallet);

        // 更新訂單狀態為「已取消(3)」
        order.setOrderStatus(3);
        ordersRepository.save(order);

        return refundAmount;
    }

    // ==================== 評價查詢 ====================

    @Override
    public List<Map<String, Object>> getSellerReviews(Integer sellerId) {
        List<Map<String, Object>> result = new ArrayList<>();

        // 使用 Repository 的現有方法查詢賣家評價
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
                if (buyerOpt.isPresent()) {
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
}