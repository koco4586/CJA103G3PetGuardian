package com.petguardian.orders.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

        // 根據訂單狀態查詢所有訂單（後台管理使用）
        List<OrdersVO> findByOrderStatusOrderByOrderTimeDesc(Integer orderStatus);
// ==================== 新增：多狀態查詢 ====================

        /**
         * 根據多個訂單狀態查詢所有訂單
         * 例如：查詢待完成訂單（狀態 0 和 1）
         */
        @Query("SELECT o FROM OrdersVO o WHERE o.orderStatus IN :statuses ORDER BY o.orderTime DESC")
        List<OrdersVO> findByOrderStatusInOrderByOrderTimeDesc(@Param("statuses") List<Integer> statuses);

        /**
         * 查詢待完成訂單（已付款 + 已出貨）
         */
        @Query("SELECT o FROM OrdersVO o WHERE o.orderStatus IN (0, 1) ORDER BY o.orderTime DESC")
        List<OrdersVO> findPendingOrders();

        /**
         * 查詢結案訂單（已完成 + 已取消）
         */
        @Query("SELECT o FROM OrdersVO o WHERE o.orderStatus IN (2, 3) ORDER BY o.orderTime DESC")
        List<OrdersVO> findClosedOrders();

        /**
         * 查詢退貨相關訂單（申請退貨中 + 退貨完成）
         */
        @Query("SELECT o FROM OrdersVO o WHERE o.orderStatus IN (4, 5) ORDER BY o.orderTime DESC")
        List<OrdersVO> findReturnOrders();

        /**
         * 統計各狀態訂單數量
         */
        @Query("SELECT o.orderStatus, COUNT(o) FROM OrdersVO o GROUP BY o.orderStatus")
        List<Object[]> countByOrderStatus();

        /**
         * 查詢已完成但未撥款的訂單（狀態為 2）
         */
        @Query("SELECT o FROM OrdersVO o WHERE o.orderStatus = 2 ORDER BY o.orderTime DESC")
        List<OrdersVO> findCompletedNotPaidOut();

        /**
         * 查詢已撥款的訂單（假設狀態 6 為已撥款）
         */
        @Query("SELECT o FROM OrdersVO o WHERE o.orderStatus = 6 ORDER BY o.orderTime DESC")
        List<OrdersVO> findPaidOutOrders();
}
