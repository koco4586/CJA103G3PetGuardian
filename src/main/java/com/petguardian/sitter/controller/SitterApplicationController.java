package com.petguardian.sitter.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.petguardian.sitter.model.SitterApplicationVO;
import com.petguardian.sitter.service.SitterApplicationService;

import jakarta.servlet.http.HttpSession;

/**
 * 保姆申請控制器
 * 
 * 處理保姆申請相關的頁面導向與表單提交
 * 符合開發規範的 URL 命名 (kebab-case)
 */
@Controller
@RequestMapping("/sitter-application")
public class SitterApplicationController {

    @Autowired
    private SitterApplicationService service;

    /**
     * 導向申請表格頁面
     * URL: GET /sitter-application/apply
     */
    @GetMapping("/apply")
    public String showApplyForm(HttpSession session, RedirectAttributes redirectAttributes) {
        // 檢查是否已登入
        Integer memId = (Integer) session.getAttribute("memId");
        if (memId == null) {
            redirectAttributes.addFlashAttribute("error", "請先登入才能申請成為保姆");
            return "redirect:/member/login";
        }

        return "frontend/sitter/application-form"; // 對應 templates/frontend/sitter/application-form.html
    }

    /**
     * 處理申請送出
     * URL: POST /sitter-application/submit
     */
    @PostMapping("/submit")
    public String submitApplication(
            @RequestParam("intro") String intro,
            @RequestParam("experience") String experience,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        try {
            // 從 Session 取得當前登入會員 ID
            Integer memId = (Integer) session.getAttribute("memId");
            if (memId == null) {
                redirectAttributes.addFlashAttribute("error", "請先登入才能申請成為保姆");
                return "redirect:/member/login";
            }

            // 呼叫 Service 新增申請
            service.createApplication(memId, intro, experience);

            // 成功訊息
            redirectAttributes.addFlashAttribute("success", "申請已送出,請等待管理員審核");
            return "redirect:/sitter-application/list";

        } catch (IllegalArgumentException | IllegalStateException e) {
            // 業務邏輯錯誤 (如:會員不存在、重複申請)
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/sitter-application/apply";
        } catch (Exception e) {
            // 其他未預期錯誤
            redirectAttributes.addFlashAttribute("error", "系統錯誤,請稍後再試");
            return "redirect:/sitter-application/apply";
        }
    }

    /**
     * 查詢會員的申請列表
     * URL: GET /sitter-application/list
     */
    @GetMapping("/list")
    public String listApplications(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        // 從 Session 取得當前登入會員 ID
        Integer memId = (Integer) session.getAttribute("memId");
        if (memId == null) {
            redirectAttributes.addFlashAttribute("error", "請先登入");
            return "redirect:/member/login";
        }

        // 查詢該會員的所有申請
        List<SitterApplicationVO> list = service.getApplicationsByMember(memId);
        model.addAttribute("applications", list);

        return "frontend/sitter/application-list"; // 對應 templates/frontend/sitter/application-list.html
    }
}
