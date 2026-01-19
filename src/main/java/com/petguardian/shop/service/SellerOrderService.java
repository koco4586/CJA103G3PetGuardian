package com.petguardian.shop.service;

import com.petguardian.orders.model.*;
import java.util.List;
import java.util.Optional;

public interface SellerOrderService {

    List<OrdersVO> getSellerOrders(Integer sellerMemId);
    Optional<OrdersVO> getOrderById(Integer orderId);
    OrdersVO updateOrderStatus(Integer orderId, Integer newStatus);
    List<OrderItemVO> getOrderItems(Integer orderId);
}
