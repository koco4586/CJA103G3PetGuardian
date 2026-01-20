package com.petguardian.orders.service;

import com.petguardian.orders.dto.OrderFormDTO;
import com.petguardian.orders.dto.OrderItemDTO;
import com.petguardian.orders.model.OrderItemRepository;
import com.petguardian.orders.model.OrderItemVO;
import com.petguardian.orders.model.OrdersRepository;
import com.petguardian.orders.model.OrdersVO;
import com.petguardian.seller.model.ProductPicRepository;
import com.petguardian.seller.model.ProductPic;
import com.petguardian.seller.model.ProductRepository;
import com.petguardian.seller.model.Product;
import com.petguardian.store.service.StoreService;
import com.petguardian.wallet.model.Wallet;
import com.petguardian.wallet.model.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.Base64;

@Service
@Transactional
public class OrdersServiceImpl implements OrdersService {

    @Autowired
    private OrdersRepository ordersDAO;

    @Autowired
    private OrderItemRepository orderItemDAO;

    @Autowired
    private ProductRepository productDAO;

    @Autowired
    private ProductPicRepository productPicDAO;

    @Autowired
    private StoreService productService;

    @Autowired
    private WalletRepository walletDAO;

    // 訂單狀態常數
    public static final Integer STATUS_PAID = 0; // 已付款
    public static final Integer STATUS_SHIPPED = 1; // 已出貨
    public static final Integer STATUS_COMPLETED = 2; // 已完成
    public static final Integer STATUS_CANCELED = 3; // 已取消
    public static final Integer STATUS_REFUNDING = 4; // 申請退貨中
    public static final Integer STATUS_REFUNDED = 5; // 退貨完成

    // 預設佔位圖（1x1 灰色像素）
    private static final String DEFAULT_IMAGE = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8/5+hHgAHggJ/PchI7wAAAABJRU5ErkJggg==";

    /**
     * 取得商品圖片的 Base64 字串
     */
    private String getProductImageBase64(Integer proId) {
        List<ProductPic> pics = productPicDAO.findByProduct_ProId(proId);
        if (!pics.isEmpty() && pics.get(0).getProPic() != null) {
            byte[] imageBytes = pics.get(0).getProPic();
            return "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(imageBytes);
        }
        return DEFAULT_IMAGE;
    }

    /**
     * 完整結帳流程（Thymeleaf 表單用）
     * 包含：驗證庫存 → 建立訂單 → 扣除庫存 → Mock 錢包扣款
     */
    @Override
    @Transactional
    public OrdersVO checkout(Integer buyerMemId, OrderFormDTO form) {
        // 1. 參數驗證
        if (buyerMemId == null) {
            throw new IllegalArgumentException("買家會員 ID 不能為 null");
        }
        if (form == null) {
            throw new IllegalArgumentException("結帳表單不能為 null");
        }
        if (form.getItems() == null || form.getItems().isEmpty()) {
            throw new IllegalArgumentException("訂單項目不能為空");
        }
        if (form.getSellerId() == null) {
            throw new IllegalArgumentException("賣家會員 ID 不能為 null");
        }

        // 2. 驗證庫存
        for (OrderItemDTO item : form.getItems()) {
            Product product = productService.getProductById(item.getProId())
                    .orElseThrow(() -> new IllegalArgumentException("商品不存在: " + item.getProId()));

            if (product.getStockQuantity() == null || product.getStockQuantity() < item.getQuantity()) {
                throw new IllegalArgumentException(
                        "商品「" + product.getProName() + "」庫存不足");
            }
        }

        // 3. 計算總金額（無運費）
        Integer total = form.getItems().stream()
                .mapToInt(item -> {
                    Integer price = item.getProPrice();
                    if (price == null || price <= 0) {
                        // Fallback: 從資料庫取得價格
                        Product product = productDAO.findById(item.getProId()).orElse(null);
                        if (product != null) {
                            price = product.getProPrice();
                            item.setProPrice(price);
                        } else {
                            price = 0;
                        }
                    }
                    return price * item.getQuantity();
                })
                .sum();

        // 4. 錢包餘額檢查
        Wallet buyerWallet = walletDAO.findByMemId(buyerMemId)
                .orElseThrow(() -> new IllegalArgumentException("錢包不存在，請先開通錢包"));

        if (buyerWallet.getBalance() < total) {
            throw new IllegalArgumentException("錢包餘額不足（目前餘額：$" + buyerWallet.getBalance() + "，需支付：$" + total + "）");
        }

        // 5. 建立訂單
        OrdersVO order = new OrdersVO();
        order.setBuyerMemId(buyerMemId);
        order.setSellerMemId(form.getSellerId());
        order.setOrderTotal(total);
        order.setPaymentMethod(0); // 強制錢包付款
        order.setOrderStatus(STATUS_PAID);
        order.setReceiverName(form.getReceiverName());
        order.setReceiverPhone(form.getReceiverPhone());
        order.setReceiverAddress(form.getReceiverAddress());
        order.setSpecialInstructions(form.getSpecialInstructions());

        OrdersVO savedOrder = ordersDAO.save(order);

        // 6. 儲存訂單項目
        for (OrderItemDTO item : form.getItems()) {
            OrderItemVO orderItem = new OrderItemVO();
            orderItem.setOrderId(savedOrder.getOrderId());
            orderItem.setProId(item.getProId());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setProPrice(item.getProPrice());
            orderItemDAO.save(orderItem);
        }

        // 7. 扣除庫存
        for (OrderItemDTO item : form.getItems()) {
            productService.deductStock(item.getProId(), item.getQuantity());
        }

        // 8. 錢包扣款（買家扣款，賣家收款）
        buyerWallet.setBalance(buyerWallet.getBalance() - total);
        walletDAO.save(buyerWallet);

        // 賣家收款
        Wallet sellerWallet = walletDAO.findByMemId(form.getSellerId()).orElse(null);
        if (sellerWallet != null) {
            sellerWallet.setBalance(sellerWallet.getBalance() + total);
            walletDAO.save(sellerWallet);
        }

        return savedOrder;
    }

    @Override
    public Map<String, Object> createOrderWithItems(OrdersVO order, List<OrderItemVO> orderItems) {
        // 驗證
        if (order == null) {
            throw new IllegalArgumentException("訂單資訊不能為 null");
        }
        if (orderItems == null || orderItems.isEmpty()) {
            throw new IllegalArgumentException("訂單項目不能為空");
        }

        // 計算總金額（不含運費）
        Integer total = orderItems.stream()
                .mapToInt(item -> {
                    Integer price = item.getProPrice();
                    if (price == null || price <= 0) {
                        // Fallback: fetch price from DB
                        Product product = productDAO.findById(item.getProId()).orElse(null);
                        if (product != null) {
                            price = product.getProPrice();
                            item.setProPrice(price); // Update item with correct price
                        } else {
                            price = 0; // Product not found
                        }
                    }
                    return price * item.getQuantity();
                })
                .sum();

        // 不再加運費
        order.setOrderTotal(total);
        order.setOrderStatus(STATUS_PAID);

        // 儲存訂單
        OrdersVO savedOrder = ordersDAO.save(order);

        // 儲存訂單項目
        List<OrderItemVO> savedItems = new ArrayList<>();
        for (OrderItemVO item : orderItems) {
            item.setOrderId(savedOrder.getOrderId());
            savedItems.add(orderItemDAO.save(item));
        }

        // 組裝結果
        Map<String, Object> result = new HashMap<>();
        result.put("order", savedOrder);
        result.put("orderItems", savedItems);
        result.put("itemCount", savedItems.size());
        result.put("orderTotal", total);

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getOrderWithItems(Integer orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException("訂單ID不能為 null");
        }

        OrdersVO order = ordersDAO.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("訂單不存在: " + orderId));

        List<OrderItemVO> items = orderItemDAO.findByOrderId(orderId);

        // 加入商品資訊（標題、圖片）
        List<Map<String, Object>> itemsWithProduct = new ArrayList<>();
        for (OrderItemVO item : items) {
            Map<String, Object> itemData = new HashMap<>();
            itemData.put("orderItemId", item.getOrderItemId());
            itemData.put("orderId", item.getOrderId());
            itemData.put("proId", item.getProId());
            itemData.put("quantity", item.getQuantity());
            itemData.put("proPrice", item.getProPrice());
            itemData.put("subtotal", item.getSubtotal());

            // 從 product 表取得商品資訊
            Product product = productDAO.findById(item.getProId()).orElse(null);
            if (product != null) {
                itemData.put("productTitle", product.getProName());
                itemData.put("productImg", getProductImageBase64(item.getProId()));
            } else {
                itemData.put("productTitle", "商品已下架");
                itemData.put("productImg", DEFAULT_IMAGE);
            }

            itemsWithProduct.add(itemData);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("order", order);
        result.put("orderItems", itemsWithProduct);
        result.put("itemCount", items.size());

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getBuyerOrdersWithItems(Integer buyerMemId) {
        if (buyerMemId == null) {
            throw new IllegalArgumentException("買家會員ID不能為 null");
        }

        List<OrdersVO> orders = ordersDAO.findByBuyerMemIdOrderByOrderTimeDesc(buyerMemId);
        List<Map<String, Object>> result = new ArrayList<>();

        for (OrdersVO order : orders) {
            Map<String, Object> orderData = new HashMap<>();
            List<OrderItemVO> items = orderItemDAO.findByOrderId(order.getOrderId());

            // 加入商品資訊（標題、圖片）
            List<Map<String, Object>> itemsWithProduct = new ArrayList<>();
            for (OrderItemVO item : items) {
                Map<String, Object> itemData = new HashMap<>();
                itemData.put("orderItemId", item.getOrderItemId());
                itemData.put("orderId", item.getOrderId());
                itemData.put("proId", item.getProId());
                itemData.put("quantity", item.getQuantity());
                itemData.put("proPrice", item.getProPrice());
                itemData.put("subtotal", item.getSubtotal());

                // 從 product 表取得商品資訊
                Product product = productDAO.findById(item.getProId()).orElse(null);
                if (product != null) {
                    itemData.put("productTitle", product.getProName());
                    itemData.put("productImg", getProductImageBase64(item.getProId()));
                } else {
                    itemData.put("productTitle", "商品已下架");
                    itemData.put("productImg", DEFAULT_IMAGE);
                }

                itemsWithProduct.add(itemData);
            }

            orderData.put("order", order);
            orderData.put("orderItems", itemsWithProduct);
            orderData.put("itemCount", items.size());
            result.add(orderData);
        }

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getSellerOrdersWithItems(Integer sellerMemId) {
        if (sellerMemId == null) {
            throw new IllegalArgumentException("賣家會員ID不能為 null");
        }

        List<OrdersVO> orders = ordersDAO.findBySellerMemIdOrderByOrderTimeDesc(sellerMemId);
        List<Map<String, Object>> result = new ArrayList<>();

        for (OrdersVO order : orders) {
            Map<String, Object> orderData = new HashMap<>();
            List<OrderItemVO> items = orderItemDAO.findByOrderId(order.getOrderId());

            // 加入商品資訊（標題、圖片）
            List<Map<String, Object>> itemsWithProduct = new ArrayList<>();
            for (OrderItemVO item : items) {
                Map<String, Object> itemData = new HashMap<>();
                itemData.put("orderItemId", item.getOrderItemId());
                itemData.put("orderId", item.getOrderId());
                itemData.put("proId", item.getProId());
                itemData.put("quantity", item.getQuantity());
                itemData.put("proPrice", item.getProPrice());
                itemData.put("subtotal", item.getSubtotal());

                // 從 product 表取得商品資訊
                Product product = productDAO.findById(item.getProId()).orElse(null);
                if (product != null) {
                    itemData.put("productTitle", product.getProName());
                    itemData.put("productImg", getProductImageBase64(item.getProId()));
                } else {
                    itemData.put("productTitle", "商品已下架");
                    itemData.put("productImg", DEFAULT_IMAGE);
                }

                itemsWithProduct.add(itemData);
            }

            orderData.put("order", order);
            orderData.put("orderItems", itemsWithProduct);
            orderData.put("itemCount", items.size());
            result.add(orderData);
        }

        return result;
    }

    @Override
    public OrdersVO updateOrderStatus(Integer orderId, Integer newStatus) {
        if (orderId == null) {
            throw new IllegalArgumentException("訂單ID不能為 null");
        }
        if (newStatus == null) {
            throw new IllegalArgumentException("新狀態不能為 null");
        }

        OrdersVO order = ordersDAO.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("訂單不存在: " + orderId));

        order.setOrderStatus(newStatus);
        return ordersDAO.save(order);
    }

    // ==================== 訂單項目基本操作 ====================

    @Override
    @Transactional(readOnly = true)
    public List<OrderItemVO> getOrderItems(Integer orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException("訂單ID不能為 null");
        }
        return orderItemDAO.findByOrderId(orderId);
    }

    @Override
    public OrderItemVO addOrderItem(OrderItemVO orderItem) {
        if (orderItem == null) {
            throw new IllegalArgumentException("訂單項目不能為 null");
        }
        if (orderItem.getOrderId() == null) {
            throw new IllegalArgumentException("訂單ID不能為 null");
        }

        // 驗證訂單是否存在
        ordersDAO.findById(orderItem.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("訂單不存在"));

        // 儲存項目
        OrderItemVO savedItem = orderItemDAO.save(orderItem);

        // 更新訂單總金額
        Integer newTotal = calculateOrderTotal(orderItem.getOrderId());
        OrdersVO order = ordersDAO.findById(orderItem.getOrderId()).get();
        order.setOrderTotal(newTotal);
        ordersDAO.save(order);

        return savedItem;
    }

    @Override
    @Transactional(readOnly = true)
    public Integer calculateOrderTotal(Integer orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException("訂單ID不能為 null");
        }

        Integer total = orderItemDAO.calculateOrderTotal(orderId);
        // 不再加運費
        return total != null ? total : 0;
    }

    // ==================== 取消訂單與退款 ====================

    @Override
    public OrdersVO cancelOrderWithRefund(Integer orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException("訂單ID不能為 null");
        }

        OrdersVO order = ordersDAO.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("訂單不存在: " + orderId));

        // 檢查訂單狀態（只有已付款狀態才能取消）
        if (!order.getOrderStatus().equals(STATUS_PAID)) {
            throw new IllegalArgumentException("此訂單狀態無法取消");
        }

        // 檢查是否在24小時內
        if (!canCancelOrder(orderId)) {
            throw new IllegalArgumentException("已超過取消期限（下單後24小時內可取消）");
        }

        // 更新訂單狀態為已取消
        order.setOrderStatus(STATUS_CANCELED);
        ordersDAO.save(order);

        // 退款到買家錢包
        refundToBuyerWallet(orderId);

        return order;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canCancelOrder(Integer orderId) {
        if (orderId == null) {
            return false;
        }

        OrdersVO order = ordersDAO.findById(orderId).orElse(null);
        if (order == null) {
            return false;
        }

        // 只有已付款狀態才能取消
        if (!order.getOrderStatus().equals(STATUS_PAID)) {
            return false;
        }

        // 檢查是否在下單後24小時內
        LocalDateTime orderTime = order.getOrderTime();
        if (orderTime == null) {
            return false;
        }

        long hoursSinceOrder = ChronoUnit.HOURS.between(orderTime, LocalDateTime.now());
        return hoursSinceOrder <= 24;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canApplyReturn(Integer orderId) {
        if (orderId == null) {
            return false;
        }

        OrdersVO order = ordersDAO.findById(orderId).orElse(null);
        if (order == null) {
            return false;
        }

        // 只有已完成狀態才能申請退貨
        if (!order.getOrderStatus().equals(STATUS_COMPLETED)) {
            return false;
        }

        // 檢查是否在訂單完成後24小時內（這裡使用 orderTime + 假設完成時間為近期）
        // 由於沒有完成時間欄位，使用 orderTime 作為基準
        LocalDateTime orderTime = order.getOrderTime();
        if (orderTime == null) {
            return false;
        }

        // 假設從下單到完成至少需要一天，所以用 orderTime + 1天 作為完成時間基準
        // 在實際情況中，應該記錄完成時間
        long hoursSinceOrder = ChronoUnit.HOURS.between(orderTime, LocalDateTime.now());
        // 下單後1-3天內可申請退貨（假設1天出貨+配送，所以給予更寬鬆的時間）
        return hoursSinceOrder <= 72; // 3天內可申請退貨
    }

    @Override
    public void refundToBuyerWallet(Integer orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException("訂單ID不能為 null");
        }

        OrdersVO order = ordersDAO.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("訂單不存在: " + orderId));

        Integer refundAmount = order.getOrderTotal();
        Integer buyerMemId = order.getBuyerMemId();
        Integer sellerMemId = order.getSellerMemId();

        // 退款到買家錢包
        Wallet buyerWallet = walletDAO.findByMemId(buyerMemId).orElse(null);
        if (buyerWallet != null) {
            buyerWallet.setBalance(buyerWallet.getBalance() + refundAmount);
            walletDAO.save(buyerWallet);
        }

        // 從賣家錢包扣除（如果賣家已收到款項）
        Wallet sellerWallet = walletDAO.findByMemId(sellerMemId).orElse(null);
        if (sellerWallet != null && sellerWallet.getBalance() >= refundAmount) {
            sellerWallet.setBalance(sellerWallet.getBalance() - refundAmount);
            walletDAO.save(sellerWallet);
        }
    }
}
