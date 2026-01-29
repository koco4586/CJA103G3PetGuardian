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

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/sitter")
public class SitterBookingController {

    @Autowired
    private BookingService bookingService;

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
        Integer sitterId = authStrategyService.getCurrentUserId(request);
        if (sitterId == null)
            return "redirect:/front/loginpage";

        List<BookingOrderVO> bookingList = (status != null)
                ? bookingService.findBySitterAndStatus(sitterId, status)
                : bookingService.getOrdersBySitterId(sitterId);

        var member = dataService.getMemberInfo(sitterId);

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