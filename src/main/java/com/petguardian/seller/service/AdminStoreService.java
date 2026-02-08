package com.petguardian.seller.service;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

public interface AdminStoreService {

    List<Map<String, Object>> getPendingOrders();

    List<Map<String, Object>> getClosedOrders();

    List<Map<String, Object>> getReturnOrders();

    List<Map<String, Object>> getReturnOrdersWithDetails();

    Map<String, Object> getReturnOrderDetail(Integer returnId);

    void approveReturn(Integer returnId);

    void rejectReturn(Integer returnId);

    void payoutToSeller(Integer orderId);

    @Transactional(readOnly = true)
    Map<String, Object> getOrderDetailForAdmin(Integer orderId);

}