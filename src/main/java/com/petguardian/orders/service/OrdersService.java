package com.petguardian.orders.service;

import com.petguardian.orders.dto.OrderFormDTO;
import com.petguardian.orders.model.OrderItemVO;
import com.petguardian.orders.model.OrdersVO;

import java.util.List;
import java.util.Map;

public interface OrdersService {

//    驗證庫存 → 建立訂單 → 扣除庫存 → Mock 錢包扣款
    OrdersVO checkout(Integer buyerMemId, OrderFormDTO form);

    // 建立訂單（含訂單項目）- 保留供相容使用
    Map<String, Object> createOrderWithItems(OrdersVO order, List<OrderItemVO> orderItems);

    // 查詢訂單（含訂單項目）
    Map<String, Object> getOrderWithItems(Integer orderId);

    // 查詢買家所有訂單
    List<Map<String, Object>> getBuyerOrdersWithItems(Integer buyerMemId);

    // 查詢賣家所有訂單
    List<Map<String, Object>> getSellerOrdersWithItems(Integer sellerMemId);

    // 更新訂單狀態
    OrdersVO updateOrderStatus(Integer orderId, Integer newStatus);

    // 查詢訂單的所有商品項目
    List<OrderItemVO> getOrderItems(Integer orderId);

    // 新增訂單商品項目
    OrderItemVO addOrderItem(OrderItemVO orderItem);

    // 計算訂單總金額（不含運費）
    Integer calculateOrderTotal(Integer orderId);

    // 取消訂單並退款到買家錢包（含一天期限檢查）
    OrdersVO cancelOrderWithRefund(Integer orderId);

    // 檢查訂單是否可取消（下單後24小時內）
    boolean canCancelOrder(Integer orderId);

    // 檢查訂單是否可申請退貨（收貨後24小時內）
    boolean canApplyReturn(Integer orderId);

    // 退貨審核通過後退款到買家錢包
    void refundToBuyerWallet(Integer orderId);
}