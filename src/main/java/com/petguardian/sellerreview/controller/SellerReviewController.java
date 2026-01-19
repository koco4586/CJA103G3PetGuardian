package com.petguardian.sellerreview.controller;

import com.petguardian.sellerreview.service.SellerReviewReportService;
import com.petguardian.sellerreview.service.SellerReviewService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 賣家評價控制器
 * 處理評價提交與檢舉功能
 */
@Controller
@RequestMapping("/reviews")
public class SellerReviewController {

    private static final Integer TEST_MEM_ID = 1001;

    @Autowired
    private SellerReviewService reviewService;

    @Autowired
    private SellerReviewReportService reportService;

    /**
     * 取得當前會員 ID（含模擬登入邏輯）
     */
    private Integer getCurrentMemId(HttpSession session) {
        Integer memId = (Integer) session.getAttribute("memId");
        if (memId == null) {
            memId = TEST_MEM_ID;
            session.setAttribute("memId", memId);
        }
        return memId;
    }

    /**
     * 提交評價
     */
    @PostMapping("/submit")
    public String submitReview(@RequestParam Integer orderId,
                               @RequestParam Integer rating,
                               @RequestParam(required = false) String reviewContent,
                               HttpSession session,
                               RedirectAttributes redirectAttr) {
        getCurrentMemId(session);

        try {
            reviewService.createReview(orderId, rating, reviewContent);
            redirectAttr.addFlashAttribute("message", "評價已提交");
        } catch (Exception e) {
            redirectAttr.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/dashboard/orders";
    }

    /**
     * 提交檢舉
     */
    @PostMapping("/report")
    public String submitReport(@RequestParam Integer reviewId,
                               @RequestParam String reportReason,
                               @RequestParam(required = false, defaultValue = "/checkout") String redirectUrl,
                               HttpSession session,
                               RedirectAttributes redirectAttr) {
        Integer memId = getCurrentMemId(session);

        try {
            reportService.createReport(reviewId, memId, reportReason);
            redirectAttr.addFlashAttribute("message", "檢舉已提交");
        } catch (Exception e) {
            redirectAttr.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:" + redirectUrl;
    }
}
