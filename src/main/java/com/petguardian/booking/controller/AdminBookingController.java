package com.petguardian.booking.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.petguardian.booking.model.BookingOrderRepository;
import com.petguardian.booking.model.BookingOrderVO;
import com.petguardian.booking.service.BookingService;
import com.petguardian.sitter.model.SitterRepository;

@Controller
@RequestMapping("/admin/bookings") // 設定後台管理路徑
public class AdminBookingController {

    @Autowired
    private BookingOrderRepository orderRepository;

    @Autowired
    private BookingService bookingService;
    
    @Autowired
    private SitterRepository sitterRepository;

    @GetMapping("/all")
    public String listAllBookings(Model model) {
        // 1. 抓取所有預約訂單
        List<BookingOrderVO> allBookings = orderRepository.findAll();

        // 2. 抓取所有「退款中」或「有取消原因」的訂單
        List<BookingOrderVO> refundRequests = orderRepository.findByOrderStatus(3);
        
        // 批次抓取保姆狀態
        java.util.Set<Integer> sitterIds = allBookings.stream()
        		.map(BookingOrderVO::getSitterId).collect(java.util.stream.Collectors.toSet());

        // 一次性查出所有保姆
        java.util.Map<Integer, Byte> sitterStatusMap = sitterRepository.findAllById(sitterIds).stream()
                .collect(java.util.stream.Collectors.toMap(
                    com.petguardian.sitter.model.SitterVO::getSitterId, 
                    com.petguardian.sitter.model.SitterVO::getSitterStatus
                ));
        // 補齊資料
        allBookings.forEach(o -> o.setSitterStatus(sitterStatusMap.get(o.getSitterId())));
        refundRequests.forEach(o -> o.setSitterStatus(sitterStatusMap.get(o.getSitterId())));
        
        // 3. 將資料存入 model 傳給 Thymeleaf
        model.addAttribute("allBookings", allBookings);
        model.addAttribute("refundRequests", refundRequests);
        model.addAttribute("bookingService", bookingService);

        return "backend/bookings"; // 對應 HTML 檔案位置
    }
    @PostMapping("/approveRefund")
    @ResponseBody // 代表回傳的是純文字或 JSON，而不是跳轉頁面
    public String approveRefund(@RequestParam Integer orderId) {
    	try {
            // 1. 撈出訂單
            BookingOrderVO order = orderRepository.findById(orderId).orElseThrow();
            
            // 2. 讓系統計算比例
            Double calculatedRatio = bookingService.calculateRefundRatio(order.getStartTime(), order.getCancelTime());
            
            // 3. 呼叫 Service 執行 (使用計算後的比例)
            bookingService.approveRefund(orderId, calculatedRatio);
            
            return "success";
        } catch (Exception e) {
            return "error: " + e.getMessage();
        }
    }
    @PostMapping("/payout")
    @ResponseBody
    public String payout(@RequestParam Integer orderId) {
        try {
            bookingService.completePayout(orderId);
            return "success";
        } catch (RuntimeException e) {
            return e.getMessage();
        } catch (Exception e) {
            return "系統異常，請稍後再試";
        }
    }
    @PostMapping("/rejectRefund")
    @ResponseBody
    public String rejectRefund(@RequestParam Integer orderId) {
        try {
            // 駁回邏輯：把狀態從 3 改回 1 (繼續進行中)
            BookingOrderVO order = orderRepository.findById(orderId).orElseThrow();
            order.setOrderStatus(1);
            orderRepository.save(order);
            return "success";
        } catch (Exception e) { return "error"; }
    }
    @PostMapping("/forceComplete")
    @ResponseBody
    public String forceComplete(@RequestParam Integer orderId) {
        try {
            BookingOrderVO order = orderRepository.findById(orderId).orElseThrow();
            // 將狀態從 3 (取消/退款中) 強制改回 2 (服務完成)
            order.setOrderStatus(2);
            // 清除取消原因，避免混淆（選配）
            order.setCancelReason(order.getCancelReason() + " (逾期退款，管理員強制完成)");
            orderRepository.save(order);
            return "success";
        } catch (Exception e) {
            return "error: " + e.getMessage();
        }
    }
    
    @PostMapping("/forceCompleteRefund")
    @ResponseBody
    public String forceCompleteRefund(@RequestParam Integer orderId) {
        try {
            BookingOrderVO order = orderRepository.findById(orderId).orElseThrow();
            // 自動計算比例
            Double ratio = bookingService.calculateRefundRatio(order.getStartTime(), order.getCancelTime());
            // 執行退款邏輯
            bookingService.approveRefund(orderId, ratio);
            // 狀態改為 5 (已結案)，使其從狀態 3 的列表中消失
            order.setOrderStatus(5);
            orderRepository.save(order);
            return "success";
        } catch (Exception e) {
            return "error: " + e.getMessage();
        }
    }

    @GetMapping("/api/refund-count")
    @ResponseBody
    public Long getRefundRequestCount() {
        //計算待處理退款的訂單總數 後台dashboard
        return (long) orderRepository.findByOrderStatus(3).size();
    }
    
    @PostMapping("/suspendRefund")
    @ResponseBody
    public String suspendRefund(@RequestParam Integer orderId) {
        try {
            bookingService.suspendSitterRefund(orderId);
            return "success";
        } catch (Exception e) {
            return "error: " + e.getMessage();
        }
    }

}