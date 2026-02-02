package com.petguardian.booking.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.petguardian.booking.model.BookingFavoriteVO;
import com.petguardian.booking.model.BookingOrderVO;

/**
 * 預約服務
 * 職責：統一的服務入口，將請求委託給對應的子服務處理
 * - 對外提供統一的介面（實作 BookingService 介面）
 * - 委託給多個專門的子服務處理實際業務邏輯
 */
@Service
@Transactional
public class BookingServiceImpl implements BookingService {

    // 訂單查詢服務 
    @Autowired
    private BookingOrderQueryService orderQueryService;

    // 訂單建立服務
    @Autowired
    private BookingCreateService createService;

    // 退款服務 
    @Autowired
    private BookingRefundService refundService;

    // 撥款服務
    @Autowired
    private BookingPayoutService payoutService;

    // 收藏服務 
    @Autowired
    private BookingFavoriteService favoriteService;

    // 保母端操作服務 
    @Autowired
    private BookingSitterOperationService sitterOperationService;

    // 訂單查詢服務

    /**
     * 查詢會員的所有訂單
     */
    @Override
    public List<BookingOrderVO> getOrdersByMemberId(Integer memId) {
        return orderQueryService.getOrdersByMemberId(memId);
    }

    /**
     * 根據訂單ID查詢單筆訂單
     */
    @Override
    public BookingOrderVO getOrderById(Integer orderId) {
        return orderQueryService.getOrderById(orderId);
    }

    /**
     * 查詢會員的進行中訂單
     */
    @Override
    public List<BookingOrderVO> getActiveOrdersByMemberId(Integer memId) {
        return orderQueryService.getActiveOrdersByMemberId(memId);
    }

    /**
     * 查詢會員特定狀態的訂單
     */
    @Override
    public List<BookingOrderVO> findByMemberAndStatus(Integer memId, Integer status) {
        return orderQueryService.findByMemberAndStatus(memId, status);
    }

    // 訂單建立 

    /**
     * 建立預約訂單
     */
    @Override
    public BookingOrderVO createBooking(BookingOrderVO order) {
        return createService.createBooking(order);
    }

    // 訂單取消與退款 

    /**
     * 取消訂單並處理退款
     */
    @Override
    public void cancelBooking(Integer orderId, String reason) {
        refundService.cancelBooking(orderId, reason);
    }

    /**
     * 核准退款（管理員操作）
     */
    @Override
    public void approveRefund(Integer orderId, Double ratio) {
        refundService.approveRefund(orderId, ratio);
    }

    /**
     * 計算退款比例
     */
    @Override
    public Double calculateRefundRatio(LocalDateTime startTime, LocalDateTime cancelTime) {
        return refundService.calculateRefundRatio(startTime, cancelTime);
    }

    // 訂單查詢服務和保母操作服務

    /**
     * 查詢保母的所有訂單
     */
    @Override
    public List<BookingOrderVO> getOrdersBySitterId(Integer sitterId) {
        return orderQueryService.getOrdersBySitterId(sitterId);
    }

    /**
     * 查詢保母特定狀態的訂單
     */
    @Override
    public List<BookingOrderVO> findOrdersBySitterAndStatus(Integer sitterId, Integer status) {
        return orderQueryService.findOrdersBySitterAndStatus(sitterId, status);
    }

    /**
     * 保母更新訂單狀態
     */
    @Override
    public void updateOrderStatusBySitter(Integer orderId, Integer newStatus) {
        sitterOperationService.updateOrderStatusBySitter(orderId, newStatus);
    }

    // 撥款服務

    /**
     * 完成撥款（後台操作）
     */
    @Override
    public void completePayout(Integer orderId) {
        payoutService.completePayout(orderId);
    }

    // 收藏功能 

    /**
     * 切換收藏狀態
     */
    @Override
    public boolean toggleSitterFavorite(Integer memId, Integer sitterId) {
        return favoriteService.toggleSitterFavorite(memId, sitterId);
    }

    /**
     * 取得會員的所有收藏保母列表
     */
    @Override
    public List<BookingFavoriteVO> getSitterFavoritesByMember(Integer memId) {
        return favoriteService.getSitterFavoritesByMember(memId);
    }
}