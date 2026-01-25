package com.petguardian.seller.controller;

import com.petguardian.common.service.AuthStrategyService;
import com.petguardian.seller.model.*;
import com.petguardian.orders.model.*;
import com.petguardian.seller.service.*;
import com.petguardian.sellerreview.service.SellerReviewService;
import com.petguardian.sellerreview.model.SellerReviewVO;
import com.petguardian.wallet.model.Wallet;
import com.petguardian.wallet.model.WalletRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * 賣家管理中心控制器
 */
@Controller
@RequestMapping("/seller")
public class SellerController {

    @Autowired
    private ProductService productService;

    @Autowired
    private SellerOrderService sellerOrderService;

    @Autowired
    private AuthStrategyService authService; // 使用科宏

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private SellerReviewService sellerReviewService;

    /**
     * 取得當前登入會員 ID（使用 AuthStrategyService）
     * ✨ 修改：改用 HttpServletRequest 作為參數
     */
    private Integer getCurrentMemId(HttpServletRequest request) {
        return authService.getCurrentUserId(request);
    }

    /**
     * 賣家管理中心 - 首頁（營運概況）
     * URL: /seller/dashboard
     */
    @GetMapping("/dashboard")
    public String showSellerDashboard(HttpServletRequest request, Model model) {

        Integer sellerId = getCurrentMemId(request);
        if (sellerId == null) {
            return "redirect:/store"; // 未登入導向登入頁
        }

        // 查詢賣家的所有商品
        List<Product> allProducts = productService.getSellerProducts(sellerId);

        // 查詢賣家的所有訂單
        List<OrdersVO> allOrders = sellerOrderService.getSellerOrders(sellerId);

        // 統計數據
        long totalProducts = allProducts.size();
        long activeProducts = allProducts.stream()
                .filter(p -> p.getProState() == 1)
                .count();
        long totalOrders = allOrders.size();
        long pendingOrders = allOrders.stream()
                .filter(o -> o.getOrderStatus() == 0)
                .count();

        // 評分統計
        Map<String, Object> ratingStats = sellerReviewService.getSellerRatingStats(sellerId);
        Double averageRating = (Double) ratingStats.get("averageRating");
        Long totalReviews = (Long) ratingStats.get("reviewCount");
        Integer totalRatingScore = (int) (averageRating * totalReviews);

        @SuppressWarnings("unchecked")
        List<SellerReviewVO> allReviews = (List<SellerReviewVO>) ratingStats.get("reviews");

        // 為每個評論加入訂單資訊
        List<Map<String, Object>> reviewsWithOrderInfo = allReviews.stream()
                .map(review -> {
                    Map<String, Object> reviewMap = new HashMap<>();
                    reviewMap.put("review", review);

                    sellerOrderService.getOrderById(review.getOrderId())
                            .ifPresent(order -> {
                                reviewMap.put("buyerName", order.getReceiverName());
                            });

                    return reviewMap;
                })
                .toList();

        // ✨ 錢包餘額
        Integer walletBalance = walletRepository.findByMemId(sellerId)
                .map(Wallet::getBalance)
                .orElse(0);

        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("activeProducts", activeProducts);
        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("pendingOrders", pendingOrders);
        model.addAttribute("totalRatingScore", totalRatingScore);
        model.addAttribute("totalReviews", totalReviews);
        model.addAttribute("averageRating", String.format("%.1f", averageRating));
        model.addAttribute("walletBalance", walletBalance);
        model.addAttribute("allReviews", reviewsWithOrderInfo);
        model.addAttribute("currentView", "overview");
        model.addAttribute("sellerName", "賣家 #" + sellerId);

        return "frontend/store-seller";
    }

    /**
     * 賣家管理中心 - 商品管理
     * URL: /seller/products
     */
    @GetMapping("/products")
    public String showSellerProducts(HttpServletRequest request, Model model) {

        Integer sellerId = getCurrentMemId(request);
        if (sellerId == null) {
            return "redirect:/store";
        }

        List<Product> products = productService.getSellerProducts(sellerId);
        List<ProType> proTypes = productService.getAllProTypes();

        model.addAttribute("products", products);
        model.addAttribute("proTypes", proTypes);
        model.addAttribute("currentView", "products");

        return "frontend/store-seller";
    }

    /**
     * 賣家管理中心 - 訂單管理
     * URL: /seller/orders
     */
    @GetMapping("/orders")
    public String showSellerOrders(HttpServletRequest request, Model model) {

        Integer sellerId = getCurrentMemId(request);
        if (sellerId == null) {
            return "redirect:/store";
        }

        List<OrdersVO> orders = sellerOrderService.getSellerOrders(sellerId);

        model.addAttribute("orders", orders);
        model.addAttribute("currentView", "orders");

        return "frontend/store-seller";
    }

    /**
     * 新增/編輯商品
     * URL: POST /seller/product/save
     */
    @PostMapping("/product/save")
    public String saveProduct(
            @RequestParam(required = false) Integer proId,
            @RequestParam String proName,
            @RequestParam Integer proTypeId,
            @RequestParam Integer proPrice,
            @RequestParam String proDescription,
            @RequestParam Integer stockQuantity,
            @RequestParam Integer proState,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        Integer sellerId = getCurrentMemId(request);
        if (sellerId == null) {
            return "redirect:/store";
        }

        Product product;
        if (proId != null && proId > 0) {
            // 編輯現有商品
            product = productService.getProductById(proId)
                    .orElseThrow(() -> new RuntimeException("商品不存在"));

            // 驗證商品是否屬於當前賣家
            if (!product.getMemId().equals(sellerId)) {
                redirectAttributes.addFlashAttribute("error", "無權限編輯此商品");
                return "redirect:/seller/products";
            }
        } else {
            // 新增商品
            product = new Product();
            product.setMemId(sellerId);
        }

        // 設定商品類別
        ProType proType = productService.getAllProTypes().stream()
                .filter(t -> t.getProTypeId().equals(proTypeId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("商品類別不存在"));

        product.setProType(proType);
        product.setProName(proName);
        product.setProPrice(proPrice);
        product.setProDescription(proDescription);
        product.setStockQuantity(stockQuantity);
        product.setProState(proState);

        productService.saveProduct(product);

        redirectAttributes.addFlashAttribute("successMessage", "商品儲存成功!");
        return "redirect:/seller/products";
    }

    /**
     * 刪除商品
     * URL: POST /seller/product/delete
     */
    @PostMapping("/product/delete")
    public String deleteProduct(
            @RequestParam Integer proId,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        Integer sellerId = getCurrentMemId(request);
        if (sellerId == null) {
            return "redirect:/store";
        }

        // 驗證商品是否屬於當前賣家
        Product product = productService.getProductById(proId)
                .orElseThrow(() -> new RuntimeException("商品不存在"));

        if (!product.getMemId().equals(sellerId)) {
            redirectAttributes.addFlashAttribute("error", "無權限刪除此商品");
            return "redirect:/seller/products";
        }

        productService.deleteProduct(proId);

        redirectAttributes.addFlashAttribute("successMessage", "商品已刪除!");
        return "redirect:/seller/products";
    }

    /**
     * 更新訂單狀態（出貨）
     * URL: POST /seller/order/ship
     */
    @PostMapping("/order/ship")
    public String shipOrder(
            @RequestParam Integer orderId,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        Integer sellerId = getCurrentMemId(request);
        if (sellerId == null) {
            return "redirect:/store";
        }

        // 驗證訂單是否屬於當前賣家
        OrdersVO order = sellerOrderService.getOrderById(orderId)
                .orElseThrow(() -> new RuntimeException("訂單不存在"));

        if (!order.getSellerMemId().equals(sellerId)) {
            redirectAttributes.addFlashAttribute("error", "無權限操作此訂單");
            return "redirect:/seller/orders";
        }

        sellerOrderService.updateOrderStatus(orderId, 1); // 1=已出貨

        redirectAttributes.addFlashAttribute("successMessage", "訂單已標記為已出貨!");
        return "redirect:/seller/orders";
    }

    /**
     * 取消訂單（含退款）
     * URL: POST /seller/order/cancel
     */
    @PostMapping("/order/cancel")
    public String cancelOrder(
            @RequestParam Integer orderId,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        Integer sellerId = getCurrentMemId(request);
        if (sellerId == null) {
            return "redirect:/store";
        }

        // 驗證訂單是否屬於當前賣家
        OrdersVO order = sellerOrderService.getOrderById(orderId)
                .orElseThrow(() -> new RuntimeException("訂單不存在"));

        if (!order.getSellerMemId().equals(sellerId)) {
            redirectAttributes.addFlashAttribute("error", "無權限操作此訂單");
            return "redirect:/seller/orders";
        }

        // 只有已付款狀態才能取消
        if (order.getOrderStatus() != 0) {
            redirectAttributes.addFlashAttribute("error", "只有已付款狀態的訂單可以取消");
            return "redirect:/seller/orders";
        }

        try {
            // 更新訂單狀態為已取消
            sellerOrderService.updateOrderStatus(orderId, 3); // 3=已取消

            // ✨ 退款給買家（錢包）
            Integer refundAmount = order.getOrderTotal();
            Wallet buyerWallet = walletRepository.findByMemId(order.getBuyerMemId())
                    .orElseThrow(() -> new RuntimeException("買家錢包不存在"));

            buyerWallet.setBalance(buyerWallet.getBalance() + refundAmount);
            walletRepository.save(buyerWallet);

            redirectAttributes.addFlashAttribute("successMessage",
                    "訂單已取消，已退款 $" + refundAmount + " 至買家錢包");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "取消訂單失敗：" + e.getMessage());
        }

        return "redirect:/seller/orders";
    }

    /**
     * 查看訂單詳情
     * URL: GET /seller/order/{orderId}
     */
    @GetMapping("/order/{orderId}")
    public String viewOrderDetail(
            @PathVariable Integer orderId,
            HttpServletRequest request,
            Model model,
            RedirectAttributes redirectAttributes) {

        Integer sellerId = getCurrentMemId(request);
        if (sellerId == null) {
            return "redirect:/store";
        }

        // 驗證訂單是否屬於當前賣家
        OrdersVO order = sellerOrderService.getOrderById(orderId)
                .orElseThrow(() -> new RuntimeException("訂單不存在"));

        if (!order.getSellerMemId().equals(sellerId)) {
            redirectAttributes.addFlashAttribute("error", "無權限查看此訂單");
            return "redirect:/seller/orders";
        }

        // 取得訂單項目
        List<OrderItemVO> items = sellerOrderService.getOrderItems(orderId);

        model.addAttribute("order", order);
        model.addAttribute("items", items);

        return "frontend/seller/order-detail";
    }
}