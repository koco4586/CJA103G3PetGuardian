package com.petguardian.booking.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.petguardian.booking.model.BookingOrderRepository;
import com.petguardian.booking.model.BookingOrderVO;

/**
 * 定時任務服務
 * 職責：處理所有與訂單相關的排程任務
 * - 自動退款：逾期未審核的退款申請
 * - 自動撥款：服務完成滿3天的訂單
 * - 自動接單：保母逾期未回覆的訂單
 */
@Service
@Transactional
public class BookingScheduledTaskService {

    @Autowired
    private BookingOrderRepository orderRepository;

    @Autowired
    private BookingScheduleInternalService scheduleInternalService;

    @Autowired
    private BookingRefundService refundService;

    @Autowired
    private BookingPayoutService payoutService;

    @Autowired
    private BookingDataIntegrationService dataService;

    /**
     * 自動退款排程任務
     * 頻率：每天凌晨 3 點執行
     * 找出「已取消(3)」且「取消時間超過7天」的訂單，執行退款
     */
     @Scheduled(cron = "0 0 3 * * ?") // 每天凌晨 3 點執行
    public void autoRefundExpiredOrders() {
        // 步驟 1：找出所有狀態為 3 (退款申請中) 的訂單
        List<BookingOrderVO> pendingOrders = orderRepository.findByOrderStatus(3);
        LocalDateTime now = LocalDateTime.now();
        for (BookingOrderVO order : pendingOrders) {
            // 檢查取消時間是否已超過 7 天
            if (order.getCancelTime() != null && now.isAfter(order.getCancelTime().plusDays(7))) {
                // 步驟 3：計算退款比例
                Double ratio = refundService.calculateRefundRatio(
                        order.getStartTime(),
                        order.getCancelTime()
                );
                // 步驟 4：執行退款
                int refundAmount = (int) (order.getReservationFee() * ratio);
                if (refundAmount > 0) {
                    dataService.processRefund(order.getMemId(), refundAmount);
                }
                // 步驟 5：更新訂單狀態為 4 (已退款)
                order.setOrderStatus(4); 
                // 步驟 6：記錄原因
                String logEntry = String.format(" [系統自動退款(申請逾期7天)]");
                String currentReason = (order.getCancelReason() == null) ? "" : order.getCancelReason();
                order.setCancelReason(currentReason + logEntry);
                orderRepository.save(order);
                // 步驟 7：釋放保母時段
                scheduleInternalService.updateSitterSchedule(order, '0');
                System.out.println("訂單 " + order.getBookingOrderId() + " 已自動執行退款 (超過7天)。");
            }
        }
    }

    /**
     * 自動撥款排程任務（每小時執行一次）
     * 處理邏輯：
     * 1. 查詢所有狀態為「服務完成」(2) 的訂單
     * 2. 檢查是否已超過結束時間 3 天
     * 3. 若已超過 3 天，則自動執行撥款
     * 4. 更新訂單狀態為「已撥款」(5)
     * 執行頻率：每 3600 秒（1 小時）執行一次
     */
//    @Scheduled(fixedRate = 3600000) // 3600000 毫秒 = 1 小時
//    public void autoPayoutCompletedOrders() {
//        // 步驟 1：找出所有狀態為 2 (服務完成) 的訂單
//        List<BookingOrderVO> completedOrders = orderRepository.findByOrderStatus(2);
//
//        LocalDateTime now = LocalDateTime.now();
//
//        for (BookingOrderVO order : completedOrders) {
//            //  步驟 2：檢查是否已超過結束時間 3 天 
//            if (now.isAfter(order.getEndTime().plusDays(3))) {
//                try {
//                    // 步驟 3：執行撥款 
//                    payoutService.completePayout(order.getBookingOrderId());
//
//                    // 記錄日誌
//                    System.out.println("訂單 " + order.getBookingOrderId() + " 已自動執行撥款 (服務完成滿3天)。");
//
//                } catch (Exception e) {
//                    // 記錄錯誤日誌
//                    System.err.println("訂單 " + order.getBookingOrderId() + " 自動撥款失敗: " + e.getMessage());
//                }
//            }
//        }
//    }
    
    /**
     * 自動接單排程任務
     * 每天凌晨 3 點執行
     * 保母逾期 3 天未回覆，自動接單
     */
     @Scheduled(cron = "0 0 3 * * ?") // 每天凌晨 3 點執行
    public void autoAcceptPendingOrders() {
        // 步驟 1：找出狀態為 0 (待確認) 的訂單
        List<BookingOrderVO> pendingOrders = orderRepository.findByOrderStatus(0);
        LocalDateTime now = LocalDateTime.now();
        for (BookingOrderVO order : pendingOrders) {
            // 步驟 2：檢查訂單是否已建立超過 3 天
            if (order.getCreatedAt() != null && now.isAfter(order.getCreatedAt().plusDays(3))) {
                try {
                    // 步驟 3：自動接單 (狀態改為 1)
                    order.setOrderStatus(1);
                    // 步驟 4：記錄原因
                    String currentReason = (order.getCancelReason() == null) ? "" : order.getCancelReason();
                    order.setCancelReason(currentReason + " [逾期3天未回覆，系統自動接單]");
                    orderRepository.save(order);
                    // 步驟 5：更新保母行事曆 (鎖定時段 '1')
                    scheduleInternalService.updateSitterSchedule(order, '1');
                    System.out.println("訂單 " + order.getBookingOrderId() + " 已自動接單。");
                } catch (Exception e) {
                    System.err.println("訂單 " + order.getBookingOrderId() + " 自動接單失敗: " + e.getMessage());
                }
            }
        }
    }
}