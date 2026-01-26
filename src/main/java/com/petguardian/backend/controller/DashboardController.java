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

        model.addAttribute("totalMembers", stats.get("totalMembers"));
        model.addAttribute("pendingSitters", stats.get("pendingSitters"));
        model.addAttribute("pendingRefunds", stats.get("pendingRefunds"));
        model.addAttribute("pendingReviews", stats.get("pendingReviews"));

        return "backend/index";
    }
}