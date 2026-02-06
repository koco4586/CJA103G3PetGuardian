package com.petguardian.booking.service;

import java.time.Duration;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.petguardian.booking.model.BookingOrderRepository;
import com.petguardian.booking.model.BookingOrderVO;

/**
 * 退款服務
 * 職責：處理訂單取消、退款比例計算、退款審核等功能，狀台為6代表保母被停權
 */
@Service
@Transactional
public class BookingRefundService {

    @Autowired
    private BookingOrderRepository orderRepository;

    @Autowired
    private BookingScheduleInternalService scheduleInternalService;

    @Autowired
    private BookingDataIntegrationService dataService;

    /**
     * 取消訂單並處理退款
     * 處理流程：
     * 1. 查詢訂單是否存在
     * 2. 如果訂單狀態為「進行中」，計算退款比例並退款
     * 3. 更新訂單狀態為「申請退款中」
     * 4. 記錄取消原因和取消時間
     * 5. 釋放保母排程時段
     */
    public void cancelBooking(Integer orderId, String reason) {
        // 查詢訂單
        BookingOrderVO order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("找不到訂單"));

        // 退款邏輯 
        // 僅當訂單狀態為 1（進行中）時才處理退款
        if (order.getOrderStatus() == 1) {
            // 計算退款比例（依據取消時間與服務開始時間的距離）
            Double ratio = calculateRefundRatio(order.getStartTime(), LocalDateTime.now());
            
            // 計算實際退款金額
            int refundAmount = (int) (order.getReservationFee() * ratio);

            // 如果有退款金額，執行退款
            if (refundAmount > 0) {
                dataService.processRefund(order.getMemId(), refundAmount);
            }
        }

        // 更新訂單狀態 
        order.setOrderStatus(3);                        // 3: 申請退款中
        order.setCancelReason(reason);                  // 記錄取消原因
        order.setCancelTime(LocalDateTime.now());       // 記錄取消時間
        orderRepository.save(order);

        // 釋放保母排程時段
        // '0' 表示釋放時段（可預約）
        scheduleInternalService.updateSitterSchedule(order, '0');
    }

    /**
     * 核准退款（管理員手動審核）
     * 處理流程：
     * 1. 查詢訂單是否存在
     * 2. 依據審核結果的退款比例計算退款金額
     * 3. 執行退款
     * 4. 更新訂單狀態為「已退款」
     * 5. 在取消原因後附加退款比例說明
     * 6. 釋放保母排程時段
     */
    public void approveRefund(Integer orderId, Double ratio) {
        // 查詢訂單
        BookingOrderVO order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("找不到訂單"));

        // 計算並執行退款 
        int refundAmount = (int) (order.getReservationFee() * ratio);
        
        if (refundAmount > 0) {
            dataService.processRefund(order.getMemId(), refundAmount);
        }

        //  更新訂單狀態
        order.setOrderStatus(4); // 4: 已退款

        //  記錄退款比例說明
        // 根據比例生成友善的文字說明
        String ratioText = (ratio >= 1.0) ? "全額退款" 
                         : (ratio <= 0)   ? "不予退款" 
                         : (int)(ratio * 100) + "% 部分退款";
        
        // 在原取消原因後附加退款說明
        String logEntry = String.format(" [%s - 系統判定]", ratioText);
        order.setCancelReason(order.getCancelReason() + logEntry);

        orderRepository.save(order);

        // 釋放保母排程時段 
        // '0' 表示釋放時段（可預約）
        scheduleInternalService.updateSitterSchedule(order, '0');
    }

    /**
     * 計算退款比例
     * 
     * 退款規則：
     * - 24小時前取消：100% 全額退款
     * - 12-24小時前取消：50% 部分退款
     * - 12小時內取消：0% 不予退款
     */
    public Double calculateRefundRatio(LocalDateTime startTime, LocalDateTime cancelTime) {
        // 防止空值錯誤
        if (startTime == null || cancelTime == null) {
            return 0.0;
        }

        // 計算取消時間與服務開始時間的時數差距
        long hoursBetween = Duration.between(cancelTime, startTime).toHours();

        // 根據時數差距決定退款比例
        if (hoursBetween >= 24) {
            return 1.0; // 24小時前：100% 全額退款
        } else if (hoursBetween >= 12) {
            return 0.5; // 12-24小時：50% 部分退款
        } else {
            return 0.0; // 低於12小時：不予退款
        }
    }
    
    /**
     * [後台操作] 因保母停權導致的強制全額退款
     */
    public void suspendSitterRefund(Integer orderId) {
        BookingOrderVO order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("找不到訂單"));
        // 1. 執行 100% 全額退款
        if (order.getReservationFee() > 0) {
            dataService.processRefund(order.getMemId(), order.getReservationFee());
        }
        // 2. 更新狀態與記錄
        order.setOrderStatus(6); // 6: 保母停權
        order.setCancelReason("保母已被管理員停權，系統執行全額退款。");
        order.setCancelTime(LocalDateTime.now());
        orderRepository.save(order);
        // 3. 釋放排程
        scheduleInternalService.updateSitterSchedule(order, '0');
    }
}