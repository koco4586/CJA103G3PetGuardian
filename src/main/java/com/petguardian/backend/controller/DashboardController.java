package com.petguardian.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.petguardian.backend.service.DashboardService;

import java.util.Map;

/**
 * 後台首頁 Controller
 * 負責顯示後台儀表板統計數據
 */
@Controller
@RequestMapping("/admin")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    /**
     * 後台首頁
     * URL: GET /admin/index
     */
    @GetMapping("/index")
    public String showDashboard(Model model) {
        Map<String, Object> stats = dashboardService.getDashboardStatistics();

        // 會員總數（mem_status = 1 啟用）
        model.addAttribute("totalMembers", stats.get("totalMembers"));

        // 待審保母（app_status = 0 待審核）
        model.addAttribute("pendingSitters", stats.get("pendingSitters"));

        // 預約待處理退款（booking_order 的 order_status = 3）
        model.addAttribute("bookingPendingRefunds", stats.get("bookingPendingRefunds"));

        // 商城待處理退款（return_order 的 return_status = 0）
        model.addAttribute("storePendingRefunds", stats.get("storePendingRefunds"));

        // 預約待處理評價（booking_order_report 的 report_status = 0）
        model.addAttribute("bookingPendingReviews", stats.get("bookingPendingReviews"));

        // 商城待處理評價（seller_review_report 的 report_status = 0）
        model.addAttribute("storePendingReviews", stats.get("storePendingReviews"));

        return "backend/index";
    }
}