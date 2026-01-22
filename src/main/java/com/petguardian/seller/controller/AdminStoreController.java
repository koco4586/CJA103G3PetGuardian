package com.petguardian.seller.controller;

import com.petguardian.orders.model.*;
import com.petguardian.orders.service.OrdersService;
import com.petguardian.orders.service.ReturnOrderService;
import com.petguardian.wallet.model.Wallet;
import com.petguardian.wallet.model.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 後台 - 二手商城管理控制器
 *
 * 功能：
 * 1. 查閱訂單紀錄（分三類：待完成、結案訂單、退貨）
 * 2. 退款申請審核
 * 3. 撥款給賣家
 */
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

    // ==================== 訂單狀態常數 ====================
    private static final Integer STATUS_PAID = 0;       // 已付款
    private static final Integer STATUS_SHIPPED = 1;    // 已出貨
    private static final Integer STATUS_COMPLETED = 2;  // 已完成
    private static final Integer STATUS_CANCELED = 3;   // 已取消
    private static final Integer STATUS_REFUNDING = 4;  // 申請退貨中
    private static final Integer STATUS_REFUNDED = 5;   // 退貨完成

    // 退貨狀態常數
    private static final Integer RETURN_STATUS_PENDING = 0;   // 審核中
    private static final Integer RETURN_STATUS_APPROVED = 1;  // 退貨通過
    private static final Integer RETURN_STATUS_REJECTED = 2;  // 退貨失敗

    /**
     * 後台 - 二手商城管理主頁
     * URL: GET /admin/store/manage
     * 模板: templates/backend/market.html
     */
    @GetMapping("/manage")
    public String showStoreManagement(
            @RequestParam(defaultValue = "pending") String orderTab,
            Model model) {

        // ==================== 查閱訂單紀錄 ====================

        // 1. 待完成訂單（狀態 0:已付款, 1:已出貨）
        List<Map<String, Object>> pendingOrders = new ArrayList<>();
        List<OrdersVO> paidOrders = ordersRepository.findByOrderStatusOrderByOrderTimeDesc(STATUS_PAID);
        List<OrdersVO> shippedOrders = ordersRepository.findByOrderStatusOrderByOrderTimeDesc(STATUS_SHIPPED);

        // 合併待完成訂單
        List<OrdersVO> allPendingOrders = new ArrayList<>();
        allPendingOrders.addAll(paidOrders);
        allPendingOrders.addAll(shippedOrders);

        for (OrdersVO order : allPendingOrders) {
            pendingOrders.add(buildOrderData(order));
        }

        // 2. 結案訂單（狀態 2:已完成, 3:已取消）
        List<Map<String, Object>> closedOrders = new ArrayList<>();
        List<OrdersVO> completedOrders = ordersRepository.findByOrderStatusOrderByOrderTimeDesc(STATUS_COMPLETED);
        List<OrdersVO> canceledOrders = ordersRepository.findByOrderStatusOrderByOrderTimeDesc(STATUS_CANCELED);

        // 合併結案訂單
        List<OrdersVO> allClosedOrders = new ArrayList<>();
        allClosedOrders.addAll(completedOrders);
        allClosedOrders.addAll(canceledOrders);

        for (OrdersVO order : allClosedOrders) {
            Map<String, Object> orderData = buildOrderData(order);
            // ✨ 檢查是否已撥款（這裡用一個簡單的標記，實際可以在訂單表加一個 paidOut 欄位）
            // 暫時用訂單狀態判斷：已完成且未取消的才能撥款
            orderData.put("canPayout", order.getOrderStatus().equals(STATUS_COMPLETED));
            orderData.put("isCanceled", order.getOrderStatus().equals(STATUS_CANCELED));
            closedOrders.add(orderData);
        }

        // 3. 退貨訂單（狀態 4:申請退貨中, 5:退貨完成）
        List<Map<String, Object>> returnOrders = new ArrayList<>();
        List<OrdersVO> refundingOrders = ordersRepository.findByOrderStatusOrderByOrderTimeDesc(STATUS_REFUNDING);
        List<OrdersVO> refundedOrders = ordersRepository.findByOrderStatusOrderByOrderTimeDesc(STATUS_REFUNDED);

        // 合併退貨訂單
        List<OrdersVO> allReturnOrders = new ArrayList<>();
        allReturnOrders.addAll(refundingOrders);
        allReturnOrders.addAll(refundedOrders);

        for (OrdersVO order : allReturnOrders) {
            returnOrders.add(buildOrderData(order));
        }

        // ==================== 退款申請審核 ====================
        List<Map<String, Object>> returnsWithDetails = new ArrayList<>();
        List<ReturnOrderVO> allReturnApplications = returnOrderRepository.findAllByOrderByApplyTimeDesc();

        for (ReturnOrderVO returnOrder : allReturnApplications) {
            Map<String, Object> returnData = new HashMap<>();
            returnData.put("returnOrder", returnOrder);

            // 取得關聯的訂單
            ordersRepository.findById(returnOrder.getOrderId())
                    .ifPresent(order -> {
                        returnData.put("order", order);

                        // 取得買家名稱（申請人）
                        memberRepository.findById(order.getBuyerMemId())
                                .ifPresent(buyer -> returnData.put("buyerName", buyer.getMemName()));

                        // 取得賣家名稱
                        memberRepository.findById(order.getSellerMemId())
                                .ifPresent(seller -> returnData.put("sellerName", seller.getMemName()));
                    });

            // 檢查是否待審核
            returnData.put("isPending", returnOrder.getReturnStatus().equals(RETURN_STATUS_PENDING));
            returnData.put("isApproved", returnOrder.getReturnStatus().equals(RETURN_STATUS_APPROVED));
            returnData.put("isRejected", returnOrder.getReturnStatus().equals(RETURN_STATUS_REJECTED));

            returnsWithDetails.add(returnData);
        }

        // 傳遞資料到模板
        model.addAttribute("pendingOrders", pendingOrders);
        model.addAttribute("closedOrders", closedOrders);
        model.addAttribute("returnOrders", returnOrders);
        model.addAttribute("returnsWithDetails", returnsWithDetails);
        model.addAttribute("orderTab", orderTab);

        // 統計數據
        model.addAttribute("pendingCount", pendingOrders.size());
        model.addAttribute("closedCount", closedOrders.size());
        model.addAttribute("returnCount", returnOrders.size());
        model.addAttribute("refundPendingCount",
                returnsWithDetails.stream()
                        .filter(r -> Boolean.TRUE.equals(r.get("isPending")))
                        .count());

        return "backend/market";
    }

    /**
     * 建構訂單資料（含買家、賣家名稱）
     */
    private Map<String, Object> buildOrderData(OrdersVO order) {
        Map<String, Object> orderData = new HashMap<>();
        orderData.put("order", order);

        // 取得買家名稱
        memberRepository.findById(order.getBuyerMemId())
                .ifPresent(buyer -> orderData.put("buyerName", buyer.getMemName()));

        // 取得賣家名稱
        memberRepository.findById(order.getSellerMemId())
                .ifPresent(seller -> orderData.put("sellerName", seller.getMemName()));

        // 取得訂單項目
        try {
            Map<String, Object> orderWithItems = ordersService.getOrderWithItems(order.getOrderId());
            orderData.put("items", orderWithItems.get("orderItems"));
        } catch (Exception e) {
            orderData.put("items", new ArrayList<>());
        }

        return orderData;
    }

    /**
     * 取得訂單狀態文字
     */
    private String getOrderStatusText(Integer status) {
        switch (status) {
            case 0: return "已付款";
            case 1: return "已出貨";
            case 2: return "已完成";
            case 3: return "已取消";
            case 4: return "申請退貨中";
            case 5: return "退貨完成";
            default: return "未知";
        }
    }

    /**
     * 撥款給賣家
     * URL: POST /admin/store/payout
     *
     * 條件：訂單狀態為「已完成」才能撥款
     */
    @PostMapping("/payout")
    public String payoutToSeller(
            @RequestParam Integer orderId,
            RedirectAttributes redirectAttributes) {

        try {
            OrdersVO order = ordersRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("訂單不存在"));

            // 檢查訂單狀態是否為已完成
            if (!order.getOrderStatus().equals(STATUS_COMPLETED)) {
                redirectAttributes.addFlashAttribute("error", "只有已完成的訂單才能撥款");
                return "redirect:/admin/store/manage?orderTab=closed";
            }

            // ✨ 撥款給賣家
            Integer payoutAmount = order.getOrderTotal();
            Wallet sellerWallet = walletRepository.findByMemId(order.getSellerMemId())
                    .orElseThrow(() -> new RuntimeException("賣家錢包不存在"));

            sellerWallet.setBalance(sellerWallet.getBalance() + payoutAmount);
            walletRepository.save(sellerWallet);

            // ✨ 更新訂單狀態為已撥款（使用一個新狀態，例如 6）
            // 或者可以在訂單表加一個 paidOut 欄位
            // 這裡暫時更新為狀態 6 表示已撥款
            order.setOrderStatus(6); // 6 = 已撥款
            ordersRepository.save(order);

            redirectAttributes.addFlashAttribute("successMessage",
                    "撥款成功！已匯入 $" + payoutAmount + " 至賣家錢包");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "撥款失敗：" + e.getMessage());
        }

        return "redirect:/admin/store/manage?orderTab=closed";
    }

    /**
     * 查看退貨申請詳情
     * URL: GET /admin/store/return/{returnId}
     */
    @GetMapping("/return/{returnId}")
    @ResponseBody
    public Map<String, Object> getReturnOrderDetail(@PathVariable Integer returnId) {
        Map<String, Object> result = new HashMap<>();

        try {
            ReturnOrderVO returnOrder = returnOrderRepository.findById(returnId)
                    .orElseThrow(() -> new RuntimeException("退貨申請不存在"));

            result.put("success", true);
            result.put("returnId", returnOrder.getReturnId());
            result.put("orderId", returnOrder.getOrderId());
            result.put("returnReason", returnOrder.getReturnReason());
            result.put("refundAmount", returnOrder.getRefundAmount());
            result.put("applyTime", returnOrder.getApplyTime());
            result.put("returnStatus", returnOrder.getReturnStatus());

            // 取得訂單資訊
            ordersRepository.findById(returnOrder.getOrderId())
                    .ifPresent(order -> {
                        result.put("orderTotal", order.getOrderTotal());

                        // 取得買家名稱
                        memberRepository.findById(order.getBuyerMemId())
                                .ifPresent(buyer -> result.put("buyerName", buyer.getMemName()));

                        // 取得賣家名稱
                        memberRepository.findById(order.getSellerMemId())
                                .ifPresent(seller -> result.put("sellerName", seller.getMemName()));
                    });

            // TODO: 取得退貨圖片（如果有的話）
            // result.put("returnImages", returnOrderPicRepository.findByReturnId(returnId));

        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }

        return result;
    }

    /**
     * 批准退款
     * URL: POST /admin/store/return/approve
     *
     * 處理流程：
     * 1. 更新退貨狀態為「通過」
     * 2. 更新訂單狀態為「退貨完成」
     * 3. 退款金額撥入買家錢包
     */
    @PostMapping("/return/approve")
    public String approveReturn(
            @RequestParam Integer returnId,
            RedirectAttributes redirectAttributes) {

        try {
            ReturnOrderVO returnOrder = returnOrderRepository.findById(returnId)
                    .orElseThrow(() -> new RuntimeException("退貨申請不存在"));

            // 檢查是否為待審核狀態
            if (!returnOrder.getReturnStatus().equals(RETURN_STATUS_PENDING)) {
                redirectAttributes.addFlashAttribute("error", "此退貨申請已處理過");
                return "redirect:/admin/store/manage";
            }

            // 更新退貨狀態為通過
            returnOrder.setReturnStatus(RETURN_STATUS_APPROVED);
            returnOrderRepository.save(returnOrder);

            // 更新訂單狀態為退貨完成
            OrdersVO order = ordersRepository.findById(returnOrder.getOrderId())
                    .orElseThrow(() -> new RuntimeException("訂單不存在"));
            order.setOrderStatus(STATUS_REFUNDED); // 5=退貨完成
            ordersRepository.save(order);

            // ✨ 退款給買家（撥入買家錢包）
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
     *
     * 處理流程：
     * 1. 更新退貨狀態為「失敗」
     * 2. 更新訂單狀態為「已完成」
     * 3. 訂單金額撥入賣家錢包
     */
    @PostMapping("/return/reject")
    public String rejectReturn(
            @RequestParam Integer returnId,
            RedirectAttributes redirectAttributes) {

        try {
            ReturnOrderVO returnOrder = returnOrderRepository.findById(returnId)
                    .orElseThrow(() -> new RuntimeException("退貨申請不存在"));

            // 檢查是否為待審核狀態
            if (!returnOrder.getReturnStatus().equals(RETURN_STATUS_PENDING)) {
                redirectAttributes.addFlashAttribute("error", "此退貨申請已處理過");
                return "redirect:/admin/store/manage";
            }

            // 更新退貨狀態為失敗
            returnOrder.setReturnStatus(RETURN_STATUS_REJECTED);
            returnOrderRepository.save(returnOrder);

            // 恢復訂單狀態為已完成
            OrdersVO order = ordersRepository.findById(returnOrder.getOrderId())
                    .orElseThrow(() -> new RuntimeException("訂單不存在"));
            order.setOrderStatus(STATUS_COMPLETED); // 2=已完成
            ordersRepository.save(order);

            // ✨ 撥款給賣家（退款金額撥入賣家錢包）
            Integer payoutAmount = returnOrder.getRefundAmount();
            Wallet sellerWallet = walletRepository.findByMemId(order.getSellerMemId())
                    .orElseThrow(() -> new RuntimeException("賣家錢包不存在"));

            sellerWallet.setBalance(sellerWallet.getBalance() + payoutAmount);
            walletRepository.save(sellerWallet);

            redirectAttributes.addFlashAttribute("successMessage",
                    "已駁回退款申請，訂單金額 $" + payoutAmount + " 已撥給賣家");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "駁回退款失敗：" + e.getMessage());
        }

        return "redirect:/admin/store/manage";
    }
}