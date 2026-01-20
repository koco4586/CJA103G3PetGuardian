package com.petguardian.seller.controller;


import com.petguardian.orders.model.*;
import com.petguardian.seller.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/store")
public class AdminStoreController {

    @Autowired
    private ReturnOrderRepository returnOrderRepository;

    @Autowired
    private OrdersRepository ordersRepository;

    @Autowired
    private ProductService.WalletService walletService;


    /**
     * 後台 - 二手商城管理
     * URL: /admin/marketplace/manage
     * 模板: templates/backend/marketplace.html
     */
    @GetMapping("/manage")
    public String showStoreManagement(Model model) {
        // 取得所有退貨申請
        List<ReturnOrderVO> returnOrders = returnOrderRepository.findAllByOrderByApplyTimeDesc();

        // 取得已完成的訂單（待撥款）
        List<OrdersVO> completedOrders = ordersRepository.findByOrderStatusOrderByOrderTimeDesc(2);

        model.addAttribute("returnOrders", returnOrders);
        model.addAttribute("completedOrders", completedOrders);

        return "backend/market";
    }

    /**
     * 批准退款
     * URL: POST /admin/marketplace/return/approve
     */
    @PostMapping("/return/approve")
    public String approveReturn(
            @RequestParam Integer returnId,
            RedirectAttributes redirectAttributes) {

        ReturnOrderVO returnOrder = returnOrderRepository.findById(returnId)
                .orElseThrow(() -> new RuntimeException("退貨申請不存在"));

        returnOrder.setReturnStatus(1); // 1=退貨通過
        returnOrderRepository.save(returnOrder);

        // 更新訂單狀態
        OrdersVO order = returnOrder.getOrdersVO();
        order.setOrderStatus(5); // 5=退貨完成
        ordersRepository.save(order);

        // 退款給買家
        walletService.addBalance(order.getBuyerMemId(), returnOrder.getRefundAmount());

        redirectAttributes.addFlashAttribute("successMessage", "退款已批准並撥款至買家錢包！");
        return "redirect:/admin/store/manage";
    }

    /**
     * 撥款給賣家（完整實作）
     */
    @PostMapping("/payout")
    public String payoutToSeller(
            @RequestParam Integer orderId,
            RedirectAttributes redirectAttributes) {

        OrdersVO order = ordersRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("訂單不存在"));

        // 撥款給賣家
        walletService.addBalance(order.getSellerMemId(), order.getOrderTotal());

        // 更新訂單狀態（可選：標記為已撥款）
        // order.setOrderStatus(3); // 假設 3 表示已撥款
        // ordersRepository.save(order);

        redirectAttributes.addFlashAttribute("successMessage", "撥款成功！已匯入賣家錢包。");
        return "redirect:/admin/store/manage";
    }
}