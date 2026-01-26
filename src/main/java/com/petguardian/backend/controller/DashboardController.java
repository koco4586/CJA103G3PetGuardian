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

        // 會員總數
        model.addAttribute("totalMembers", stats.get("totalMembers"));

        // 待審保母
        model.addAttribute("pendingSitters", stats.get("pendingSitters"));

        // 預約待處理退款
        model.addAttribute("bookingPendingRefunds", stats.get("bookingPendingRefunds"));

        // 商城待處理退款
        model.addAttribute("storePendingRefunds", stats.get("storePendingRefunds"));

        // 預約待處理評價
        model.addAttribute("bookingPendingReviews", stats.get("bookingPendingReviews"));

        // 商城待處理評價
        model.addAttribute("storePendingReviews", stats.get("storePendingReviews"));

        return "backend/index";
    }
}