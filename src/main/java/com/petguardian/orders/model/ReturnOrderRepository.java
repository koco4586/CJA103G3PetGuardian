package com.petguardian.orders.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReturnOrderRepository extends JpaRepository<ReturnOrderVO, Integer> {

//    根據訂單ID查詢退貨單
    Optional<ReturnOrderVO> findByOrderId(Integer orderId);

//    查詢指定狀態的退貨單
    List<ReturnOrderVO> findByReturnStatusOrderByApplyTimeDesc(Integer returnStatus);

//    檢查訂單是否已有退貨申請
    boolean existsByOrderId(Integer orderId);

//    查詢所有退貨單（依申請時間降序）
    List<ReturnOrderVO> findAllByOrderByApplyTimeDesc();

//    根據買家ID查詢退貨單（透過 JOIN）
    @Query("SELECT r FROM ReturnOrderVO r JOIN OrdersVO o ON r.orderId = o.orderId WHERE o.buyerMemId = :buyerMemId ORDER BY r.applyTime DESC")
    List<ReturnOrderVO> findByBuyerMemId(@Param("buyerMemId") Integer buyerMemId);

//    根據賣家ID查詢退貨單（透過 JOIN）
    @Query("SELECT r FROM ReturnOrderVO r JOIN OrdersVO o ON r.orderId = o.orderId WHERE o.sellerMemId = :sellerMemId ORDER BY r.applyTime DESC")
    List<ReturnOrderVO> findBySellerMemId(@Param("sellerMemId") Integer sellerMemId);

    /**
     * 計算指定退貨狀態的退貨單數量
     * return_status: 0=審核中, 1=退貨通過, 2=退貨失敗
     */
    long countByReturnStatus(Integer returnStatus);
}
