package com.petguardian.orders.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.petguardian.orders.model.ReturnOrderVO;

public interface ReturnOrderService {

    /**
     * 申請退貨
     */
    Map<String, Object> applyReturn(Integer orderId, String returnReason);

    /**
     * 查詢退貨單
     */
    Optional<ReturnOrderVO> getReturnOrderById(Integer returnId);

    /**
     * 根據訂單ID查詢退貨單
     */
    Optional<ReturnOrderVO> getReturnOrderByOrderId(Integer orderId);

    /**
     * 查詢所有退貨單
     */
    List<ReturnOrderVO> getAllReturnOrders();

    /**
     * 查詢買家的退貨單
     */
    List<ReturnOrderVO> getReturnOrdersByBuyerId(Integer buyerMemId);

    /**
     * 更新退貨狀態
     */
    ReturnOrderVO updateReturnStatus(Integer returnId, Integer newStatus);
}