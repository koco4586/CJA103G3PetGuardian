package com.petguardian.booking.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.petguardian.booking.model.BookingOrderRepository;
import com.petguardian.booking.model.BookingOrderVO;
import com.petguardian.sitter.model.SitterRepository;
import com.petguardian.sitter.model.SitterVO;

/**
 * 撥款服務
 * 職責：處理訂單完成後的保母撥款流程
 */
@Service
@Transactional
public class BookingPayoutService {

    @Autowired
    private BookingOrderRepository orderRepository;

    @Autowired
    private SitterRepository sitterRepository;

    @Autowired
    private BookingDataIntegrationService dataService;

    /**
     * 完成撥款（後台管理員操作）
     * 處理流程：
     * 1. 查詢訂單是否存在
     * 2. 驗證訂單狀態必須為「已完成服務」
     * 3. 查詢保母資料並取得對應的會員ID
     * 4. 執行撥款（將預約金額撥給保母的會員帳戶）
     * 5. 更新訂單狀態為「已撥款」
     */
    public void completePayout(Integer orderId) {
        // 步驟 1：查詢訂單 
        BookingOrderVO order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("找不到訂單"));

        // 步驟 2：驗證訂單狀態
        // 只有狀態為 2（已完成服務）的訂單才能進行撥款
        if (order.getOrderStatus() != 2) {
            throw new RuntimeException("訂單尚未完成服務。");
        }

        //  步驟 3：查詢保母資料 
        // 透過保母ID查詢保母資料
        SitterVO sitter = sitterRepository.findById(order.getSitterId())
                .orElseThrow(() -> new RuntimeException("找不到保姆資料"));

        // 取得保母對應的會員ID（因為撥款是撥到會員帳戶）
        Integer sitterMemId = sitter.getMemId();

        // 步驟 4：執行撥款
        // 將訂單的預約金額撥款給保母的會員帳戶
        dataService.processPayout(sitterMemId, order.getReservationFee());

        // 步驟 5：更新訂單狀態
        order.setOrderStatus(5); // 5: 已撥款
        orderRepository.save(order);
    }
}