package com.petguardian.seller.controller;

import com.petguardian.common.service.AuthStrategyService;
import com.petguardian.orders.model.OrderItemVO;
import com.petguardian.orders.model.ReturnOrderVO;
import com.petguardian.orders.service.ReturnOrderService;
import com.petguardian.seller.model.ProType;
import com.petguardian.seller.model.Product;
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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.*;

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

    // 圖片上傳目標資料夾（相對於 static 目錄）
    private static final String UPLOAD_SUB_PATH = "images/store_image/";

    private Integer getCurrentMemId(HttpServletRequest request) {
        return authService.getCurrentUserId(request);
    }

    // ==================== 營運概況 ====================

    @GetMapping("/dashboard")
    public String showDashboard(HttpServletRequest request, Model model) {
        Integer sellerId = getCurrentMemId(request);
        if (sellerId == null) return "redirect:/front/loginpage";

        Map<String, Object> dashboardData = dashboardService.getDashboardData(sellerId);
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

    @GetMapping("/products")
    public String showProducts(HttpServletRequest request, Model model) {
        Integer sellerId = getCurrentMemId(request);
        if (sellerId == null) return "redirect:/front/loginpage";

        List<Map<String, Object>> productsWithImages = productService.getSellerProductsWithImages(sellerId);
        List<ProType> proTypes = productService.getAllProTypes();

        model.addAttribute("sellerInfo", dashboardService.getSellerBasicInfo(sellerId));
        model.addAttribute("productsWithImages", productsWithImages);
        model.addAttribute("proTypes", proTypes);
        model.addAttribute("currentView", "products");

        return "frontend/store-seller";
    }

    @GetMapping("/product/{proId}/images")
    @ResponseBody
    public List<Map<String, Object>> getProductImages(@PathVariable Integer proId) {
        return productService.getProductImages(proId);
    }

    @PostMapping("/product/save")
    public String saveProduct(
            @RequestParam(required = false) Integer proId,
            @RequestParam String proName,
            @RequestParam Integer proTypeId,
            @RequestParam Integer proPrice,
            @RequestParam(required = false) String proDescription,
            @RequestParam Integer stockQuantity,
            @RequestParam Integer proState,
            @RequestParam(value = "productImage", required = false) MultipartFile productImage,
            @RequestParam(value = "deleteImageIds", required = false) List<Integer> deleteImageIds,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        Integer sellerId = getCurrentMemId(request);
        if (sellerId == null) return "redirect:/front/loginpage";

        try {
            // 處理圖片上傳
            String imageUrl = null;
            if (productImage != null && !productImage.isEmpty()) {
                imageUrl = saveUploadedImage(productImage);
            }

            productService.saveProductWithImages(sellerId, proId, proName, proTypeId,
                    proPrice, proDescription, stockQuantity, proState,
                    imageUrl, deleteImageIds);

            redirectAttributes.addFlashAttribute("successMessage", "商品儲存成功!");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "儲存失敗: " + e.getMessage());
        }

        return "redirect:/seller/products";
    }

    /**
     * 將上傳的圖片檔案存到 src/main/resources/static/images/store_image/ 資料夾
     * 回傳前端可存取的 URL 短路徑
     */
    private String saveUploadedImage(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.lastIndexOf(".") > 0) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        // 產生唯一檔名
        String newFilename = "prod_" + UUID.randomUUID().toString().substring(0, 8) + "_" + System.currentTimeMillis() + extension;

        // 取得 static/images/store_image/ 的實際路徑 (本機開發用)
        String staticPath = "src/main/resources/static/" + UPLOAD_SUB_PATH;
        Path uploadDir = Paths.get(staticPath).toAbsolutePath().normalize();

        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        Path targetPath = uploadDir.resolve(newFilename).normalize();
        file.transferTo(targetPath.toFile());

        // 回傳相對路徑供前端讀取
        return "/" + UPLOAD_SUB_PATH + newFilename;
    }

    @PostMapping("/product/delete")
    public String deleteProduct(
            @RequestParam Integer proId,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        Integer sellerId = getCurrentMemId(request);
        if (sellerId == null) return "redirect:/front/loginpage";

        boolean success = productService.deleteProductBySeller(sellerId, proId);
        if (success) {
            redirectAttributes.addFlashAttribute("successMessage", "商品已刪除!");
        } else {
            redirectAttributes.addFlashAttribute("error", "無權限刪除此商品");
        }

        return "redirect:/seller/products";
    }

    // ==================== 訂單管理 ====================

    @GetMapping("/orders")
    public String showOrders(HttpServletRequest request, Model model) {
        Integer sellerId = getCurrentMemId(request);
        if (sellerId == null) return "redirect:/front/loginpage";

        List<Map<String, Object>> ordersWithDetails = orderService.getSellerOrdersWithDetails(sellerId);

        model.addAttribute("sellerInfo", dashboardService.getSellerBasicInfo(sellerId));
        model.addAttribute("ordersWithDetails", ordersWithDetails);
        model.addAttribute("currentView", "orders");

        return "frontend/store-seller";
    }

    @GetMapping("/order/{orderId}")
    public String showOrderDetail(
            @PathVariable Integer orderId,
            HttpServletRequest request,
            Model model,
            RedirectAttributes redirectAttributes) {

        Integer sellerId = getCurrentMemId(request);
        if (sellerId == null) return "redirect:/front/loginpage";

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

    @GetMapping("/order/{orderId}/items")
    @ResponseBody
    public List<Map<String, Object>> getOrderItems(@PathVariable Integer orderId, HttpServletRequest request) {
        Integer sellerId = getCurrentMemId(request);
        if (sellerId == null) return new ArrayList<>();

        Map<String, Object> orderDetail = orderService.getOrderDetail(sellerId, orderId);
        if (orderDetail == null) return new ArrayList<>();

        List<OrderItemVO> items = (List<OrderItemVO>) orderDetail.get("items");
        if (items == null) return new ArrayList<>();

        List<Map<String, Object>> result = new ArrayList<>();
        for (OrderItemVO item : items) {
            Map<String, Object> itemData = new HashMap<>();
            itemData.put("proId", item.getProId());
            itemData.put("proPrice", item.getProPrice());
            itemData.put("quantity", item.getQuantity());
            itemData.put("subtotal", item.getSubtotal());

            String productName = productService.getProductById(item.getProId())
                    .map(Product::getProName)
                    .orElse("商品 #" + item.getProId());
            itemData.put("productName", productName);

            String productImage = productService.getProductMainImage(item.getProId());
            itemData.put("productImage", productImage);

            result.add(itemData);
        }
        return result;
    }

    /**
     * 取得訂單退貨資訊（AJAX）
     * 用於訂單Modal中動態載入退貨資訊
     * 自行組裝 Map 回傳給前端
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

        try {
            // 使用 getReturnOrderByOrderId 取得 Optional
            Optional<ReturnOrderVO> returnOrderOpt = returnOrderService.getReturnOrderByOrderId(orderId);

            if (returnOrderOpt.isPresent()) {
                ReturnOrderVO returnOrder = returnOrderOpt.get();
                result.put("success", true);
                result.put("hasReturnOrder", true);
                result.put("returnId", returnOrder.getReturnId());

                // 格式化時間
                String applyTimeStr = "-";
                if (returnOrder.getApplyTime() != null) {
                    applyTimeStr = returnOrder.getApplyTime().format(
                            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                }
                result.put("applyTime", applyTimeStr);

                result.put("returnReason", returnOrder.getReturnReason());
                result.put("refundAmount", returnOrder.getRefundAmount());
                result.put("returnStatus", returnOrder.getReturnStatus());

                // 狀態文字轉換
                String statusText = "未知";
                if (returnOrder.getReturnStatus() != null) {
                    switch (returnOrder.getReturnStatus()) {
                        case 0: statusText = "審核中"; break;
                        case 1: statusText = "退貨通過"; break;
                        case 2: statusText = "退貨失敗"; break;
                    }
                }
                result.put("returnStatusText", statusText);

            } else {
                // 沒有退貨單也回傳 success: false (但不是系統錯誤)
                result.put("success", false);
                result.put("hasReturnOrder", false);
                result.put("error", "查無退貨資料");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    // ==================== 出貨/取消 ====================

    @PostMapping("/order/ship")
    public String shipOrder(@RequestParam Integer orderId, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        Integer sellerId = getCurrentMemId(request);
        if (sellerId == null) return "redirect:/front/loginpage";

        try {
            orderService.shipOrder(sellerId, orderId);
            redirectAttributes.addFlashAttribute("successMessage", "訂單已標記為出貨!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/seller/orders";
    }

    @PostMapping("/order/cancel")
    public String cancelOrder(@RequestParam Integer orderId, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        Integer sellerId = getCurrentMemId(request);
        if (sellerId == null) return "redirect:/front/loginpage";

        try {
            Integer refundAmount = orderService.cancelOrderWithRefund(sellerId, orderId);
            if (refundAmount != null) {
                redirectAttributes.addFlashAttribute("successMessage", "訂單已取消，已退款 $" + refundAmount + " 給買家!");
            } else {
                redirectAttributes.addFlashAttribute("error", "取消失敗，請確認訂單狀態");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/seller/orders";
    }

}