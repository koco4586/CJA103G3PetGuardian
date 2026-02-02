package com.petguardian.booking.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.petguardian.booking.model.BookingOrderRepository;
import com.petguardian.booking.model.BookingOrderVO;

/**
 * 訂單查詢服務
 * 處理所有訂單的查詢操作，包括會員端和保母端的訂單查詢
 */
@Service
@Transactional(readOnly = true)
public class BookingOrderQueryService {

    @Autowired
    private BookingOrderRepository orderRepository;

    @Autowired
    private BookingScheduleInternalService scheduleInternalService;

    @Autowired
    private BookingDataIntegrationService dataService;

    /**
     * 查詢會員的所有訂單
     */
    public List<BookingOrderVO> getOrdersByMemberId(Integer memId) {
        List<BookingOrderVO> list = orderRepository.findByMemId(memId);
        // 借用排程服務的修正邏輯，自動更新已過期但狀態未更新的訂單
        scheduleInternalService.autoUpdateExpiredOrders(list);
        // 訂單相關資訊（會員名稱、寵物名稱等）
        list.forEach(this::enrichOrderInfo);
        return list;
    }

    /**
     * 根據訂單ID查詢單筆訂單
     * 訂單若不存在則返回 null
     */
    public BookingOrderVO getOrderById(Integer orderId) {
        return orderRepository.findById(orderId).orElse(null);
    }

    /**
     * 查詢會員的進行中訂單（狀態為 0:待確認 或 1:進行中）
     */
    public List<BookingOrderVO> getActiveOrdersByMemberId(Integer memId) {
        return orderRepository.findByMemId(memId).stream()
                .filter(o -> o.getOrderStatus() == 0 || o.getOrderStatus() == 1)
                .toList();
    }

    /**
     * 查詢會員特定狀態的訂單
     * 訂單狀態（0:待確認, 1:進行中, 2:已完成, 3:申請退款中, 4:已退款, 5:已撥款）
     */
    public List<BookingOrderVO> findByMemberAndStatus(Integer memId, Integer status) {
        // 先取得會員的所有訂單
        List<BookingOrderVO> allOrders = orderRepository.findByMemId(memId);
        // 自動更新過期訂單的狀態
        scheduleInternalService.autoUpdateExpiredOrders(allOrders);
        // 篩選出指定狀態的訂單
        return allOrders.stream()
                .filter(order -> order.getOrderStatus() != null && order.getOrderStatus().equals(status))
                .toList();
    }

    /**
     * 查詢保母的所有訂單
     * 訂單列表（自動更新過期狀態並補充完整資訊）
     */
    public List<BookingOrderVO> getOrdersBySitterId(Integer sitterId) {
        List<BookingOrderVO> list = orderRepository.findBySitterId(sitterId);
        // 狀態修正：更新已過期的訂單
        scheduleInternalService.autoUpdateExpiredOrders(list);
        // 訂單相關資訊（會員名稱、寵物名稱等）
        list.forEach(this::enrichOrderInfo);
        return list;
    }

    /**
     * 查詢保母特定狀態的訂單
     * 訂單狀態（0:待確認, 1:進行中, 2:已完成, 3:申請退款中, 4:已退款, 5:已撥款）
     */
    public List<BookingOrderVO> findOrdersBySitterAndStatus(Integer sitterId, Integer status) {
        // 直接呼叫 Repository 進行查詢
        List<BookingOrderVO> list = orderRepository.findBySitterIdAndOrderStatus(sitterId, status);
        // 訂單相關資訊（如會員名稱、寵物名稱等）
        list.forEach(this::enrichOrderInfo);
        return list;
    }

    /**
     * 補充訂單的完整資訊
     */
    private void enrichOrderInfo(BookingOrderVO order) {
        // 1. 會員名稱
        if (order.getMemId() != null) {
            var member = dataService.getMemberInfo(order.getMemId());
            order.setMemName(member != null ? member.getMemName() : "未知會員");
        }

        // 2. 寵物名稱
        if (order.getPetId() != null) {
            var pet = dataService.getPetInfo(order.getPetId());
            order.setPetName(pet != null ? pet.getPetName() : "未知寵物");
        }

        // 3. 取消原因預設文字（當訂單被取消但未填寫原因時）
        if (order.getOrderStatus() == 3 && (order.getCancelReason() == null || order.getCancelReason().isBlank())) {
            order.setCancelReason("保母忙碌中，暫時無法接單");
        }
    }
}