package com.petguardian.sellerreview.controller;

import com.petguardian.common.service.AuthStrategyService;
import com.petguardian.sellerreview.service.SellerReviewReportService;
import com.petguardian.sellerreview.service.SellerReviewService;
import jakarta.servlet.http.HttpServletRequest;
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

    @Autowired
    private SellerReviewService reviewService;

    @Autowired
    private SellerReviewReportService reportService;

    @Autowired
    private AuthStrategyService authService;
    /**
     * 取得當前會員 ID（含模擬登入邏輯）
     */
    // private Integer getCurrentMemId(HttpSession session) {

    /**
     * 提交評價
     */
    @PostMapping("/submit")
    public String submitReview(@RequestParam Integer orderId,
            @RequestParam Integer rating,
            @RequestParam(required = false) String reviewContent,
            HttpSession session,
            RedirectAttributes redirectAttr,
            HttpServletRequest request) {
        Integer memId = authService.getCurrentUserId(request);
        if (memId == null) {
            return "redirect:/store";
        }

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
            @RequestParam(required = false, defaultValue = "/store/checkout") String redirectUrl,
            HttpSession session,
            RedirectAttributes redirectAttr,
            HttpServletRequest request) {
        Integer memId = authService.getCurrentUserId(request);
        if (memId == null) {
            return "redirect:/store";
        }

        try {
            reportService.createReport(reviewId, memId, reportReason);
            redirectAttr.addFlashAttribute("message", "檢舉已提交");
        } catch (Exception e) {
            redirectAttr.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:" + redirectUrl;
    }
}
