package com.petguardian.orders.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItemVO, Integer> {

    // 查詢指定訂單的所有商品項目
    List<OrderItemVO> findByOrderId(Integer orderId);

    // 查詢指定商品在所有訂單中的項目
    List<OrderItemVO> findByProId(Integer proId);

    // 計算指定訂單的總金額
    @Query("SELECT SUM(oi.proPrice * oi.quantity) FROM OrderItemVO oi WHERE oi.orderId = :orderId")
    Integer calculateOrderTotal(@Param("orderId") Integer orderId);

    // 查詢訂單的商品項目數量
    long countByOrderId(Integer orderId);
}
