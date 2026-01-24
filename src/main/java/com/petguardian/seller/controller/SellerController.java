package com.petguardian.seller.controller;

import com.petguardian.common.service.AuthStrategyService;
import com.petguardian.seller.model.ProType;
import com.petguardian.seller.service.ProductService;
import com.petguardian.seller.service.SellerDashboardService;
import com.petguardian.seller.service.SellerOrderService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

/**
 * 賣家管理中心 Controller
 */
@Controller
@RequestMapping("/seller")
public class SellerController {

    @Autowired
    private AuthStrategyService authService;

    @Autowired
    private SellerDashboardService dashboardService;

    @Autowired
    private ProductService productService;

    @Autowired
    private SellerOrderService orderService;

    /**
     * 取得當前登入會員 ID
     */
    private Integer getCurrentMemId(HttpServletRequest request) {
        return authService.getCurrentUserId(request);
    }

    // ==================== 營運概況 ====================

    /**
     * 賣家管理中心首頁
     */
    @GetMapping("/dashboard")
    public String showDashboard(HttpServletRequest request, Model model) {
        Integer sellerId = getCurrentMemId(request);
        if (sellerId == null) {
            return "redirect:/store";
        }

        // 取得所有營運概況資料（一次呼叫）
        Map<String, Object> dashboardData = dashboardService.getDashboardData(sellerId);

        // 傳遞所有資料到 Model
        model.addAttribute("sellerInfo", dashboardData.get("sellerInfo"));
        model.addAttribute("totalProducts", dashboardData.get("totalProducts"));
        model.addAttribute("activeProducts", dashboardData.get("activeProducts"));
        model.addAttribute("totalOrders", dashboardData.get("totalOrders"));
        model.addAttribute("pendingShipment", dashboardData.get("pendingShipment"));
        model.addAttribute("averageRating", dashboardData.get("averageRating"));
        model.addAttribute("totalRatingCount", dashboardData.get("totalRatingCount"));
        model.addAttribute("totalRevenue", dashboardData.get("totalRevenue"));
        model.addAttribute("allReviews", dashboardData.get("allReviews"));
        model.addAttribute("currentView", "overview");

        return "frontend/store-seller";
    }

    // ==================== 商品管理 ====================

    /**
     * 商品管理頁面
     */
    @GetMapping("/products")
    public String showProducts(HttpServletRequest request, Model model) {
        Integer sellerId = getCurrentMemId(request);
        if (sellerId == null) {
            return "redirect:/store";
        }

        // 取得商品列表（含圖片）
        List<Map<String, Object>> productsWithImages = productService.getSellerProductsWithImages(sellerId);
        List<ProType> proTypes = productService.getAllProTypes();

        // 賣家資訊（側邊欄用）
        model.addAttribute("sellerInfo", dashboardService.getSellerBasicInfo(sellerId));
        model.addAttribute("productsWithImages", productsWithImages);
        model.addAttribute("proTypes", proTypes);
        model.addAttribute("currentView", "products");

        return "frontend/store-seller";
    }

    /**
     * 取得商品圖片（AJAX）
     */
    @GetMapping("/product/{proId}/images")
    @ResponseBody
    public List<Map<String, Object>> getProductImages(@PathVariable Integer proId) {
        return productService.getProductImages(proId);
    }

    /**
     * 儲存商品（新增或編輯）
     */
    @PostMapping("/product/save")
    public String saveProduct(
            @RequestParam(required = false) Integer proId,
            @RequestParam String proName,
            @RequestParam Integer proTypeId,
            @RequestParam Integer proPrice,
            @RequestParam(required = false) String proDescription,
            @RequestParam Integer stockQuantity,
            @RequestParam Integer proState,
            @RequestParam(required = false) List<MultipartFile> productImages,
            @RequestParam(required = false) List<Integer> deleteImageIds,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        Integer sellerId = getCurrentMemId(request);
        if (sellerId == null) {
            return "redirect:/store";
        }

        try {
            productService.saveProductWithImages(sellerId, proId, proName, proTypeId,
                    proPrice, proDescription, stockQuantity, proState,
                    productImages, deleteImageIds);

            redirectAttributes.addFlashAttribute("successMessage", "商品儲存成功!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/seller/products";
    }

    /**
     * 刪除商品
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

        boolean success = productService.deleteProductBySeller(sellerId, proId);
        if (success) {
            redirectAttributes.addFlashAttribute("successMessage", "商品已刪除!");
        } else {
            redirectAttributes.addFlashAttribute("error", "無權限刪除此商品");
        }

        return "redirect:/seller/products";
    }

    // ==================== 訂單管理 ====================

    /**
     * 訂單管理頁面
     */
    @GetMapping("/orders")
    public String showOrders(HttpServletRequest request, Model model) {
        Integer sellerId = getCurrentMemId(request);
        if (sellerId == null) {
            return "redirect:/store";
        }

        // 取得訂單列表（含買家名稱、是否可出貨）
        List<Map<String, Object>> ordersWithDetails = orderService.getSellerOrdersWithDetails(sellerId);

        // 賣家資訊（側邊欄用）
        model.addAttribute("sellerInfo", dashboardService.getSellerBasicInfo(sellerId));
        model.addAttribute("ordersWithDetails", ordersWithDetails);
        model.addAttribute("currentView", "orders");

        return "frontend/store-seller";
    }

    /**
     * 訂單詳情頁面
     */
    @GetMapping("/order/{orderId}")
    public String showOrderDetail(
            @PathVariable Integer orderId,
            HttpServletRequest request,
            Model model,
            RedirectAttributes redirectAttributes) {

        Integer sellerId = getCurrentMemId(request);
        if (sellerId == null) {
            return "redirect:/store";
        }

        // 取得訂單詳情
        Map<String, Object> orderDetail = orderService.getOrderDetail(sellerId, orderId);

        if (orderDetail == null) {
            redirectAttributes.addFlashAttribute("error", "訂單不存在或無權限查看");
            return "redirect:/seller/orders";
        }

        model.addAttribute("order", orderDetail.get("order"));
        model.addAttribute("items", orderDetail.get("items"));
        model.addAttribute("buyerName", orderDetail.get("buyerName"));

        return "frontend/seller/order-detail";
    }

    /**
     * 出貨
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

        boolean success = orderService.shipOrder(sellerId, orderId);
        if (success) {
            redirectAttributes.addFlashAttribute("successMessage", "訂單已標記為已出貨!");
        } else {
            redirectAttributes.addFlashAttribute("error", "出貨失敗：訂單不存在或狀態不正確");
        }

        return "redirect:/seller/orders";
    }

    /**
     * 取消訂單（含退款）
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

        Integer refundAmount = orderService.cancelOrderWithRefund(sellerId, orderId);
        if (refundAmount != null) {
            redirectAttributes.addFlashAttribute("successMessage",
                    "訂單已取消，已退款 $" + refundAmount + " 至買家錢包");
        } else {
            redirectAttributes.addFlashAttribute("error", "取消失敗：訂單不存在或狀態不正確");
        }

        return "redirect:/seller/orders";
    }
}