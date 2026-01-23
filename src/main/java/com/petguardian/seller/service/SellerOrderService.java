package com.petguardian.seller.service;

import com.petguardian.orders.model.OrderItemVO;
import com.petguardian.orders.model.OrdersVO;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 賣家訂單管理 Service Interface
 *
 * 訂單查詢、出貨、取消退款、訂單詳情
 */
public interface SellerOrderService {

    // ==================== 基本查詢 ====================

    /**
     * 取得賣家的所有訂單（按時間降序）
     */
    List<OrdersVO> getSellerOrders(Integer sellerMemId);

    /**
     * 根據訂單ID取得訂單
     */
    Optional<OrdersVO> getOrderById(Integer orderId);

    /**
     * 取得訂單的所有商品項目
     */
    List<OrderItemVO> getOrderItems(Integer orderId);

    // ==================== 訂單操作 ====================

    /**
     * 更新訂單狀態
     */
    OrdersVO updateOrderStatus(Integer orderId, Integer newStatus);

    /**
     * 賣家出貨（狀態 0 -> 1）
     * 回傳 true 成功，false 失敗（非該賣家訂單或狀態不對）
     */
    boolean shipOrder(Integer sellerId, Integer orderId);

    /**
     * 賣家取消訂單並退款給買家
     * 回傳退款金額，失敗回傳 null
     */
    Integer cancelOrderWithRefund(Integer sellerId, Integer orderId);

    // ==================== 整合查詢（給 Controller 用） ====================

    /**
     * 取得賣家訂單列表（含買家名稱、是否可出貨）
     * 回傳 List<Map>，每個 Map 包含：
     * - order: OrdersVO
     * - buyerName: 買家名稱
     * - canShip: 是否可出貨（狀態為0）
     */
    List<Map<String, Object>> getSellerOrdersWithDetails(Integer sellerId);

    /**
     * 取得訂單詳情（含商品資訊）
     * 回傳 Map 包含：
     * - order: OrdersVO
     * - items: List<OrderItemVO>
     * - buyerName: 買家名稱
     */
    Map<String, Object> getOrderDetail(Integer sellerId, Integer orderId);

    // ==================== 統計 ====================

    /**
     * 計算待出貨訂單數量（狀態為0）
     */
    long countPendingShipment(Integer sellerId);

    /**
     * 計算已完成訂單總營收
     */
    int calculateTotalRevenue(Integer sellerId);
}