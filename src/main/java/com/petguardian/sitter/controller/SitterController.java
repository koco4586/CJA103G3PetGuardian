package com.petguardian.sitter.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.petguardian.sitter.model.SitterApplicationVO;
import com.petguardian.sitter.model.SitterVO;
import com.petguardian.sitter.service.SitterApplicationService;
import com.petguardian.sitter.service.SitterService;

/**
 * 後台保母管理 Controller
 * 
 * 負責審核保母申請、管理保母帳號權限
 * URL 前綴: /admin/sitter
 */
@Controller
@RequestMapping("/admin/sitter")
public class SitterController {

    @Autowired
    private SitterApplicationService applicationService;

    @Autowired
    private SitterService sitterService;

    /**
     * 顯示後台保母管理頁面
     * URL: GET /admin/sitter/manage
     * 
     * @param model Spring Model
     * @return 後台管理頁面路徑
     */
    @GetMapping("/manage")
    public String showManagePage(Model model) {
        // 取得所有申請和保母列表
        List<SitterApplicationVO> applications = applicationService.getAllApplications();
        List<SitterVO> sitters = sitterService.getAllSitters();

        model.addAttribute("applications", applications);
        model.addAttribute("sitters", sitters);

        return "backend/sitter/sitters";
    }

    /**
     * 審核通過申請
     * URL: POST /admin/sitter/approve/{appId}
     * 
     * @param appId              申請編號
     * @param reviewNote         審核備註
     * @param redirectAttributes RedirectAttributes
     * @return 重導向至管理頁面
     */
    @PostMapping("/approve/{appId}")
    public String approveApplication(
            @PathVariable Integer appId,
            @RequestParam(required = false) String reviewNote,
            RedirectAttributes redirectAttributes) {

        System.out.println("=== 審核通過請求 ===");
        System.out.println("申請 ID: " + appId);
        System.out.println("審核備註: " + reviewNote);

        try {
            SitterApplicationVO application = applicationService.getApplicationById(appId);

            if (application == null) {
                System.out.println("錯誤: 申請不存在");
                redirectAttributes.addFlashAttribute("errorMessage", "申請不存在");
                return "redirect:/admin/sitter/manage";
            }

            if (application.getAppStatus() != 0) {
                System.out.println("錯誤: 此申請已審核過，狀態: " + application.getAppStatus());
                redirectAttributes.addFlashAttribute("errorMessage", "此申請已審核過");
                return "redirect:/admin/sitter/manage";
            }

            // 更新申請狀態為「通過」
            // Service 內部會自動處理：建立保母資料、更新會員狀態
            System.out.println("開始審核通過流程...");
            applicationService.reviewApplication(appId, (byte) 1, reviewNote);
            System.out.println("審核通過流程完成！");

            redirectAttributes.addFlashAttribute("successMessage",
                    "申請已通過！保母帳號已建立。");

        } catch (Exception e) {
            System.out.println("審核失敗: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "審核失敗：" + e.getMessage());
        }

        return "redirect:/admin/sitter/manage";
    }

    /**
     * 駁回申請
     * URL: POST /admin/sitter/reject/{appId}
     * 
     * @param appId              申請編號
     * @param reviewNote         駁回原因 (必填)
     * @param redirectAttributes RedirectAttributes
     * @return 重導向至管理頁面
     */
    @PostMapping("/reject/{appId}")
    public String rejectApplication(
            @PathVariable Integer appId,
            @RequestParam String reviewNote,
            RedirectAttributes redirectAttributes) {

        try {
            if (reviewNote == null || reviewNote.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "請填寫駁回原因");
                return "redirect:/admin/sitter/manage";
            }

            SitterApplicationVO application = applicationService.getApplicationById(appId);

            if (application == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "申請不存在");
                return "redirect:/admin/sitter/manage";
            }

            if (application.getAppStatus() != 0) {
                redirectAttributes.addFlashAttribute("errorMessage", "此申請已審核過");
                return "redirect:/admin/sitter/manage";
            }

            // 更新申請狀態為「未通過」
            applicationService.reviewApplication(appId, (byte) 2, reviewNote);

            redirectAttributes.addFlashAttribute("successMessage", "申請已駁回。");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "駁回失敗：" + e.getMessage());
        }

        return "redirect:/admin/sitter/manage";
    }

    /**
     * 停權保母帳號
     * URL: POST /admin/sitter/suspend/{sitterId}
     * 
     * @param sitterId           保姆編號
     * @param redirectAttributes RedirectAttributes
     * @return 重導向至管理頁面
     */
    @PostMapping("/suspend/{sitterId}")
    public String suspendSitter(
            @PathVariable Integer sitterId,
            RedirectAttributes redirectAttributes) {

        try {
            SitterVO sitter = sitterService.getSitterById(sitterId);

            if (sitter == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "保母不存在");
                return "redirect:/admin/sitter/manage";
            }

            if (sitter.getSitterStatus() == 1) {
                redirectAttributes.addFlashAttribute("errorMessage", "此保母已停權");
                return "redirect:/admin/sitter/manage";
            }

            // 更新狀態為「停權」
            sitterService.updateSitterStatus(sitterId, (byte) 1);

            redirectAttributes.addFlashAttribute("successMessage", "保母帳號已停權");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "停權失敗：" + e.getMessage());
        }

        return "redirect:/admin/sitter/manage";
    }

    /**
     * 恢復保母帳號
     * URL: POST /admin/sitter/restore/{sitterId}
     * 
     * @param sitterId           保姆編號
     * @param redirectAttributes RedirectAttributes
     * @return 重導向至管理頁面
     */
    @PostMapping("/restore/{sitterId}")
    public String restoreSitter(
            @PathVariable Integer sitterId,
            RedirectAttributes redirectAttributes) {

        try {
            SitterVO sitter = sitterService.getSitterById(sitterId);

            if (sitter == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "保母不存在");
                return "redirect:/admin/sitter/manage";
            }

            if (sitter.getSitterStatus() == 0) {
                redirectAttributes.addFlashAttribute("errorMessage", "此保母已啟用");
                return "redirect:/admin/sitter/manage";
            }

            // 更新狀態為「啟用」
            sitterService.updateSitterStatus(sitterId, (byte) 0);

            redirectAttributes.addFlashAttribute("successMessage", "保母帳號已恢復");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "恢復失敗：" + e.getMessage());
        }

        return "redirect:/admin/sitter/manage";
    }
}
