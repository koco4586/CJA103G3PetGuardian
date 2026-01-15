package com.orders.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrdersDAO extends JpaRepository<Orders, Integer> {

        // 查詢買家的所有訂單
        List<Orders> findByBuyerMemIdOrderByOrderTimeDesc(Integer buyerMemId);

        // 查詢賣家的所有訂單
        List<Orders> findBySellerMemIdOrderByOrderTimeDesc(Integer sellerMemId);

        // 根據訂單狀態查詢買家訂單
        List<Orders> findByBuyerMemIdAndOrderStatusOrderByOrderTimeDesc(
                        Integer buyerMemId, Integer orderStatus);

        // 根據訂單狀態查詢賣家訂單
        List<Orders> findBySellerMemIdAndOrderStatusOrderByOrderTimeDesc(
                        Integer sellerMemId, Integer orderStatus);
}