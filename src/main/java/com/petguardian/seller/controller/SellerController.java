package com.petguardian.seller.controller;

import com.petguardian.seller.model.*;
import com.petguardian.orders.model.*;
import com.petguardian.seller.service.*;
import com.petguardian.wallet.model.WalletRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
@Controller
@RequestMapping("/seller")
public class SellerController {

    @Autowired
    private ProductService productService;

    @Autowired
    private SellerOrderService sellerOrderService;

//    @Autowired
//    private MockAuthService mockAuthService;

    @Autowired
    private WalletRepository walletRepository;

    /**
     * 處理 URL 參數切換測試帳號(/seller/dashboard?memId=1001)
     */
//    private void handleMockLogin(HttpSession session, Integer memIdParam) {
//        if (memIdParam != null) {
//            try {
//                mockAuthService.setCurrentMember(session, memIdParam);
//            } catch (IllegalArgumentException e) {
//                // 會員不存在，忽略參數
//            }
//        }
//    }

    /**
     * 賣家管理中心 - 首頁（營運概況）
     * URL: /seller/dashboard
     * 模板: templates/frontend/seller.html
     */
    @GetMapping("/dashboard")
    public String showSellerDashboard(Model model) {
        // TODO: 從 Session 取得登入的賣家 ID
        Integer sellerId = 1001;

        // 查詢賣家的所有商品
        List<Product> allProducts = productService.getSellerProducts(sellerId);

        // 查詢賣家的所有訂單
        List<OrdersVO> allOrders = sellerOrderService.getSellerOrders(sellerId);

        // 統計數據
        long totalProducts = allProducts.size(); // 商品總數
        long activeProducts = allProducts.stream()
                .filter(p -> p.getProState() == 1) // 上架中
                .count();
        long totalOrders = allOrders.size(); // 訂單總數
        long pendingOrders = allOrders.stream()
                .filter(o -> o.getOrderStatus() == 0) // 待出貨
                .count();

        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("activeProducts", activeProducts);
        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("pendingOrders", pendingOrders);
        model.addAttribute("currentView", "overview");

        return "frontend/store-seller";
    }

    /**
     * 賣家管理中心 - 商品管理
     * URL: /seller/products
     * 模板: templates/frontend/seller.html
     */
    @GetMapping("/products")
    public String showSellerProducts(Model model) {
        Integer sellerId = 1001;

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
     * 模板: templates/frontend/seller.html
     */
    @GetMapping("/orders")
    public String showSellerOrders(Model model) {
        Integer sellerId = 1001;

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
            RedirectAttributes redirectAttributes) {

        Integer sellerId = 1001;

        Product product;
        if (proId != null && proId > 0) {
            // 編輯現有商品
            product = productService.getProductById(proId)
                    .orElseThrow(() -> new RuntimeException("商品不存在"));
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

        redirectAttributes.addFlashAttribute("successMessage", "商品儲存成功！");
        return "redirect:/seller/products";
    }

    /**
     * 刪除商品
     * URL: POST /seller/product/delete
     */
    @PostMapping("/product/delete")
    public String deleteProduct(
            @RequestParam Integer proId,
            RedirectAttributes redirectAttributes) {

        productService.deleteProduct(proId);

        redirectAttributes.addFlashAttribute("successMessage", "商品已刪除！");
        return "redirect:/seller/products";
    }

    /**
     * 更新訂單狀態（出貨）
     * URL: POST /seller/order/ship
     */
    @PostMapping("/order/ship")
    public String shipOrder(
            @RequestParam Integer orderId,
            RedirectAttributes redirectAttributes) {

        sellerOrderService.updateOrderStatus(orderId, 1); // 1=已出貨

        redirectAttributes.addFlashAttribute("successMessage", "訂單已標記為已出貨！");
        return "redirect:/seller/orders";
    }
}
