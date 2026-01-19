package com.petguardian.sellerreview.controller;

import com.petguardian.sellerreview.model.*;
import com.petguardian.sellerreview.service.ReviewService;
import com.petguardian.shop.service.*;
import com.petguardian.orders.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
        import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/review")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private SellerOrderService sellerOrderService;

    /**
     * 買家評價頁面
     * URL: /review/list
     * 模板: templates/frontend/review.html
     */
    @GetMapping("/list")
    public String showReviewList(Model model) {
        // TODO: 從 Session 取得登入的買家 ID
        Integer buyerId = 1001;

        // 查詢買家的所有已完成訂單（可評價的訂單）
        List<OrdersVO> completedOrders = sellerOrderService.getSellerOrders(buyerId)
                .stream()
                .filter(o -> o.getOrderStatus() == 2) // 2=已完成
                .toList();

        model.addAttribute("completedOrders", completedOrders);

        return "frontend/review";
    }

    /**
     * 提交評價
     * URL: POST /review/submit
     */
    @PostMapping("/submit")
    public String submitReview(
            @RequestParam Integer orderId,
            @RequestParam Integer rating,
            @RequestParam String reviewContent,
            RedirectAttributes redirectAttributes) {

        // 查詢訂單
        OrdersVO order = sellerOrderService.getOrderById(orderId)
                .orElseThrow(() -> new RuntimeException("訂單不存在"));

        // 建立評價
        SellerReviewVO review = new SellerReviewVO();
        review.setOrder(order);
        review.setRating(rating);
        review.setReviewContent(reviewContent);
        review.setShowStatus(0); // 0=顯示

        reviewService.addReview(review);

        // 更新賣家的評分
        reviewService.updateMemberRating(order.getSellerMemId());

        redirectAttributes.addFlashAttribute("successMessage", "評價提交成功！");
        return "redirect:/review/list";
    }
}
