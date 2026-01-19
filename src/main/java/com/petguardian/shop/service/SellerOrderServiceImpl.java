package com.petguardian.shop.service;

import com.petguardian.orders.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SellerOrderServiceImpl implements SellerOrderService {

    @Autowired
    private OrdersRepository ordersRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Override
    public List<OrdersVO> getSellerOrders(Integer sellerMemId) {
        return ordersRepository.findBySellerMemIdOrderByOrderTimeDesc(sellerMemId);
    }

    @Override
    public Optional<OrdersVO> getOrderById(Integer orderId) {
        return ordersRepository.findById(orderId);
    }

    @Override
    public OrdersVO updateOrderStatus(Integer orderId, Integer newStatus) {
        OrdersVO order = ordersRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("訂單不存在"));
        order.setOrderStatus(newStatus);
        return ordersRepository.save(order);
    }

    @Override
    public List<OrderItemVO> getOrderItems(Integer orderId) {
        return orderItemRepository.findByOrderId(orderId);
    }
}
