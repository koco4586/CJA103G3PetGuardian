package com.petguardian.booking.model;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingOrderRepository extends JpaRepository<BookingOrderVO, Integer> {

    List<BookingOrderVO> findByOrderStatus(Integer orderStatus);

    // 根據會員 ID 查詢其所有訂單 (之後在個人中心會用到)
    List<BookingOrderVO> findByMemId(Integer memId);

    // 根據保姆 ID 查詢其收到的所有預約
    List<BookingOrderVO> findBySitterId(Integer sitterId);

    // 支援時間區間查詢
    List<BookingOrderVO> findBySitterIdAndStartTimeBetween(Integer sitterId, LocalDateTime start, LocalDateTime end);

    // 查詢某保姆的所有歷史評價 (只查有評分的訂單), 排序: 依結束時間由新到舊
    List<BookingOrderVO> findBySitterIdAndSitterRatingNotNullOrderByEndTimeDesc(Integer sitterId);

    /**
     * 計算指定訂單狀態的訂單數量
     * 用於統計待處理退款數量（orderStatus = 3 表示申請退款中）
     */
    long countByOrderStatus(Integer orderStatus);
}
