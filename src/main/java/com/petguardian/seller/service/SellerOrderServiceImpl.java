package com.petguardian.seller.service;

import com.petguardian.orders.model.OrderItemVO;
import com.petguardian.orders.model.OrderItemRepository;
import com.petguardian.orders.model.OrdersVO;
import com.petguardian.orders.model.OrdersRepository;
import com.petguardian.orders.model.StoreMemberRepository;
import com.petguardian.orders.model.StoreMemberVO;
import com.petguardian.wallet.model.Wallet;
import com.petguardian.wallet.model.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 賣家訂單管理 Service 實作
 */
@Service
@Transactional
public class SellerOrderServiceImpl implements SellerOrderService {

    @Autowired
    private OrdersRepository ordersRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private StoreMemberRepository storeMemberRepository;

    @Autowired
    private WalletRepository walletRepository;

    // 訂單狀態常數
    private static final int STATUS_PAID = 0;       // 已付款
    private static final int STATUS_SHIPPED = 1;    // 已出貨
    private static final int STATUS_COMPLETED = 2;  // 已完成
    private static final int STATUS_CANCELED = 3;   // 已取消

    // ==================== 基本查詢 ====================

    @Override
    @Transactional(readOnly = true)
    public List<OrdersVO> getSellerOrders(Integer sellerMemId) {
        if (sellerMemId == null) {
            return new ArrayList<>();
        }
        return ordersRepository.findBySellerMemIdOrderByOrderTimeDesc(sellerMemId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<OrdersVO> getOrderById(Integer orderId) {
        if (orderId == null) {
            return Optional.empty();
        }
        return ordersRepository.findById(orderId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderItemVO> getOrderItems(Integer orderId) {
        if (orderId == null) {
            return new ArrayList<>();
        }
        return orderItemRepository.findByOrderId(orderId);
    }

    // ==================== 訂單操作 ====================

    @Override
    public OrdersVO updateOrderStatus(Integer orderId, Integer newStatus) {
        OrdersVO order = ordersRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("訂單不存在"));
        order.setOrderStatus(newStatus);
        return ordersRepository.save(order);
    }

    @Override
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

        // 檢查訂單狀態是否為「已付款」
        if (order.getOrderStatus() == null || order.getOrderStatus() != STATUS_PAID) {
            return false;
        }

        // 更新為「已出貨」
        order.setOrderStatus(STATUS_SHIPPED);
        ordersRepository.save(order);

        return true;
    }

    @Override
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

        // 檢查訂單狀態是否為「已付款」
        if (order.getOrderStatus() == null || order.getOrderStatus() != STATUS_PAID) {
            return null;
        }

        // 取得買家錢包
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

        // 更新訂單狀態為「已取消」
        order.setOrderStatus(STATUS_CANCELED);
        ordersRepository.save(order);

        return refundAmount;
    }

    // ==================== 整合查詢 ====================

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getSellerOrdersWithDetails(Integer sellerId) {
        List<Map<String, Object>> result = new ArrayList<>();

        List<OrdersVO> orders = getSellerOrders(sellerId);

        for (OrdersVO order : orders) {
            Map<String, Object> orderData = new HashMap<>();
            orderData.put("order", order);

            // 取得買家名稱
            String buyerName = getBuyerName(order.getBuyerMemId());
            orderData.put("buyerName", buyerName);

            // 判斷是否可出貨（狀態為0）
            boolean canShip = (order.getOrderStatus() != null && order.getOrderStatus() == STATUS_PAID);
            orderData.put("canShip", canShip);

            result.add(orderData);
        }

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getOrderDetail(Integer sellerId, Integer orderId) {
        Map<String, Object> result = new HashMap<>();

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

        result.put("order", order);

        // 取得訂單項目
        List<OrderItemVO> items = orderItemRepository.findByOrderId(orderId);
        result.put("items", items);

        // 取得買家名稱
        String buyerName = getBuyerName(order.getBuyerMemId());
        result.put("buyerName", buyerName);

        return result;
    }

    // ==================== 統計 ====================

    @Override
    @Transactional(readOnly = true)
    public long countPendingShipment(Integer sellerId) {
        List<OrdersVO> orders = getSellerOrders(sellerId);
        return orders.stream()
                .filter(o -> o.getOrderStatus() != null && o.getOrderStatus() == STATUS_PAID)
                .count();
    }

    @Override
    @Transactional(readOnly = true)
    public int calculateTotalRevenue(Integer sellerId) {
        List<OrdersVO> orders = getSellerOrders(sellerId);
        return orders.stream()
                .filter(o -> o.getOrderStatus() != null && o.getOrderStatus() == STATUS_COMPLETED)
                .mapToInt(o -> o.getOrderTotal() != null ? o.getOrderTotal() : 0)
                .sum();
    }

    // ==================== 私有方法 ====================

    /**
     * 根據會員ID取得名稱
     */
    private String getBuyerName(Integer memId) {
        if (memId == null) {
            return "未知買家";
        }
        Optional<StoreMemberVO> memberOpt = storeMemberRepository.findById(memId);
        if (memberOpt.isPresent() && memberOpt.get().getMemName() != null) {
            return memberOpt.get().getMemName();
        }
        return "買家 #" + memId;
    }
}