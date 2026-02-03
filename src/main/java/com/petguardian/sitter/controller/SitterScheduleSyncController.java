package com.petguardian.sitter.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.petguardian.booking.model.BookingOrderRepository;
import com.petguardian.booking.model.BookingOrderVO;
import com.petguardian.booking.service.BookingScheduleInternalService;

@Controller
@RequestMapping("/sitter")
public class SitterScheduleSyncController {

    @Autowired
    private BookingOrderRepository orderRepository;

    @Autowired
    private BookingScheduleInternalService scheduleInternalService;

    /**
     * 資料修復工具：同步所有訂單至排程表
     * URL: /sitter/api/sync-schedule
     * 用途：解決舊訂單在行事曆上未顯示為「橘色」的問題，並清除已取消訂單的佔用
     */
    @GetMapping("/api/sync-schedule")
    @ResponseBody
    @Transactional
    public ResponseEntity<?> syncSchedule() {
        try {
            // 1. 查詢所有訂單
            List<BookingOrderVO> allOrders = orderRepository.findAll();

            int count = 0;

            // 2. 遍歷每一筆訂單
            // 擴充支援狀態：0, 1, 2, 3, 4, 5
            for (BookingOrderVO order : allOrders) {
                Integer status = order.getOrderStatus();

                // 檢查是否為要處理的狀態 (0, 1, 2, 3, 4, 5)
                if (status != null && (status >= 0 && status <= 5)) {

                    char statusChar = '2'; // 預設為已預約 (Booked)

                    // 狀態判斷邏輯
                    // 0:待確認, 1:進行中, 2:已完成, 5:已完成(或相關狀態) -> 視為佔用 (Booked)
                    // 3:已取消, 4:退貨/退款 -> 視為釋放 (Free)
                    if (status == 3 || status == 4) {
                        statusChar = '0'; // 釋放時段
                    }

                    // 執行更新
                    scheduleInternalService.updateSitterSchedule(order, statusChar);

                    // 處理緩衝時間 (Logic: Booked 也要鎖緩衝, Free 也要釋放緩衝)
                    BookingOrderVO bufferOrder = new BookingOrderVO();
                    bufferOrder.setSitterId(order.getSitterId());
                    bufferOrder.setBookingOrderId(order.getBookingOrderId());
                    bufferOrder.setStartTime(order.getEndTime());
                    bufferOrder.setEndTime(order.getEndTime().plusHours(1));

                    scheduleInternalService.updateSitterSchedule(bufferOrder, statusChar);

                    count++;
                }
            }

            return ResponseEntity.ok(Map.of(
                    "message", "同步完成",
                    "processedOrders", count,
                    "totalOrders", allOrders.size()));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("message", "同步失敗: " + e.getMessage()));
        }
    }
}
