package com.petguardian.seller.controller;

import com.petguardian.common.service.AuthStrategyService;
import com.petguardian.orders.model.OrderItemVO;
import com.petguardian.orders.service.ReturnOrderService;
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

import java.util.ArrayList;
import java.util.HashMap;
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

    @Autowired
    private ReturnOrderService returnOrderService;



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
            return "redirect:/front/loginpage";
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
            return "redirect:/front/loginpage";
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
            @RequestParam(value = "productImages", required = false) List<MultipartFile> productImages,
            @RequestParam(value = "deleteImageIds", required = false) List<Integer> deleteImageIds,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
//         詳細記錄接收到的資料
        System.out.println("=== saveProduct Controller 開始 ===");
        System.out.println("商品ID: " + proId);
        System.out.println("商品名稱: " + proName);
        System.out.println("類別ID: " + proTypeId);
        System.out.println("價格: " + proPrice);
        System.out.println("庫存: " + stockQuantity);
        System.out.println("狀態: " + proState);
        System.out.println("待刪除圖片IDs: " + deleteImageIds);

        // 詳細記錄圖片資訊
        if (productImages == null) {
            System.out.println("productImages 參數為 null");
        } else {
            System.out.println("productImages 數量: " + productImages.size());
            for (int i = 0; i < productImages.size(); i++) {
                MultipartFile file = productImages.get(i);
                if (file == null) {
                    System.out.println("  圖片[" + i + "]: null");
                } else {
                    System.out.println("  圖片[" + i + "]: name=" + file.getOriginalFilename()
                            + ", size=" + file.getSize()
                            + ", contentType=" + file.getContentType()
                            + ", isEmpty=" + file.isEmpty());
                }
            }
        }

        Integer sellerId = getCurrentMemId(request);
        if (sellerId == null) {
            System.out.println("錯誤: sellerId 為 null，重導向到 /store");
            return "redirect:/front/loginpage";
        }
        System.out.println("賣家ID: " + sellerId);

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
            return "redirect:/front/loginpage";
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
            return "redirect:/front/loginpage";
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
            return "redirect:/front/loginpage";
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
     * 取得訂單商品項目（AJAX）
     * 用於訂單Modal中動態載入商品明細
     */
    @GetMapping("/order/{orderId}/items")
    @ResponseBody
    public List<Map<String, Object>> getOrderItems(@PathVariable Integer orderId, HttpServletRequest request) {
        Integer sellerId = getCurrentMemId(request);
        if (sellerId == null) {
            return new ArrayList<>();
        }

        // 驗證訂單是否屬於該賣家
        Map<String, Object> orderDetail = orderService.getOrderDetail(sellerId, orderId);
        if (orderDetail == null) {
            return new ArrayList<>();
        }

        // 取得訂單項目
        List<OrderItemVO> items = (List<OrderItemVO>) orderDetail.get("items");
        if (items == null || items.isEmpty()) {
            return new ArrayList<>();
        }

        // 轉換為前端需要的格式
        List<Map<String, Object>> result = new ArrayList<>();
        for (OrderItemVO item : items) {
            Map<String, Object> itemData = new HashMap<>();
            itemData.put("proId", item.getProId());
            itemData.put("proPrice", item.getProPrice());
            itemData.put("quantity", item.getQuantity());
            itemData.put("subtotal", item.getSubtotal());

            // 透過 ProductService 取得商品名稱
            String productName = productService.getProductById(item.getProId())
                    .map(product -> product.getProName())
                    .orElse("商品 #" + item.getProId());
            itemData.put("productName", productName);

            // 取得商品主圖片
            String productImage = productService.getProductMainImage(item.getProId());
            itemData.put("productImage", productImage);

            result.add(itemData);
        }

        return result;
    }
    /**
     * 取得訂單退貨資訊（AJAX）
     * 用於訂單Modal中動態載入退貨資訊
     * 當訂單狀態為「申請退貨中」(4)或「退貨完成」(5)時使用
     */
    @GetMapping("/order/{orderId}/return-info")
    @ResponseBody
    public Map<String, Object> getOrderReturnInfo(@PathVariable Integer orderId, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();

        Integer sellerId = getCurrentMemId(request);
        if (sellerId == null) {
            result.put("success", false);
            result.put("error", "未登入");
            return result;
        }

        // 驗證訂單是否屬於該賣家
        Map<String, Object> orderDetail = orderService.getOrderDetail(sellerId, orderId);
        if (orderDetail == null) {
            result.put("success", false);
            result.put("error", "訂單不存在或無權限查看");
            return result;
        }

        // 取得訂單
        com.petguardian.orders.model.OrdersVO order =
                (com.petguardian.orders.model.OrdersVO) orderDetail.get("order");

        // 檢查訂單狀態是否為退貨相關
        Integer orderStatus = order.getOrderStatus();
        if (orderStatus == null || (orderStatus != 4 && orderStatus != 5)) {
            result.put("success", false);
            result.put("hasReturnOrder", false);
            result.put("error", "此訂單無退貨資訊");
            return result;
        }

        // 查詢退貨單資訊
        java.util.Optional<com.petguardian.orders.model.ReturnOrderVO> returnOrderOpt =
                returnOrderService.getReturnOrderByOrderId(orderId);

        if (returnOrderOpt.isPresent()) {
            com.petguardian.orders.model.ReturnOrderVO returnOrder = returnOrderOpt.get();
            result.put("success", true);
            result.put("hasReturnOrder", true);
            result.put("returnId", returnOrder.getReturnId());
            result.put("applyTime", returnOrder.getApplyTime() != null ?
                    returnOrder.getApplyTime().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "-");
            result.put("returnReason", returnOrder.getReturnReason());
            result.put("refundAmount", returnOrder.getRefundAmount());
            result.put("returnStatus", returnOrder.getReturnStatus());

            // 退貨狀態文字
            String returnStatusText = "審核中";
            if (returnOrder.getReturnStatus() != null) {
                switch (returnOrder.getReturnStatus()) {
                    case 0: returnStatusText = "審核中"; break;
                    case 1: returnStatusText = "退貨通過"; break;
                    case 2: returnStatusText = "退貨失敗"; break;
                }
            }
            result.put("returnStatusText", returnStatusText);
        } else {
            result.put("success", false);
            result.put("hasReturnOrder", false);
            result.put("error", "查無退貨資料");
        }

        return result;
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
            return "redirect:/front/loginpage";
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
            return "redirect:/front/loginpage";
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