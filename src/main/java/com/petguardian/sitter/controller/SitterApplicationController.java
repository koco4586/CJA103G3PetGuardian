package com.petguardian.sitter.controller;

import java.util.List;
import java.util.Map;

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
import com.petguardian.sitter.model.SitterVO;
import com.petguardian.sitter.service.SitterApplicationService;
import com.petguardian.sitter.service.SitterService;

import com.petguardian.common.service.AuthStrategyService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.ConstraintViolationException;
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

    @Autowired // AuthStrategyService做介面做登入驗證 登入驗證的規格書
    private AuthStrategyService authStrategyService;

    @Autowired
    private SitterApplicationService service;

    @Autowired
    private SitterService sitterService;

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
    public String showApplyForm(HttpServletRequest request, HttpSession session, Model model,
            RedirectAttributes redirectAttributes) {

        Integer memId = authStrategyService.getCurrentUserId(request);
        if (memId == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "請先登入才能申請成為保姆");
            return "redirect:/front/loginpage";
        }

        if (!model.containsAttribute("successMessage")) {
            // [Refactor] 改用 Service 檢查狀態
            String statusMsg = service.checkApplicationStatus(memId);
            if (statusMsg != null) {
                model.addAttribute("errorMessage", statusMsg);
                model.addAttribute("isDisableSubmit", true);
            }
        }

        // [NEW] 檢查是否已通過審核 (改用 Service 方法)
        if (service.isSitter(memId)) {
            // 進一步檢查是否被停權
            SitterVO sitter = sitterService.getSitterByMemId(memId);
            if (sitter != null && sitter.getSitterStatus() != null && sitter.getSitterStatus() == 1) {
                // 如果是被停權，則停留在當前頁面並顯示錯誤訊息 (讓前端 JS 彈出 SweetAlert)
                model.addAttribute("errorMessage", "您已被停權,請向管理員聯繫處理");

                // 為了讓頁面正常渲染，還是需要準備基本資料
                SitterApplicationDTO dto = new SitterApplicationDTO();
                dto.setMemId(memId);
                model.addAttribute("sitterApplication", dto);

                String avatarUrl = (String) session.getAttribute("avatarUrl");
                Map<String, Object> initData = service.getApplyFormInitData(memId, avatarUrl);
                model.addAllAttributes(initData);

                return "frontend/sitter/dashboard-sitter-registration";
            }

            return "redirect:/sitter/dashboard";// 導回保姆個人頁面
        }

        // ✅ 準備 Model 屬性
        // 1. 建立空的 DTO 供表單綁定
        SitterApplicationDTO dto = new SitterApplicationDTO();
        dto.setMemId(memId); // 預填 memId
        model.addAttribute("sitterApplication", dto);

        // 2. [Refactor] 改用 Service 取得初始資料
        // 2. [Refactor] 改用 Service 取得初始資料
        String avatarUrl = (String) session.getAttribute("avatarUrl");
        Map<String, Object> initData = service.getApplyFormInitData(memId, avatarUrl);
        model.addAllAttributes(initData);

        return "frontend/sitter/dashboard-sitter-registration";
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
            HttpServletRequest request,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            // 檢查是否已登入
            Integer memId = authStrategyService.getCurrentUserId(request);
            if (memId == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "請先登入才能申請成為保姆");
                return "redirect:/front/loginpage";
            }

            // ✅ 檢查驗證錯誤
            if (bindingResult.hasErrors()) {
                // 重新準備 Model 屬性（因為表單驗證失敗要重新顯示）
                prepareModelAttributes(request, session, model);
                return "frontend/sitter/dashboard-sitter-registration";
            }

            // 確保 memId 正確
            dto.setMemId(memId);

            // 呼叫 Service 新增申請
            service.createApplication(memId, dto.getAppIntro(), dto.getAppExperience());

            // 成功訊息
            redirectAttributes.addFlashAttribute("successMessage", "申請已送出，我們將在 1-3 個工作天內完成審核！");
            return "redirect:/sitter/apply";

        } catch (IllegalArgumentException | IllegalStateException e) {
            // 業務邏輯錯誤 (如:重複申請) -> 保留資料並顯示錯誤
            model.addAttribute("errorMessage", e.getMessage());
            prepareModelAttributes(request, session, model);
            return "frontend/sitter/dashboard-sitter-registration";

        } catch (ConstraintViolationException e) {
            // Hibernate 驗證錯誤 -> 提取並顯示具體的驗證訊息
            StringBuilder errorMsg = new StringBuilder("表單驗證失敗:\n");
            e.getConstraintViolations().forEach(violation -> {
                errorMsg.append("• ").append(violation.getMessage()).append("\n");
            });
            model.addAttribute("errorMessage", errorMsg.toString());
            prepareModelAttributes(request, session, model);
            return "frontend/sitter/dashboard-sitter-registration";

        } catch (Exception e) {
            // 其他未預期錯誤 -> 保留資料並顯示友善的錯誤訊息
            // 記錄完整錯誤到日誌供開發人員查看
            e.printStackTrace(); // 或使用 logger.error("申請提交失敗", e);

            // 只顯示友善的錯誤訊息給使用者,不暴露技術細節
            model.addAttribute("errorMessage", "系統錯誤,請稍後再試。如問題持續發生,請聯繫客服人員。");
            prepareModelAttributes(request, session, model);
            return "frontend/sitter/dashboard-sitter-registration";
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
    public String listApplications(HttpServletRequest request, HttpSession session, Model model,
            RedirectAttributes redirectAttributes) {
        // 從 Session 取得當前登入會員 ID
        Integer memId = authStrategyService.getCurrentUserId(request);
        if (memId == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "請先登入");
            return "redirect:/front/loginpage";
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
    private void prepareModelAttributes(HttpServletRequest request, HttpSession session,
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

    /**
     * [NEW] 保姆專區入口 (前端 Header 連結至此)
     * URL: GET /sitter/hub
     * 
     * 邏輯：
     * 1. 檢查使用者是否登入 -> 未登入轉 login
     * 2. 檢查使用者是否有「已通過 (Status=1)」的申請紀錄
     * - 是 -> 導向到保姆主頁 (sitter-dashboard.html)
     * - 否 -> 導向到申請頁面 (sitter/apply)
     */
    @GetMapping("/hub")
    public String checkSitterStatus(HttpServletRequest request) {
        // 1. 取得當前會員 ID
        Integer memId = authStrategyService.getCurrentUserId(request);
        if (memId == null) {
            return "redirect:/front/loginpage";
        }

        // 2. 查詢該會員是否有「已通過」的保姆資格 (改用 Service 方法)
        if (service.isSitter(memId)) {
            return "redirect:/sitter/dashboard"; // 前往保姆主頁
        } else {
            return "redirect:/sitter/apply"; // 前往申請頁面
        }
    }
}
