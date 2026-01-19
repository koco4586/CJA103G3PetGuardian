package com.petguardian.orders.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrdersRepository extends JpaRepository<OrdersVO, Integer> {

        // 查詢買家的所有訂單
        List<OrdersVO> findByBuyerMemIdOrderByOrderTimeDesc(Integer buyerMemId);

        // 查詢賣家的所有訂單
        List<OrdersVO> findBySellerMemIdOrderByOrderTimeDesc(Integer sellerMemId);

        // 根據訂單狀態查詢買家訂單
        List<OrdersVO> findByBuyerMemIdAndOrderStatusOrderByOrderTimeDesc(
                        Integer buyerMemId, Integer orderStatus);

        // 根據訂單狀態查詢賣家訂單
        List<OrdersVO> findBySellerMemIdAndOrderStatusOrderByOrderTimeDesc(
                        Integer sellerMemId, Integer orderStatus);

    List<OrdersVO> findByOrderStatusOrderByOrderTimeDesc(int i);
}