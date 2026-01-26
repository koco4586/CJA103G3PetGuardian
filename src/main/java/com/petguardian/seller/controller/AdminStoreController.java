package com.petguardian.seller.controller;

import com.petguardian.seller.service.AdminStoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/store")
public class AdminStoreController {

    @Autowired
    private AdminStoreService adminStoreService;

    @GetMapping("/manage")
    public String showStoreManagement(
            @RequestParam(defaultValue = "pending") String orderTab,
            Model model) {

        List<Map<String, Object>> pendingOrders = adminStoreService.getPendingOrders();
        List<Map<String, Object>> closedOrders = adminStoreService.getClosedOrders();
        List<Map<String, Object>> returnOrders = adminStoreService.getReturnOrders();
        List<Map<String, Object>> returnsWithDetails = adminStoreService.getReturnOrdersWithDetails();

        model.addAttribute("pendingOrders", pendingOrders);
        model.addAttribute("closedOrders", closedOrders);
        model.addAttribute("returnOrders", returnOrders);
        model.addAttribute("returnsWithDetails", returnsWithDetails);
        model.addAttribute("orderTab", orderTab);

        model.addAttribute("pendingCount", pendingOrders.size());
        model.addAttribute("closedCount", closedOrders.size());
        model.addAttribute("returnCount", returnOrders.size());
        model.addAttribute("refundPendingCount",
                returnsWithDetails.stream()
                        .filter(r -> Boolean.TRUE.equals(r.get("isPending")))
                        .count());

        return "backend/market";
    }

    @GetMapping("/return/{returnId}")
    @ResponseBody
    public Map<String, Object> getReturnOrderDetail(@PathVariable Integer returnId) {
        return adminStoreService.getReturnOrderDetail(returnId);
    }

    @PostMapping("/return/approve")
    public String approveReturn(
            @RequestParam Integer returnId,
            RedirectAttributes redirectAttributes) {

        try {
            adminStoreService.approveReturn(returnId);
            redirectAttributes.addFlashAttribute("successMessage", "退款已批准並撥款至買家錢包");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "批准退款失敗：" + e.getMessage());
        }

        return "redirect:/admin/store/manage";
    }

    @PostMapping("/return/reject")
    public String rejectReturn(
            @RequestParam Integer returnId,
            RedirectAttributes redirectAttributes) {

        try {
            adminStoreService.rejectReturn(returnId);
            redirectAttributes.addFlashAttribute("successMessage", "已駁回退款申請並撥款至賣家錢包");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "駁回退款失敗：" + e.getMessage());
        }

        return "redirect:/admin/store/manage";
    }

    @PostMapping("/payout")
    public String payoutToSeller(
            @RequestParam Integer orderId,
            RedirectAttributes redirectAttributes) {

        try {
            adminStoreService.payoutToSeller(orderId);
            redirectAttributes.addFlashAttribute("successMessage", "撥款成功");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "撥款失敗：" + e.getMessage());
        }

        return "redirect:/admin/store/manage?orderTab=closed";
    }
}