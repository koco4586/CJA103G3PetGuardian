package com.petguardian.sitter.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.petguardian.sitter.model.SitterApplicationDTO;
import com.petguardian.sitter.model.SitterApplicationVO;
import com.petguardian.sitter.service.SitterApplicationService;
// [NEW] Import SitterMemberVO/Repository
import com.petguardian.sitter.model.SitterMemberVO;
import com.petguardian.sitter.model.SitterMemberRepository;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

/**
 * 保姆申請控制器
 * 
 * 處理保姆申請相關的頁面導向與表單提交
 * 符合開發規範的 URL 命名 (kebab-case)
 */
@Controller
@RequestMapping("/sitter")
public class SitterApplicationController {

    @Autowired
    private com.petguardian.common.service.AuthStrategyService authStrategyService;

    @Autowired
    private SitterApplicationService service;

    // [NEW] 注入 SitterMemberRepository 用於查詢會員資料
    @Autowired
    private SitterMemberRepository sitterMemberRepository;

    /**
     * 導向申請表格頁面
     * URL: GET /sitter/apply
     * 
     * @param session            HttpSession 用於取得會員資訊
     * @param model              Spring Model 用於傳遞資料到視圖
     * @param redirectAttributes 用於重導向時傳遞訊息
     * @return 申請頁面路徑或重導向路徑
     */
    @GetMapping("/apply")
    public String showApplyForm(jakarta.servlet.http.HttpServletRequest request, HttpSession session, Model model,
            RedirectAttributes redirectAttributes) {

        Integer memId = authStrategyService.getCurrentUserId(request);
        if (memId == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "請先登入才能申請成為保姆");
            return "redirect:/member/login";
        }

        // ✅ 準備 Model 屬性
        // 1. 建立空的 DTO 供表單綁定
        SitterApplicationDTO dto = new SitterApplicationDTO();
        dto.setMemId(memId); // 預填 memId
        model.addAttribute("sitterApplication", dto);

        // 2. 從 AuthStrategy/Session 取得會員資訊

        // ========== [OLD] 舊版邏輯 (註解保留) ==========
        /*
         * String memName = authStrategyService.getCurrentUserName(request);
         * String memPhone = (String) session.getAttribute("memPhone");
         * String avatarUrl = (String) session.getAttribute("avatarUrl");
         */
        // ==============================================

        // ========== [NEW] 新版邏輯 (查詢資料庫) ==========
        String memName = "會員姓名";
        String memPhone = "未設定";
        String avatarUrl = (String) session.getAttribute("avatarUrl"); // 頭像暫時仍從 Session 拿，或依需求調整

        if (memId != null) {
            SitterMemberVO member = sitterMemberRepository.findById(memId).orElse(null);
            if (member != null) {
                memName = member.getMemName();
                memPhone = member.getMemTel(); // 從 DB 取得真實電話
            }
        }
        // ===============================================

        model.addAttribute("memName", memName != null ? memName : "會員姓名");
        model.addAttribute("memPhone", memPhone != null ? memPhone : "未設定");
        model.addAttribute("avatarUrl", avatarUrl);
        model.addAttribute("memberRole", "一般會員");

        // 3. 預留的預設值（可選）
        model.addAttribute("defaultCity", "台北市");
        model.addAttribute("defaultDistrict", "大安區");

        return "frontend/dashboard-sitter-registration";
    }

    /**
     * 處理申請送出
     * URL: POST /sitter/apply
     * 
     * @param dto                表單綁定物件
     * @param bindingResult      驗證結果
     * @param session            HttpSession
     * @param model              Spring Model
     * @param redirectAttributes RedirectAttributes
     * @return 成功或失敗的重導向路徑
     */
    @PostMapping("/apply")
    public String submitApplication(
            @Valid @ModelAttribute("sitterApplication") SitterApplicationDTO dto,
            BindingResult bindingResult,
            jakarta.servlet.http.HttpServletRequest request,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            // 檢查是否已登入
            Integer memId = authStrategyService.getCurrentUserId(request);
            if (memId == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "請先登入才能申請成為保姆");
                return "redirect:/member/login";
            }

            // ✅ 檢查驗證錯誤
            if (bindingResult.hasErrors()) {
                // 重新準備 Model 屬性（因為表單驗證失敗要重新顯示）
                prepareModelAttributes(request, session, model);
                return "frontend/dashboard-sitter-registration";
            }

            // 確保 memId 正確
            dto.setMemId(memId);

            // 呼叫 Service 新增申請
            service.createApplication(memId, dto.getAppIntro(), dto.getAppExperience());

            // 成功訊息
            redirectAttributes.addFlashAttribute("successMessage", "申請已送出，我們將在 1-3 個工作天內完成審核！");
            return "redirect:/sitter/apply";

        } catch (IllegalArgumentException | IllegalStateException e) {
            // 業務邏輯錯誤 (如:重複申請)
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/sitter/apply";
        } catch (Exception e) {
            // 其他未預期錯誤
            redirectAttributes.addFlashAttribute("errorMessage", "系統錯誤，請稍後再試");
            return "redirect:/sitter/apply";
        }
    }

    /**
     * 查詢會員的申請列表
     * URL: GET /sitter/applications
     * 
     * @param session            HttpSession
     * @param model              Spring Model
     * @param redirectAttributes RedirectAttributes
     * @return 申請列表頁面路徑
     */
    @GetMapping("/applications")
    public String listApplications(jakarta.servlet.http.HttpServletRequest request, HttpSession session, Model model,
            RedirectAttributes redirectAttributes) {
        // 從 Session 取得當前登入會員 ID
        Integer memId = authStrategyService.getCurrentUserId(request);
        if (memId == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "請先登入");
            return "redirect:/member/login";
        }

        // 查詢該會員的所有申請
        List<SitterApplicationVO> list = service.getApplicationsByMember(memId);
        model.addAttribute("applications", list);

        return "frontend/sitter/application-list";
    }

    /**
     * 輔助方法：準備 Model 屬性
     * 
     * 當表單驗證失敗時，重新載入頁面所需的顯示資料
     * 
     * @param session HttpSession
     * @param model   Spring Model
     */
    private void prepareModelAttributes(jakarta.servlet.http.HttpServletRequest request, HttpSession session,
            Model model) {
        String memName = authStrategyService.getCurrentUserName(request);
        String memPhone = (String) session.getAttribute("memPhone");
        String avatarUrl = (String) session.getAttribute("avatarUrl");

        model.addAttribute("memName", memName != null ? memName : "會員姓名");
        model.addAttribute("memPhone", memPhone != null ? memPhone : "未設定");
        model.addAttribute("avatarUrl", avatarUrl);
        model.addAttribute("memberRole", "一般會員");
        model.addAttribute("defaultCity", "台北市");
        model.addAttribute("defaultDistrict", "大安區");
    }
}
