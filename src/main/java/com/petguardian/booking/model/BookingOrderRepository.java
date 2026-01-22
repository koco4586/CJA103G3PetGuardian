package com.petguardian.booking.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingOrderRepository extends JpaRepository<BookingOrderVO, Integer> {
    
	List<BookingOrderVO> findByOrderStatus(Integer orderStatus);
    // 根據會員 ID 查詢其所有訂單 (之後在個人中心會用到)
    List<BookingOrderVO> findByMemId(Integer memId);
    
    // 根據保姆 ID 查詢其收到的所有預約
    List<BookingOrderVO> findBySitterId(Integer sitterId);
    
}
