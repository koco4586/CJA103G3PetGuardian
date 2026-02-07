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

        // 1. 管理員總數 (adm_status = 1)
        model.addAttribute("totalAdmins", stats.get("totalAdmins"));

        // 2. 會員總數 (mem_status = 1)
        model.addAttribute("totalMembers", stats.get("totalMembers"));

        // 3. 待審保母 (app_status = 0)
        model.addAttribute("pendingSitters", stats.get("pendingSitters"));

        // 4. 預約待審退款 (order_status = 3)
        model.addAttribute("bookingPendingRefunds", stats.get("bookingPendingRefunds"));

        // 5. 預約待審評價檢舉 (report_status = 0)
        model.addAttribute("bookingPendingReviews", stats.get("bookingPendingReviews"));

        // 6. 聊天室待審檢舉 (report_status = 0)
        model.addAttribute("chatPendingReports", stats.get("chatPendingReports"));

        // 7. 已發布文章數量 (IS_PUBLISHED = 1) -> 新增
        model.addAttribute("publishedNewsCount", stats.get("publishedNewsCount"));

        // 8. 商城待審退款 (return_status = 0)
        model.addAttribute("storePendingRefunds", stats.get("storePendingRefunds"));

        // 9. 商城待審評價檢舉 (report_status = 0)
        model.addAttribute("storePendingReviews", stats.get("storePendingReviews"));

        // 10. 論壇待審留言檢舉 (report_status = 0)
        model.addAttribute("forumPendingCommentReports", stats.get("forumPendingCommentReports"));

        // 11. 論壇待審文章檢舉 (report_status = 0)
        model.addAttribute("forumPendingPostReports", stats.get("forumPendingPostReports"));

        return "backend/index";
    }
}