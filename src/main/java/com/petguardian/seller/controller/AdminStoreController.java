package com.petguardian.seller.controller;


import com.petguardian.orders.model.*;
import com.petguardian.orders.service.OrdersService;
import com.petguardian.orders.service.ReturnOrderService;
import com.petguardian.wallet.model.Wallet;
import com.petguardian.wallet.model.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/store")
public class AdminStoreController {

    @Autowired
    private OrdersRepository ordersRepository;

    @Autowired
    private ReturnOrderRepository returnOrderRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private OrdersService ordersService;

    @Autowired
    private ReturnOrderService returnOrderService;

    @Autowired
    private StoreMemberRepository memberRepository;
    /**
     * 後台 - 二手商城管理
     * URL: /admin/marketplace/manage
     * 模板: templates/backend/marketplace.html
     */
    @GetMapping("/manage")
    public String showStoreManagement(Model model) {

        // ✨ 1. 取得所有已完成訂單（待撥款）
        List<OrdersVO> completedOrders = ordersRepository.findByOrderStatusOrderByOrderTimeDesc(2);

        // 為每個訂單加入詳細資訊
        List<Map<String, Object>> ordersWithDetails = new ArrayList<>();
        for (OrdersVO order : completedOrders) {
            Map<String, Object> orderData = new HashMap<>();
            orderData.put("order", order);

            // 取得買家名稱
            memberRepository.findById(order.getBuyerMemId())
                    .ifPresent(buyer -> orderData.put("buyerName", buyer.getMemName()));

            // 取得賣家名稱
            memberRepository.findById(order.getSellerMemId())
                    .ifPresent(seller -> orderData.put("sellerName", seller.getMemName()));

            // 取得訂單項目
            Map<String, Object> orderWithItems = ordersService.getOrderWithItems(order.getOrderId());
            orderData.put("items", orderWithItems.get("orderItems"));

            ordersWithDetails.add(orderData);
        }

        // ✨ 2. 取得所有退貨申請
        List<ReturnOrderVO> returnOrders = returnOrderRepository.findAllByOrderByApplyTimeDesc();

        // 為每個退貨申請加入訂單資訊
        List<Map<String, Object>> returnsWithDetails = new ArrayList<>();
        for (ReturnOrderVO returnOrder : returnOrders) {
            Map<String, Object> returnData = new HashMap<>();
            returnData.put("returnOrder", returnOrder);

            // 取得關聯的訂單
            ordersRepository.findById(returnOrder.getOrderId())
                    .ifPresent(order -> {
                        returnData.put("order", order);

                        // 取得買家名稱
                        memberRepository.findById(order.getBuyerMemId())
                                .ifPresent(buyer -> returnData.put("buyerName", buyer.getMemName()));

                        // 取得賣家名稱
                        memberRepository.findById(order.getSellerMemId())
                                .ifPresent(seller -> returnData.put("sellerName", seller.getMemName()));
                    });

            returnsWithDetails.add(returnData);
        }

        model.addAttribute("ordersWithDetails", ordersWithDetails);
        model.addAttribute("returnsWithDetails", returnsWithDetails);

        return "backend/market";
    }


    /**
     * 批准退款
     * URL: POST /admin/store/return/approve
     */
    @PostMapping("/return/approve")
    public String approveReturn(
            @RequestParam Integer returnId,
            RedirectAttributes redirectAttributes) {

        try {
            ReturnOrderVO returnOrder = returnOrderRepository.findById(returnId)
                    .orElseThrow(() -> new RuntimeException("退貨申請不存在"));

            // 更新退貨狀態為通過
            returnOrder.setReturnStatus(1); // 1=退貨通過
            returnOrderRepository.save(returnOrder);

            // 更新訂單狀態
            OrdersVO order = ordersRepository.findById(returnOrder.getOrderId())
                    .orElseThrow(() -> new RuntimeException("訂單不存在"));
            order.setOrderStatus(5); // 5=退貨完成
            ordersRepository.save(order);

            // ✨ 退款給買家
            Integer refundAmount = returnOrder.getRefundAmount();
            Wallet buyerWallet = walletRepository.findByMemId(order.getBuyerMemId())
                    .orElseThrow(() -> new RuntimeException("買家錢包不存在"));

            buyerWallet.setBalance(buyerWallet.getBalance() + refundAmount);
            walletRepository.save(buyerWallet);

            redirectAttributes.addFlashAttribute("successMessage",
                    "退款已批准！已撥款 $" + refundAmount + " 至買家錢包");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "批准退款失敗：" + e.getMessage());
        }
        return "redirect:/admin/store/manage";
    }

    /**
     * 駁回退款
     * URL: POST /admin/store/return/reject
     */
    @PostMapping("/return/reject")
    public String rejectReturn(
            @RequestParam Integer returnId,
            RedirectAttributes redirectAttributes) {

        try {
            ReturnOrderVO returnOrder = returnOrderRepository.findById(returnId)
                    .orElseThrow(() -> new RuntimeException("退貨申請不存在"));

            // 更新退貨狀態為失敗
            returnOrder.setReturnStatus(2); // 2=退貨失敗
            returnOrderRepository.save(returnOrder);

            // 恢復訂單狀態為已完成
            OrdersVO order = ordersRepository.findById(returnOrder.getOrderId())
                    .orElseThrow(() -> new RuntimeException("訂單不存在"));
            order.setOrderStatus(2); // 2=已完成
            ordersRepository.save(order);

            redirectAttributes.addFlashAttribute("successMessage", "已駁回退款申請");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "駁回退款失敗：" + e.getMessage());
        }

        return "redirect:/admin/store/manage";
    }

    /**
     * 撥款給賣家（完整實作）
     */
    @PostMapping("/payout")
    public String payoutToSeller(
            @RequestParam Integer orderId,
            RedirectAttributes redirectAttributes) {

        try {
            OrdersVO order = ordersRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("訂單不存在"));

            // 檢查訂單狀態是否為已完成
            if (order.getOrderStatus() != 2) {
                redirectAttributes.addFlashAttribute("error", "只有已完成的訂單才能撥款");
                return "redirect:/admin/marketplace/manage";
            }

            // ✨ 撥款給賣家
            Integer payoutAmount = order.getOrderTotal();
            Wallet sellerWallet = walletRepository.findByMemId(order.getSellerMemId())
                    .orElseThrow(() -> new RuntimeException("賣家錢包不存在"));

            sellerWallet.setBalance(sellerWallet.getBalance() + payoutAmount);
            walletRepository.save(sellerWallet);

            // 可選：更新訂單狀態或加上撥款標記
            // order.setOrderStatus(6); // 假設 6 表示已撥款
            // ordersRepository.save(order);

            redirectAttributes.addFlashAttribute("successMessage",
                    "撥款成功！已匯入 $" + payoutAmount + " 至賣家錢包");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "撥款失敗：" + e.getMessage());
        }

        return "redirect:/admin/store/manage";
    }
}