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
     * 自動退款排程任務（每分鐘執行一次）
     * 處理邏輯：
     * 1. 查詢所有狀態為「申請退款中」(3) 的訂單
     * 2. 檢查是否已過服務開始時間（逾期未審核）
     * 3. 若已逾期，則自動計算退款比例並執行退款
     * 4. 更新訂單狀態為「已退款」(4)
     * 5. 記錄退款說明（包含系統自動判定字樣）
     * 6. 釋放保母排程時段
     * 執行頻率：每 60 秒（1 分鐘）執行一次
     */
//    @Scheduled(fixedRate = 60000) // 60000 毫秒 = 1 分鐘
//    public void autoRefundExpiredOrders() {
//        // 步驟 1：找出所有狀態為 3 (退款申請中) 的訂單
//        List<BookingOrderVO> pendingOrders = orderRepository.findByOrderStatus(3);
//
//        LocalDateTime now = LocalDateTime.now();
//
//        for (BookingOrderVO order : pendingOrders) {
//            // 步驟 2：檢查是否已過服務開始時間
//            if (now.isAfter(order.getStartTime())) {
//
//                // 步驟 3：計算退款比例 
//                // 依申請時間（cancelTime）vs 服務開始時間計算退款比例
//                Double ratio = refundService.calculateRefundRatio(
//                        order.getStartTime(),
//                        order.getCancelTime()
//                );
//
//                // 步驟 4：計算金額並執行退款 
//                int refundAmount = (int) (order.getReservationFee() * ratio);
//                if (refundAmount > 0) {
//                    dataService.processRefund(order.getMemId(), refundAmount);
//                }
//
//                // 步驟 5：更新訂單狀態
//                order.setOrderStatus(4); // 4: 已退款
//
//                // 步驟 6：設定原因字串（包含用戶要求的特定文字）
//                String ratioText = (ratio >= 1.0) ? "全額退款"
//                                 : (ratio <= 0)   ? "不予退款"
//                                 : (int)(ratio * 100) + "% 部分退款";
//                
//                String logEntry = String.format(" [%s - 系統判定(已過審核時間)]", ratioText);
//                order.setCancelReason(order.getCancelReason() + logEntry);
//
//                orderRepository.save(order);
//
//                // 步驟 7：釋放保母排程時段 
//                scheduleInternalService.updateSitterSchedule(order, '0');
//
//                // 記錄日誌
//                System.out.println("訂單 " + order.getBookingOrderId() + " 已自動執行逾期退款。");
//            }
//        }
//    }

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
     * 自動接單排程任務（每小時執行一次）
     * 處理邏輯：
     * 1. 查詢所有狀態為「待確認」(0) 的訂單
     * 2. 檢查訂單建立時間是否已超過 3 天
     * 3. 若已超過 3 天保母仍未回應，則自動接單
     * 4. 更新訂單狀態為「進行中」(1)
     * 5. 更新保母排程（鎖定時段）
     * 6. 記錄自動接單原因
     */
//    @Scheduled(fixedRate = 3600000) 
//    public void autoAcceptPendingOrders() {
//        // 步驟 1：找出所有狀態為 0 (待確認) 的訂單
//        List<BookingOrderVO> pendingOrders = orderRepository.findByOrderStatus(0);
//
//        LocalDateTime now = LocalDateTime.now();
//
//        for (BookingOrderVO order : pendingOrders) {
//            //  步驟 2：檢查是否已有建立時間，且已超過 3 天
//            if (order.getCreatedAt() != null && now.isAfter(order.getCreatedAt().plusDays(3))) {
//                try {
//                    // 步驟 3：更新狀態為 1 (進行中/已接單)
//                    order.setOrderStatus(1);
//
//                    // 步驟 4：加註原因（方便日後查詢）
//                    String currentReason = (order.getCancelReason() == null) ? "" : order.getCancelReason();
//                    order.setCancelReason(currentReason + " [逾期3天未回覆，系統自動接單]");
//
//                    orderRepository.save(order);
//
//                    // 步驟 5：更新保母行事曆（鎖定時段）
//                    // 參數 '1' 代表將時段設為忙碌/已預約
//                    scheduleInternalService.updateSitterSchedule(order, '1');
//
//                    // 記錄日誌
//                    System.out.println("訂單 " + order.getBookingOrderId() + " 已自動接單 (逾期3天未回覆)。");
//
//                } catch (Exception e) {
//                    // 記錄錯誤日誌
//                    System.err.println("訂單 " + order.getBookingOrderId() + " 自動接單失敗: " + e.getMessage());
//                }
//            }
//        }
//    }
}