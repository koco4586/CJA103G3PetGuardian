package com.petguardian.seller.controller;

import com.petguardian.seller.model.ProType;
import com.petguardian.seller.service.AdminStoreService;
import com.petguardian.seller.service.ProTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 後台二手商城管理 Controller
 */
@Controller
@RequestMapping("/admin/store")
public class AdminStoreController {

    @Autowired
    private AdminStoreService adminStoreService;

    @Autowired
    private ProTypeService proTypeService;

    /**
     * 二手商城管理頁面
     */
    @GetMapping("/manage")
    public String showStoreManagement(
            @RequestParam(defaultValue = "pending") String orderTab,
            Model model) {

            // 取得訂單相關資料
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

            // 取得商品類別資料
            List<ProType> proTypes = proTypeService.getAllProTypes();
            List<Map<String, Object>> proTypesWithCount = new java.util.ArrayList<>();
            for (ProType proType : proTypes) {
                Map<String, Object> typeData = new HashMap<>();
                typeData.put("proType", proType);
                typeData.put("productCount", proTypeService.countProductsByProType(proType.getProTypeId()));
                proTypesWithCount.add(typeData);
            }
            model.addAttribute("proTypesWithCount", proTypesWithCount);
            model.addAttribute("proTypeCount", proTypes.size());

            return "backend/market";
        }

    // ==================== 退貨相關 ====================

    /**
     * 取得退貨詳情（AJAX）
     */
    @GetMapping("/return/{returnId}")
    @ResponseBody
    public Map<String, Object> getReturnOrderDetail(@PathVariable Integer returnId) {
        return adminStoreService.getReturnOrderDetail(returnId);
    }

    /**
     * 批准退貨申請
     */
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

    /**
     * 駁回退貨申請
     */
    @PostMapping("/return/reject")
    public String rejectReturn(
            @RequestParam Integer returnId,
            RedirectAttributes redirectAttributes) {

        try {
            adminStoreService.rejectReturn(returnId);
            redirectAttributes.addFlashAttribute("successMessage", "已駁回退款申請並移至結案訂單");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "駁回退款失敗：" + e.getMessage());
        }

        return "redirect:/admin/store/manage";
    }

    /**
     * 撥款給賣家
     */
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
    /**
     * 取得訂單詳情（AJAX）
     * 後台訂單列表點擊「查閱」按鈕時呼叫
     */
    @GetMapping("/order/{orderId}/detail")
    @ResponseBody
    public Map<String, Object> getOrderDetail(@PathVariable Integer orderId) {
        return adminStoreService.getOrderDetailForAdmin(orderId);
    }
    // ==================== 商品類別管理 ====================

    /**
     * 新增商品類別
     */
    @PostMapping("/protype/add")
    public String addProType(
            @RequestParam String proTypeName,
            RedirectAttributes redirectAttributes) {

        try {
            proTypeService.addProType(proTypeName);
            redirectAttributes.addFlashAttribute("successMessage", "商品類別新增成功");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "新增失敗：" + e.getMessage());
        }

        return "redirect:/admin/store/manage";
    }

    /**
     * 更新商品類別
     */
    @PostMapping("/protype/update")
    public String updateProType(
            @RequestParam Integer proTypeId,
            @RequestParam String proTypeName,
            RedirectAttributes redirectAttributes) {

        try {
            proTypeService.updateProType(proTypeId, proTypeName);
            redirectAttributes.addFlashAttribute("successMessage", "商品類別更新成功");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "更新失敗：" + e.getMessage());
        }

        return "redirect:/admin/store/manage";
    }

    /**
     * 刪除商品類別
     */
    @PostMapping("/protype/delete")
    public String deleteProType(
            @RequestParam Integer proTypeId,
            RedirectAttributes redirectAttributes) {

        try {
            boolean success = proTypeService.deleteProType(proTypeId);
            if (success) {
                redirectAttributes.addFlashAttribute("successMessage", "商品類別刪除成功");
            } else {
                redirectAttributes.addFlashAttribute("error", "商品類別不存在");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "刪除失敗：" + e.getMessage());
        }

        return "redirect:/admin/store/manage";
    }

    /**
     * 取得單一商品類別（AJAX）
     */
    @GetMapping("/protype/{proTypeId}")
    @ResponseBody
    public Map<String, Object> getProType(@PathVariable Integer proTypeId) {
        Map<String, Object> result = new HashMap<>();
        try {
            ProType proType = proTypeService.getProTypeById(proTypeId)
                    .orElseThrow(() -> new RuntimeException("類別不存在"));
            result.put("success", true);
            result.put("proTypeId", proType.getProTypeId());
            result.put("proTypeName", proType.getProTypeName());
            result.put("productCount", proTypeService.countProductsByProType(proTypeId));
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }
}