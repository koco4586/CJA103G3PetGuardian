package com.petguardian.orders.controller;

import com.petguardian.orders.dto.CartItem;
import com.petguardian.orders.dto.OrderFormDTO;
import com.petguardian.orders.dto.OrderItemDTO;
import com.petguardian.orders.model.OrdersVO;
import com.petguardian.orders.service.OrdersService;

import com.petguardian.orders.service.ReturnOrderService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 訂單控制器
 * 處理訂單相關的頁面路由與表單提交
 */
@Controller
@RequestMapping("/orders")
public class OrderController {

    private static final Integer TEST_MEM_ID = 1001;

    @Autowired
    private OrdersService ordersService;

    @Autowired
    private ReturnOrderService returnOrderService;

    // 取得當前會員 ID（含模擬登入邏輯）
    private Integer getCurrentMemId(HttpSession session) {
        Integer memId = (Integer) session.getAttribute("memId");
        if (memId == null) {
            memId = TEST_MEM_ID;
            session.setAttribute("memId", memId);
        }
        return memId;
    }

    // 取得或建立購物車
    @SuppressWarnings("unchecked")
    private List<CartItem> getOrCreateCart(HttpSession session) {
        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
        if (cart == null) {
            cart = new ArrayList<>();
            session.setAttribute("cart", cart);
        }
        return cart;
    }

    // 訂單完成頁面
    @GetMapping("/complete/{orderId}")
    public String orderCompletePage(@PathVariable Integer orderId,
            Model model, HttpSession session) {
        getCurrentMemId(session);

        if (orderId == null) {
            return "redirect:/store";
        }

        try {
            Map<String, Object> orderData = ordersService.getOrderWithItems(orderId);
            model.addAttribute("order", orderData.get("order"));
            model.addAttribute("orderItems", orderData.get("orderItems"));
            model.addAttribute("itemCount", orderData.get("itemCount"));
            return "frontend/orders/order-complete";
        } catch (Exception e) {
            return "redirect:/store";
        }
    }

    // 提交訂單（POST 表單）
    @PostMapping("/submit")
    public String submitOrder(@RequestParam Integer sellerId,
            @RequestParam String receiverName,
            @RequestParam String receiverPhone,
            @RequestParam String receiverAddress,
            @RequestParam(required = false) String specialInstructions,
            HttpSession session,
            RedirectAttributes redirectAttr) {
        Integer memId = getCurrentMemId(session);

        // 取得購物車
        List<CartItem> cart = getOrCreateCart(session);
        if (cart.isEmpty()) {
            redirectAttr.addFlashAttribute("error", "購物車為空");
            return "redirect:/store";
        }

        try {
            // 組裝 OrderFormDTO
            OrderFormDTO form = new OrderFormDTO();
            form.setSellerId(sellerId);
            form.setReceiverName(receiverName);
            form.setReceiverPhone(receiverPhone);
            form.setReceiverAddress(receiverAddress);
            form.setSpecialInstructions(specialInstructions);
            form.setPaymentMethod(0); // 錢包付款

            // 轉換購物車項目為 OrderItemDTO
            List<OrderItemDTO> items = cart.stream().map(cartItem -> {
                OrderItemDTO dto = new OrderItemDTO();
                dto.setProId(cartItem.getProId());
                dto.setQuantity(cartItem.getQuantity());
                dto.setProPrice(cartItem.getProPrice());
                return dto;
            }).collect(Collectors.toList());
            form.setItems(items);

            // 執行結帳
            OrdersVO order = ordersService.checkout(memId, form);

            // 清空購物車
            session.removeAttribute("cart");

            // 導向訂單完成頁
            return "redirect:/orders/complete/" + order.getOrderId();

        } catch (Exception e) {
            redirectAttr.addFlashAttribute("error", e.getMessage());
            return "redirect:/store/checkout";
        }
    }

    // 取消訂單（含退款）
    @PostMapping("/{orderId}/cancel")
    public String cancelOrder(@PathVariable Integer orderId,
            HttpSession session,
            RedirectAttributes redirectAttr) {
        getCurrentMemId(session);

        try {
            ordersService.cancelOrderWithRefund(orderId);
            redirectAttr.addFlashAttribute("message", "訂單已取消，款項已退回錢包");
        } catch (Exception e) {
            redirectAttr.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/dashboard/orders";
    }

    // 申請退貨
    @PostMapping("/return")
    public String applyReturn(@RequestParam Integer orderId,
            @RequestParam String returnReason,
            @RequestParam(required = false) String returnDescription,
            HttpSession session,
            RedirectAttributes redirectAttr) {
        getCurrentMemId(session);

        try {
            // 合併退貨原因與詳細說明
            String fullReason = returnReason;
            if (returnDescription != null && !returnDescription.trim().isEmpty()) {
                fullReason += "：" + returnDescription;
            }

            // 使用 ReturnOrderService 建立退貨單並更新訂單狀態
            returnOrderService.applyReturn(orderId, fullReason);
            redirectAttr.addFlashAttribute("message", "退貨申請已提交");
        } catch (Exception e) {
            redirectAttr.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/dashboard/orders";
    }

    // 確認收貨（已出貨 → 已完成）
    @PostMapping("/{orderId}/confirm")
    public String confirmReceipt(@PathVariable Integer orderId,
            HttpSession session,
            RedirectAttributes redirectAttr) {
        getCurrentMemId(session);

        try {
            ordersService.updateOrderStatus(orderId, 2); // 2 = 已完成
            redirectAttr.addFlashAttribute("message", "已確認收貨，訂單已完成");
        } catch (Exception e) {
            redirectAttr.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/dashboard/orders";
    }
}
