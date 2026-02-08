package com.petguardian.booking.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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
import com.petguardian.sitter.model.SitterVO;
import com.petguardian.sitter.service.SitterService;

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
    		@RequestParam(defaultValue = "0") int page,
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

     
     // 加上本月收入計算邏輯 (僅計算本月且已完成/已撥款)
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0);
        int income = bookingList.stream()
                .filter(o -> o.getOrderStatus() != null && (o.getOrderStatus() == 2 || o.getOrderStatus() == 5))
                .filter(o -> o.getStartTime().isAfter(startOfMonth)) // 僅計入本月
                .mapToInt(BookingOrderVO::getReservationFee)
                .sum();
        bookingList = bookingList.stream()
        	    .filter(order -> {
        	        // 過濾一個月前的取消訂單
        	        if (order.getOrderStatus() == 3 || order.getOrderStatus() == 4 || order.getOrderStatus() == 6) {
        	            LocalDateTime limit = LocalDateTime.now().minusMonths(1);
        	            return order.getUpdatedAt() != null && order.getUpdatedAt().isAfter(limit);
        	        }
        	        return true;
        	    })
        	    .sorted((o1, o2) -> {
        	        // 定義狀態權重 (1=0, 0=1, 2/5=2, 3/4/6=3)
        	        int p1 = getPriority(o1.getOrderStatus());
        	        int p2 = getPriority(o2.getOrderStatus());
        	        
        	        if (p1 != p2) return p1 - p2;
        	        
        	        // 同一等級內的次要排序
        	        if (p1 == 1) { // 即將到來：時間由近到遠 (昇冪)
        	            return o1.getStartTime().compareTo(o2.getStartTime());
        	        } else { // 其他：時間由新到舊 (降冪)
        	            return o2.getStartTime().compareTo(o1.getStartTime());
        	        }
        	    })
        	    .collect(Collectors.toList());
        
        int pageSize = 6;
        int totalRecords = bookingList.size();
        int totalPages = (int) Math.ceil((double) totalRecords / pageSize);

        if (page < 0) page = 0;
        if (totalPages > 0 && page >= totalPages) page = totalPages - 1;

        int start = page * pageSize;
        int end = Math.min(start + pageSize, totalRecords);

        List<BookingOrderVO> pagedList = (totalRecords > 0) 
            ? bookingList.subList(start, end) 
            : new java.util.ArrayList<>();

        model.addAttribute("bookingList", pagedList);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("sitter", sitter); 
        model.addAttribute("currentMember", member); 
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
    private int getPriority(Integer status) {
        if (status == null) return 99;
        switch (status) {
            case 1: return 0; // 已確認
            case 0: return 1; // 待確認
            case 2:
            case 5: return 2; // 服務中、已評價
            case 3:
            case 4:
            case 6: return 3; // 已取消、已拒絕、退款
            default: return 99;
        }
    }
}