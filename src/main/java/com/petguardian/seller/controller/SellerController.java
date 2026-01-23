package com.petguardian.seller.controller;

import com.petguardian.common.service.AuthStrategyService;
import com.petguardian.seller.model.*;
import com.petguardian.seller.service.SellerDashboardService;
import com.petguardian.orders.model.*;
import com.petguardian.seller.service.*;
import com.petguardian.sellerreview.service.SellerReviewService;
import com.petguardian.wallet.model.Wallet;
import com.petguardian.wallet.model.WalletRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
    private AuthStrategyService authService;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private SellerReviewService sellerReviewService;

    @Autowired
    private StoreMemberRepository memberRepository;

    @Autowired
    private SellerDashboardService sellerDashboardService;

    /**
     * 取得當前登入會員 ID（使用 AuthStrategyService）
     * ✨ 修改：改用 HttpServletRequest 作為參數
     */
    private Integer getCurrentMemId(HttpServletRequest request) {
        return authService.getCurrentUserId(request);
    }

// ==================== 頁面導向 ====================

    /**
     * 賣家管理中心 - 首頁（營運概況）
     * URL: /seller/dashboard
     */
    @GetMapping("/dashboard")
    public String showSellerDashboard(HttpServletRequest request, Model model) {

        Integer sellerId = getCurrentMemId(request);
        if (sellerId == null) {
            return "redirect:/store";
        }

        // 取得賣家基本資訊（memId, memName, memImage）
        Map<String, Object> sellerInfo = sellerDashboardService.getSellerBasicInfo(sellerId);
        model.addAttribute("sellerInfo", sellerInfo);

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

        // 待出貨 = 訂單狀態為「已付款(0)」的數量
        long pendingShipment = allOrders.stream()
                .filter(o -> o.getOrderStatus() != null && o.getOrderStatus() == 0)
                .count();

        // 評價統計（平均分/5.0、總評價數量）
        Map<String, Object> ratingStats = sellerDashboardService.getSellerRatingStats(sellerId);
        Double averageRating = (Double) ratingStats.get("averageRating");
        Integer totalRatingCount = (Integer) ratingStats.get("totalRatingCount");
        Integer totalRatingScore = (Integer) ratingStats.get("totalRatingScore");

        // 總營收（已完成訂單累計）
        int totalRevenue = allOrders.stream()
                .filter(o -> o.getOrderStatus() != null && o.getOrderStatus() == 2)
                .mapToInt(o -> o.getOrderTotal() != null ? o.getOrderTotal() : 0)
                .sum();

        // 取得評價列表
        List<Map<String, Object>> allReviews = sellerDashboardService.getSellerReviews(sellerId);

        // 傳遞到 Model
        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("activeProducts", activeProducts);
        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("pendingShipment", pendingShipment);  // ✨ 改名：待出貨
        model.addAttribute("averageRating", averageRating);
        model.addAttribute("totalRatingCount", totalRatingCount);  // ✨ 改名：總評價數量
        model.addAttribute("totalRatingScore", totalRatingScore);
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("allReviews", allReviews);
        model.addAttribute("currentView", "overview");

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

        // 取得賣家基本資訊
        Map<String, Object> sellerInfo = sellerDashboardService.getSellerBasicInfo(sellerId);
        model.addAttribute("sellerInfo", sellerInfo);

        List<Product> products = productService.getSellerProducts(sellerId);
        List<ProType> proTypes = productService.getAllProTypes();

        // 為每個商品加上主圖 Base64
        List<Map<String, Object>> productsWithImages = new ArrayList<>();
        for (Product p : products) {
            Map<String, Object> productData = new HashMap<>();
            productData.put("product", p);
            productData.put("mainImage", sellerDashboardService.getProductMainImage(p.getProId()));
            productsWithImages.add(productData);
        }

        model.addAttribute("products", products);
        model.addAttribute("productsWithImages", productsWithImages);
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

        // 取得賣家基本資訊
        Map<String, Object> sellerInfo = sellerDashboardService.getSellerBasicInfo(sellerId);
        model.addAttribute("sellerInfo", sellerInfo);

        List<OrdersVO> orders = sellerOrderService.getSellerOrders(sellerId);

        // 為每筆訂單加上買家名稱和是否可出貨
        List<Map<String, Object>> ordersWithDetails = new ArrayList<>();
        for (OrdersVO order : orders) {
            Map<String, Object> orderData = new HashMap<>();
            orderData.put("order", order);

            // 取得買家名稱
            Optional<StoreMemberVO> buyerOpt = memberRepository.findById(order.getBuyerMemId());
            if (buyerOpt.isPresent()) {
                orderData.put("buyerName", buyerOpt.get().getMemName());
            } else {
                orderData.put("buyerName", "買家 #" + order.getBuyerMemId());
            }

            // 判斷是否可出貨（只有已付款狀態才能出貨）
            boolean canShip = (order.getOrderStatus() != null && order.getOrderStatus() == 0);
            orderData.put("canShip", canShip);

            ordersWithDetails.add(orderData);
        }

        model.addAttribute("orders", orders);
        model.addAttribute("ordersWithDetails", ordersWithDetails);
        model.addAttribute("currentView", "orders");

        return "frontend/store-seller";
    }

    /**
     * 查看訂單詳情
     * URL: GET /seller/order/{orderId}
     */
    @GetMapping("/order/{orderId}")
    public String showOrderDetail(@PathVariable Integer orderId,
                                  HttpServletRequest request, Model model) {

        Integer sellerId = getCurrentMemId(request);
        if (sellerId == null) {
            return "redirect:/store";
        }

        // 取得訂單詳情
        Map<String, Object> orderDetail = sellerOrderService.getOrderDetail(sellerId, orderId);
        if (orderDetail == null) {
            return "redirect:/seller/orders";
        }

        // 取得賣家基本資訊
        Map<String, Object> sellerInfo = sellerDashboardService.getSellerBasicInfo(sellerId);
        model.addAttribute("sellerInfo", sellerInfo);

        model.addAttribute("orderDetail", orderDetail);

        return "frontend/seller/order-detail";
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
            @RequestParam(required = false) String proDescription,
            @RequestParam(defaultValue = "0") Integer stockQuantity,
            @RequestParam(defaultValue = "1") Integer proState,
            @RequestParam(required = false) List<MultipartFile> productImages,
            @RequestParam(required = false) List<Integer> deleteImageIds,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        Integer sellerId = getCurrentMemId(request);
        if (sellerId == null) {
            return "redirect:/store";
        }

        try {
            Product product;

            if (proId != null && proId > 0) {
                // 編輯
                product = productService.getProduct(proId);
                if (product == null || !product.getMemId().equals(sellerId)) {
                    redirectAttributes.addFlashAttribute("error", "無權限編輯此商品");
                    return "redirect:/seller/products";
                }
            } else {
                // 新增
                product = new Product();
                product.setMemId(sellerId);
            }

            product.setProName(proName);
            product.setProTypeId(proTypeId);
            product.setProPrice(proPrice);
            product.setProDescription(proDescription);
            product.setStockQuantity(stockQuantity);
            product.setProState(proState);

            // 儲存商品
            Product savedProduct = productService.saveProduct(product);

            // 刪除指定的圖片
            if (deleteImageIds != null && !deleteImageIds.isEmpty()) {
                for (Integer picId : deleteImageIds) {
                    sellerDashboardService.deleteProductImage(picId);
                }
            }

            // \儲存新上傳的圖片
            if (productImages != null && !productImages.isEmpty()) {
                // 過濾空檔案
                List<MultipartFile> validImages = new ArrayList<>();
                for (MultipartFile img : productImages) {
                    if (img != null && !img.isEmpty()) {
                        validImages.add(img);
                    }
                }
                if (!validImages.isEmpty()) {
                    sellerDashboardService.saveProductImages(savedProduct.getProId(), validImages);
                }
            }

            redirectAttributes.addFlashAttribute("successMessage",
                    proId != null ? "商品更新成功" : "商品上架成功");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "儲存失敗：" + e.getMessage());
        }

        return "redirect:/seller/products";
    }

    /**
     * 刪除商品
     * URL: POST /seller/product/delete
     */
    @PostMapping("/product/delete")
    public String deleteProduct(@RequestParam Integer proId,
                                HttpServletRequest request,
                                RedirectAttributes redirectAttributes) {

        Integer sellerId = getCurrentMemId(request);
        if (sellerId == null) {
            return "redirect:/store";
        }

        try {
            Product product = productService.getProduct(proId);
            if (product == null || !product.getMemId().equals(sellerId)) {
                redirectAttributes.addFlashAttribute("error", "無權限刪除此商品");
                return "redirect:/seller/products";
            }

            productService.deleteProduct(proId);
            redirectAttributes.addFlashAttribute("successMessage", "商品已刪除");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "刪除失敗：" + e.getMessage());
        }

        return "redirect:/seller/products";
    }

    /**
     * 賣家出貨
     * URL: POST /seller/order/ship
     */
    @PostMapping("/order/ship")
    public String shipOrder(@RequestParam Integer orderId,
                            HttpServletRequest request,
                            RedirectAttributes redirectAttributes) {

        Integer sellerId = getCurrentMemId(request);
        if (sellerId == null) {
            return "redirect:/store";
        }

        // 呼叫 Service 處理出貨
        boolean success = sellerDashboardService.shipOrder(sellerId, orderId);

        if (success) {
            redirectAttributes.addFlashAttribute("successMessage", "訂單已出貨");
        } else {
            redirectAttributes.addFlashAttribute("error", "出貨失敗，請確認訂單狀態");
        }

        return "redirect:/seller/orders";
    }

    /**
     * 賣家取消訂單
     * URL: POST /seller/order/cancel
     */
    @PostMapping("/order/cancel")
    public String cancelOrder(@RequestParam Integer orderId,
                              HttpServletRequest request,
                              RedirectAttributes redirectAttributes) {

        Integer sellerId = getCurrentMemId(request);
        if (sellerId == null) {
            return "redirect:/store";
        }

        // 呼叫 Service 處理取消並退款
        Integer refundAmount = sellerDashboardService.cancelOrderWithRefund(sellerId, orderId);

        if (refundAmount != null) {
            redirectAttributes.addFlashAttribute("successMessage",
                    "訂單已取消，已退款 $" + refundAmount + " 給買家");
        } else {
            redirectAttributes.addFlashAttribute("error", "取消失敗，請確認訂單狀態");
        }

        return "redirect:/seller/orders";
    }

    /**
     * 取得商品圖片（AJAX 用）
     * URL: GET /seller/product/{proId}/images
     */
    @GetMapping("/product/{proId}/images")
    @ResponseBody
    public List<Map<String, Object>> getProductImages(@PathVariable Integer proId) {
        return sellerDashboardService.getProductImages(proId);
    }
}