package com.petguardian.store.controller;

import com.petguardian.common.service.AuthStrategyService;
import com.petguardian.orders.dto.*;
import com.petguardian.orders.model.OrdersVO;
import com.petguardian.orders.model.StoreMemberRepository;
import com.petguardian.orders.service.OrdersService;
import com.petguardian.seller.model.ProductPicRepository;
import com.petguardian.seller.model.ProductPic;
import com.petguardian.seller.model.Product;
import com.petguardian.store.service.StoreService;
import com.petguardian.productfavoritelist.service.ProductFavoriteListService;
import com.petguardian.orders.service.ReturnOrderService;
import com.petguardian.sellerreview.model.SellerReviewVO;
import com.petguardian.sellerreview.service.SellerReviewService;
import com.petguardian.wallet.model.WalletRepository;
import com.petguardian.seller.model.ProType;
import com.petguardian.seller.model.ProTypeRepository;
import com.petguardian.store.service.ImageCacheService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 商品控制器
 *
 * 負責處理：
 * - 商城首頁 (/store)
 * - 結帳頁面 (/store/checkout)
 * - 購買商品 (/product/{proId}/buy)
 * - 購物車操作 (/cart/*)
 * - 會員中心 - 訂單列表 (/dashboard/orders)
 */
@Controller
public class StoreController {

    @Autowired
    private StoreService productService;

    @Autowired
    private ProductFavoriteListService favoriteService;

    @Autowired
    private OrdersService ordersService;

    @Autowired
    private SellerReviewService reviewService;

    @Autowired
    private ReturnOrderService returnOrderService;

    @Autowired
    private ProductPicRepository productPicDAO;

    @Autowired
    private StoreMemberRepository memberDAO;

    @Autowired
    private AuthStrategyService authService;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private ProTypeRepository proTypeRepository;

    @Autowired
    private ImageCacheService imageCacheService;

    // ==================== 輔助方法 ====================

    /**
     * 取得當前會員 ID（含模擬登入邏輯）
     */
    // private Integer getCurrentMemId(HttpSession session) {
    // Integer memId = (Integer) session.getAttribute("memId");

    /**
     * 取得或建立購物車
     */
    @SuppressWarnings("unchecked")
    private List<CartItem> getOrCreateCart(HttpSession session) {
        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
        if (cart == null) {
            cart = new ArrayList<>();
            session.setAttribute("cart", cart);
        }
        return cart;
    }

    /**
     * 將商品圖片轉換為 Base64 字串（使用快取服務）
     */
    private String getProductImageBase64(Integer proId) {
        return imageCacheService.getProductImageBase64(proId);
    }

    /**
     * 將 ProductVO 轉換為 ProductDisplayDTO
     */
    private ProductDisplayDTO toProductDisplayDTO(Product product, Set<Integer> favoriteIds) {
        ProductDisplayDTO dto = new ProductDisplayDTO();
        dto.setProId(product.getProId());
        dto.setSellerId(product.getMemId());
        dto.setProName(product.getProName());
        dto.setProPrice(product.getProPrice());
        dto.setStockQuantity(product.getStockQuantity());
        dto.setProDescription(product.getProDescription());
        dto.setImageBase64(getProductImageBase64(product.getProId()));
        dto.setFavorited(favoriteIds != null && favoriteIds.contains(product.getProId()));
        // 加入分類資訊
        if (product.getProType() != null) {
            dto.setProTypeId(product.getProType().getProTypeId());
            dto.setProTypeName(product.getProType().getProTypeName());
        }
        return dto;
    }

    /**
     * 取得賣家資訊 DTO（含評分統計與評價列表）
     */
    private SellerInfoDTO getSellerInfoDTO(Integer sellerId) {
        SellerInfoDTO info = new SellerInfoDTO();
        info.setSellerId(sellerId);

        // 取得賣家名稱
        memberDAO.findById(sellerId).ifPresent(member -> info.setSellerName(member.getMemName()));
        if (info.getSellerName() == null) {
            info.setSellerName("賣家 #" + sellerId);
        }

        // 取得評分統計
        Map<String, Object> stats = reviewService.getSellerRatingStats(sellerId);
        info.setAverageRating((Double) stats.get("averageRating"));
        info.setReviewCount((Long) stats.get("reviewCount"));

        // 取得評價列表並轉換為 DTO
        @SuppressWarnings("unchecked")
        List<SellerReviewVO> reviews = (List<SellerReviewVO>) stats.get("reviews");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        List<SellerInfoDTO.ReviewDisplayDTO> reviewDTOs = reviews.stream().map(review -> {
            SellerInfoDTO.ReviewDisplayDTO dto = new SellerInfoDTO.ReviewDisplayDTO();
            dto.setReviewId(review.getReviewId());
            dto.setOrderId(review.getOrderId());
            dto.setRating(review.getRating());
            dto.setReviewContent(review.getReviewContent());
            dto.setReviewTime(review.getReviewTime() != null
                    ? review.getReviewTime().format(formatter)
                    : "");

            // 取得買家名稱
            try {
                Map<String, Object> orderData = ordersService.getOrderWithItems(review.getOrderId());
                OrdersVO order = (OrdersVO) orderData.get("order");
                if (order != null) {
                    memberDAO.findById(order.getBuyerMemId())
                            .ifPresent(buyer -> dto.setBuyerName(buyer.getMemName()));
                }
            } catch (Exception e) {
                // 忽略錯誤
            }
            if (dto.getBuyerName() == null) {
                dto.setBuyerName("匿名買家");
            }
            return dto;
        }).collect(Collectors.toList());

        info.setReviews(reviewDTOs);
        return info;
    }

    // ==================== 商城頁面 ====================

    /**
     * 商城首頁
     * GET /store
     */
    @GetMapping("/store")
    public String storePage(@RequestParam(required = false) Integer categoryId,
            Model model, HttpSession session, HttpServletRequest request) {
        Integer memId = authService.getCurrentUserId(request);

        // 取得所有上架商品
        List<Product> products = productService.getAllActiveProducts();

        // 如果有指定分類，進行篩選
        if (categoryId != null) {
            products = products.stream()
                    .filter(p -> p.getProType() != null && categoryId.equals(p.getProType().getProTypeId()))
                    .collect(Collectors.toList());
        }

        // 取得使用者收藏的商品 ID 集合 (若未登入則為空集合)
        Set<Integer> favoriteIds = (memId != null)
                ? favoriteService.getFavoriteProductIds(memId)
                : Collections.emptySet();

        // 轉換為 ProductDisplayDTO（含 Base64 圖片）
        List<ProductDisplayDTO> productDTOs = products.stream()
                .map(p -> toProductDisplayDTO(p, favoriteIds))
                .collect(Collectors.toList());

        // 取得所有商品分類
        List<ProType> categories = proTypeRepository.findAll();

        model.addAttribute("products", productDTOs);
        model.addAttribute("memId", memId);
        model.addAttribute("categories", categories);
        model.addAttribute("selectedCategoryId", categoryId);

        return "frontend/orders/store";
    }

    /**
     * 進入結帳頁（從商城點擊商品）
     * GET /product/{proId}/buy
     */
    @GetMapping("/product/{proId}/buy")
    public String buyProduct(@PathVariable Integer proId,
            @RequestParam(defaultValue = "1") Integer quantity,
            HttpSession session,
            RedirectAttributes redirectAttr,
            HttpServletRequest request) {
        Integer memId = authService.getCurrentUserId(request);
        if (memId == null) {
            redirectAttr.addFlashAttribute("error", "請先登入");
            return "redirect:/store";
        }

        // 取得商品資訊
        Optional<Product> productOpt = productService.getProductById(proId);
        if (productOpt.isEmpty()) {
            redirectAttr.addFlashAttribute("error", "商品不存在");
            return "redirect:/store";
        }
        Product product = productOpt.get();

        // 驗證庫存
        if (product.getStockQuantity() == null || product.getStockQuantity() < quantity) {
            redirectAttr.addFlashAttribute("error", "「" + product.getProName() + "」庫存不足");
            return "redirect:/store";
        }

        // 清空購物車並加入新商品（直接結帳模式）
        List<CartItem> cart = new ArrayList<>();
        CartItem item = new CartItem(
                product.getProId(),
                product.getMemId(),
                product.getProName(),
                product.getProPrice(),
                quantity);
        cart.add(item);
        session.setAttribute("cart", cart);

        return "redirect:/store/checkout";
    }

    /**
     * 結帳頁面
     * GET /store/checkout
     */
    @GetMapping("/store/checkout")
    public String checkoutPage(Model model, HttpSession session, HttpServletRequest request) {
        Integer memId = authService.getCurrentUserId(request);
        if (memId == null) {
            return "redirect:/store";
        }

        // 取得購物車資料
        List<CartItem> cart = getOrCreateCart(session);

        // 購物車為空導回商城
        if (cart.isEmpty()) {
            return "redirect:/store";
        }

        // 取得賣家 ID
        Integer sellerId = cart.get(0).getSellerId();

        // 取得使用者收藏的商品 ID 集合
        Set<Integer> favoriteIds = favoriteService.getFavoriteProductIds(memId);

        // 建立 CheckoutResponseDTO
        CheckoutResponseDTO checkout = new CheckoutResponseDTO();
        checkout.setMemId(memId);

        // 1. 購物車商品（含 Base64 圖片與庫存）
        List<CheckoutResponseDTO.CartItemDisplayDTO> cartItemDTOs = cart.stream().map(item -> {
            CheckoutResponseDTO.CartItemDisplayDTO dto = new CheckoutResponseDTO.CartItemDisplayDTO();
            dto.setProId(item.getProId());
            dto.setSellerId(item.getSellerId());
            dto.setProName(item.getProName());
            dto.setProPrice(item.getProPrice());
            dto.setQuantity(item.getQuantity());
            dto.setSubtotal(item.getSubtotal());
            dto.setImageBase64(getProductImageBase64(item.getProId()));

            // 取得即時庫存
            productService.getProductById(item.getProId())
                    .ifPresent(p -> dto.setStockQuantity(p.getStockQuantity()));

            return dto;
        }).collect(Collectors.toList());
        checkout.setCartItems(cartItemDTOs);

        // 2. 主商品（購物車第一項）
        if (!cartItemDTOs.isEmpty()) {
            CheckoutResponseDTO.CartItemDisplayDTO mainItem = cartItemDTOs.get(0);
            ProductDisplayDTO mainProduct = new ProductDisplayDTO();
            mainProduct.setProId(mainItem.getProId());
            mainProduct.setSellerId(mainItem.getSellerId());
            mainProduct.setProName(mainItem.getProName());
            mainProduct.setProPrice(mainItem.getProPrice());
            mainProduct.setStockQuantity(mainItem.getStockQuantity());
            mainProduct.setImageBase64(mainItem.getImageBase64());
            mainProduct.setFavorited(favoriteIds.contains(mainItem.getProId()));

            checkout.setMainProduct(mainProduct);
            checkout.setMainQuantity(mainItem.getQuantity());
        }

        // 3. 計算總金額
        int orderTotal = cart.stream()
                .mapToInt(CartItem::getSubtotal)
                .sum();
        checkout.setOrderTotal(orderTotal);

        // 4. 賣家資訊（含評分統計與評價列表）
        SellerInfoDTO sellerInfo = getSellerInfoDTO(sellerId);
        checkout.setSellerInfo(sellerInfo);

        // 5. 同店加購商品
        List<Integer> cartProIds = cart.stream()
                .map(CartItem::getProId)
                .collect(Collectors.toList());
        List<Product> upsellProducts = productService.getOtherActiveProductsBySeller(sellerId, cartProIds);
        List<ProductDisplayDTO> upsellDTOs = upsellProducts.stream()
                .map(p -> toProductDisplayDTO(p, favoriteIds))
                .collect(Collectors.toList());
        checkout.setUpsellProducts(upsellDTOs);

        // 6. 取得會員錢包餘額
        Integer walletBalance = walletRepository.findByMemId(memId)
                .map(wallet -> wallet.getBalance())
                .orElse(0);

        model.addAttribute("checkout", checkout);
        model.addAttribute("sellerId", sellerId);
        model.addAttribute("walletBalance", walletBalance);

        return "frontend/orders/checkout";
    }

    // ==================== 會員中心頁面 ====================

    /**
     * 會員中心 - 訂單列表
     * GET /dashboard/orders
     */
    @GetMapping("/dashboard/orders")
    public String dashboardOrdersPage(@RequestParam(required = false) String filter,
            Model model, HttpSession session, HttpServletRequest request) {
        // 檢查是否已登入
        Integer memId = authService.getCurrentUserId(request);
        if (memId == null) {
            return "redirect:/store";
        }

        List<Map<String, Object>> orders = ordersService.getBuyerOrdersWithItems(memId);

        // 為每個訂單加入 hasReview、returnOrder、canCancel、canApplyReturn 資訊
        for (Map<String, Object> orderData : orders) {
            OrdersVO order = (OrdersVO) orderData.get("order");
            if (order != null) {
                // 檢查是否已評價
                boolean hasReview = reviewService.hasReviewed(order.getOrderId());
                order.setHasReview(hasReview);

                // 取得退貨單資訊（如果有）
                returnOrderService.getReturnOrderByOrderId(order.getOrderId())
                        .ifPresent(returnOrder -> orderData.put("returnOrder", returnOrder));

                // 檢查是否可取消（24小時內）
                boolean canCancel = ordersService.canCancelOrder(order.getOrderId());
                orderData.put("canCancel", canCancel);

                // 檢查是否可申請退貨（72小時內）
                boolean canApplyReturn = ordersService.canApplyReturn(order.getOrderId());
                orderData.put("canApplyReturn", canApplyReturn);
            }
        }

        // 根據篩選條件過濾訂單
        if (filter != null && !filter.isEmpty() && !"all".equals(filter)) {
            orders = orders.stream().filter(orderData -> {
                OrdersVO order = (OrdersVO) orderData.get("order");
                if (order == null)
                    return false;
                int status = order.getOrderStatus();
                switch (filter) {
                    case "processing":
                        return status == 0 || status == 1 || status == 4; // 已付款、已出貨、申請退貨中
                    case "completed":
                        return status == 2 || status == 5; // 已完成、退貨完成
                    case "cancelled":
                        return status == 3; // 已取消
                    default:
                        return true;
                }
            }).collect(Collectors.toList());
        }

        model.addAttribute("orders", orders);
        model.addAttribute("memId", memId);
        model.addAttribute("filter", filter);

        return "frontend/orders/dashboard-orders";
    }

    // ==================== 購物車操作 ====================

    /**
     * 加購商品（從結帳頁面加購區）
     * POST /cart/upsell
     */
    @PostMapping("/cart/upsell")
    public String upsellToCart(@RequestParam Integer proId,
            @RequestParam(defaultValue = "1") Integer quantity,
            HttpSession session,
            RedirectAttributes redirectAttr,
            HttpServletRequest request) {
        Integer memId = authService.getCurrentUserId(request);
        if (memId == null) {
            return "redirect:/store";
        }

        List<CartItem> cart = getOrCreateCart(session);

        // 購物車不應為空（加購需有主商品）
        if (cart.isEmpty()) {
            redirectAttr.addFlashAttribute("error", "請先選擇主商品");
            return "redirect:/store";
        }

        // 取得商品資訊
        Optional<Product> productOpt = productService.getProductById(proId);
        if (productOpt.isEmpty()) {
            redirectAttr.addFlashAttribute("error", "商品不存在");
            return "redirect:/store/checkout";
        }
        Product product = productOpt.get();

        // 驗證是否為同一賣家
        Integer currentSellerId = cart.get(0).getSellerId();
        if (!currentSellerId.equals(product.getMemId())) {
            redirectAttr.addFlashAttribute("error", "只能加購同一賣家的商品");
            return "redirect:/store/checkout";
        }

        // 驗證庫存
        if (product.getStockQuantity() == null || product.getStockQuantity() < quantity) {
            redirectAttr.addFlashAttribute("error", "「" + product.getProName() + "」庫存不足");
            return "redirect:/store/checkout";
        }

        // 檢查是否已在購物車中
        boolean exists = cart.stream().anyMatch(item -> item.getProId().equals(proId));
        if (exists) {
            redirectAttr.addFlashAttribute("message", "此商品已在購物車中");
            return "redirect:/store/checkout";
        }

        // 新增加購商品
        CartItem newItem = new CartItem(proId, product.getMemId(), product.getProName(),
                product.getProPrice(), quantity);
        cart.add(newItem);
        session.setAttribute("cart", cart);

        redirectAttr.addFlashAttribute("message", "已加入加購商品");
        return "redirect:/store/checkout";
    }

    /**
     * 更新購物車商品數量
     * POST /cart/update
     */
    @PostMapping("/cart/update")
    public String updateCartQuantity(@RequestParam Integer proId,
            @RequestParam Integer quantity,
            HttpSession session,
            RedirectAttributes redirectAttr,
            HttpServletRequest request) {
        Integer memId = authService.getCurrentUserId(request);
        if (memId == null) {
            return "redirect:/store";
        }

        List<CartItem> cart = getOrCreateCart(session);

        // 驗證數量
        if (quantity <= 0) {
            // 數量為 0 或負數，移除商品
            cart.removeIf(item -> item.getProId().equals(proId));
            session.setAttribute("cart", cart);
            redirectAttr.addFlashAttribute("message", "已從購物車移除");

            // 若購物車為空導回商城
            if (cart.isEmpty()) {
                return "redirect:/store";
            }
            return "redirect:/store/checkout";
        }

        // 驗證庫存
        Optional<Product> productOpt = productService.getProductById(proId);
        if (productOpt.isEmpty()) {
            redirectAttr.addFlashAttribute("error", "商品不存在");
            return "redirect:/store/checkout";
        }
        Product product = productOpt.get();
        if (product.getStockQuantity() == null || product.getStockQuantity() < quantity) {
            redirectAttr.addFlashAttribute("error", "超過庫存上限（目前庫存：" + product.getStockQuantity() + "）");
            return "redirect:/store/checkout";
        }

        // 更新數量
        cart.stream()
                .filter(item -> item.getProId().equals(proId))
                .findFirst()
                .ifPresent(item -> item.setQuantity(quantity));

        session.setAttribute("cart", cart);
        redirectAttr.addFlashAttribute("message", "已更新數量");
        return "redirect:/store/checkout";
    }

    /**
     * 移除購物車商品
     * POST /cart/remove
     */
    @PostMapping("/cart/remove")
    public String removeFromCart(@RequestParam Integer proId,
            HttpSession session,
            RedirectAttributes redirectAttr) {
        List<CartItem> cart = getOrCreateCart(session);

        cart.removeIf(item -> item.getProId().equals(proId));
        session.setAttribute("cart", cart);

        redirectAttr.addFlashAttribute("message", "已從購物車移除");

        // 若購物車已空，導回商城
        if (cart.isEmpty()) {
            return "redirect:/store";
        }
        return "redirect:/store/checkout";
    }

}
