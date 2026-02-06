package com.petguardian.booking.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.petguardian.booking.model.BookingOrderVO;
import com.petguardian.booking.service.BookingService;
import com.petguardian.common.service.AuthStrategyService;
import com.petguardian.sitter.service.SitterService;
import com.petguardian.sitter.model.SitterVO;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 保姆端預約管理控制器
 * 
 * 職責範圍：
 * - 顯示保姆收到的預約列表
 * - 處理保姆對預約的操作（接受/拒絕/完成）
 * - 計算保姆的本月收入
 * - 提供預約狀態篩選功能
 * 訂單狀態流程（保姆視角）：
 * 1. 待確認 (0) → 保姆可以「接受」或「拒絕」
 * 2. 已確認 (1) → 等待服務開始
 * 3. 服務中 (2) → 保姆可以標記「完成」
 * 4. 已完成 (4) → 等待會員評價
 * 5. 已評價 (5) → 流程結束
 */

@Controller
@RequestMapping("/sitter")
public class SitterBookingController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private SitterService sitterService;

    @Autowired
    private AuthStrategyService authStrategyService;

    @Autowired
    private com.petguardian.booking.service.BookingDataIntegrationService dataService;

    /**
     * 【保母預約管理主頁】
     */
    @GetMapping("/bookings")
    public String listSitterOrders(@RequestParam(required = false) Integer status,
            HttpServletRequest request,
            Model model) {
        Integer memId = authStrategyService.getCurrentUserId(request);
//        if (memId == null)
//            return "redirect:/front/loginpage";

        // [Fix] 用 MemId 查 SitterId
        SitterVO sitter = sitterService.getSitterByMemId(memId);
        if (sitter == null) {
            return "redirect:/sitter/apply";
        }
        Integer sitterId = sitter.getSitterId();

        List<BookingOrderVO> bookingList = (status != null)
                ? bookingService.findOrdersBySitterAndStatus(sitterId, status)
                : bookingService.getOrdersBySitterId(sitterId);

        var member = dataService.getMemberInfo(memId);

        // 加上本月收入計算邏輯
        int income = bookingList.stream()
                .filter(o -> o.getOrderStatus() != null && (o.getOrderStatus() == 2 || o.getOrderStatus() == 5))
                .mapToInt(BookingOrderVO::getReservationFee)
                .sum();

        model.addAttribute("bookingList", bookingList);
        model.addAttribute("sitter", member);
        model.addAttribute("currentStatus", status);
        model.addAttribute("monthlyIncome", income);

        return "frontend/booking/sitter-bookings";
    }

    /**
     * 【保母操作：接受/完成訂單】(AJAX)
     */
    @PostMapping("/updateStatus/{orderId}")
    public ResponseEntity<String> updateStatus(@PathVariable Integer orderId,
            @RequestParam Integer newStatus,
            @RequestParam(required = false) String reason) {
        try {
            if (newStatus == 3) {
                // 呼叫包含理由的取消邏輯
                bookingService.cancelBooking(orderId, reason);
            } else {
                bookingService.updateOrderStatusBySitter(orderId, newStatus);
            }
            return ResponseEntity.ok("更新成功");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}