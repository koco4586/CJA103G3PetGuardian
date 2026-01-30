package com.petguardian.booking.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.petguardian.booking.model.BookingOrderRepository;
import com.petguardian.booking.model.BookingOrderVO;
import com.petguardian.booking.model.BookingScheduleRepository;
import com.petguardian.booking.model.BookingScheduleVO;

/**
 * 負責處理預約訂單的建立、退款審核、撥款流程以及同步更新保母排程。
 */

public interface BookingService {

	// 會員端
	List<BookingOrderVO> getOrdersByMemberId(Integer memId);

	List<BookingOrderVO> getActiveOrdersByMemberId(Integer memId);

	List<BookingOrderVO> findByMemberAndStatus(Integer memId, Integer status);

	BookingOrderVO getOrderById(Integer orderId);

	BookingOrderVO createBooking(BookingOrderVO order);

	void cancelBooking(Integer orderId, String reason);

	// 保母端
	List<BookingOrderVO> getOrdersBySitterId(Integer sitterId);

	List<BookingOrderVO> findBySitterAndStatus(Integer sitterId, Integer status);

	/** 查詢某保母特定狀態的訂單 (例如待確認) */
	List<BookingOrderVO> findOrdersBySitterAndStatus(Integer sitterId, Integer status);

	void updateOrderStatusBySitter(Integer orderId, Integer newStatus);

	// 管理員端
	void approveRefund(Integer orderId, Double ratio);

	void completePayout(Integer orderId);
	
	//收藏
	boolean toggleSitterFavorite(Integer memId, Integer sitterId);

	
	List<com.petguardian.booking.model.BookingFavoriteVO> getSitterFavoritesByMember(Integer memId);
}