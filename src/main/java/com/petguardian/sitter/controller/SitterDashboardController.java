package com.petguardian.sitter.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.petguardian.booking.model.BookingScheduleVO;
import com.petguardian.sitter.model.SitterVO;
import com.petguardian.sitter.service.SitterService;
import com.petguardian.sitter.model.SitterDashboardDTO;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 保姆主頁面控制器
 * 
 * 負責顯示保姆儀表板資訊，包含服務數量、地區數量等統計數據
 */
@Controller
@RequestMapping("/sitter")
public class SitterDashboardController {

    @Autowired
    private SitterService sitterService;

    @Autowired
    private com.petguardian.common.service.AuthStrategyService authStrategyService;

    /**
     * 保母主頁 (Dashboard)
     * URL: /sitter/dashboard
     * 
     * @param request HttpServletRequest 用於取得登入會員 ID
     * @param model   Spring Model 用於傳遞保姆資料與統計數據
     * @return 儀表板頁面路徑 (frontend/sitter/dashboard) 或重導向至申請頁
     */
    @GetMapping("/dashboard")
    // public String dashboard(jakarta.servlet.http.HttpServletRequest request,
    // Model model) {
    public String dashboard(HttpServletRequest request, Model model,
            RedirectAttributes redirectAttributes) {
        // 1. 檢查登入
        Integer memId = authStrategyService.getCurrentUserId(request);
        if (memId == null) {
            return "redirect:/front/loginpage";
        }

        // 2. 獲取儀表板整合資料
        SitterDashboardDTO dashboardData = sitterService.getDashboardData(memId);

        // 如果還不是保母，導向申請頁 (DTO 為 null 表示找不到對應保姆)
        if (dashboardData == null) {
            return "redirect:/sitter/apply";
        }

        SitterVO sitter = dashboardData.getSitter();

        // 3. 檢查停權狀態 (sitterStatus == 1, 停權)
        if (sitter.getSitterStatus() != null && sitter.getSitterStatus() == 1) {
            redirectAttributes.addFlashAttribute("errorMessage", "您已經被停權,無法使用保姆的服務,如有疑問請聯繫管理員處理");
            return "redirect:/front/managementpage";
        }

        // 4. 準備 Model (維持與原本 View 的相容性)
        model.addAttribute("sitter", sitter);
        model.addAttribute("serviceTime", sitter.getServiceTime());
        model.addAttribute("serviceCount", dashboardData.getServiceCount());
        model.addAttribute("areaCount", dashboardData.getAreaCount());
        model.addAttribute("services", dashboardData.getServices());
        model.addAttribute("areas", dashboardData.getAreas());
        model.addAttribute("averageRating", dashboardData.getAverageRating());

        // 待確認訂單數量
        model.addAttribute("pendingCount", dashboardData.getPendingOrderCount());

        return "frontend/sitter/dashboard";
    }

    /**
     * API: 獲取保母特定月份的排程狀態
     * URL: /sitter/api/schedule?year=2026&month=1
     */
    @GetMapping("/api/schedule")
    @ResponseBody
    public List<BookingScheduleVO> getSchedule(
            HttpServletRequest request,
            @RequestParam int year,
            @RequestParam int month) {

        Integer memId = authStrategyService.getCurrentUserId(request);
        if (memId == null)
            return List.of();

        return sitterService.getScheduleByMember(memId, year, month);
    }

    /**
     * API: 儲存單日排程
     * URL: POST /sitter/api/schedule
     * Payload: { "date": "2026-01-01", "status": "00000..." }
     * 
     * [Refactor Note]
     * 改寫用途：標準化 API 回傳格式
     * 改寫方法：將回傳型別從 String 改為 ResponseEntity<?>，並以 JSON 格式回傳訊息。
     * 這樣前端 AJAX 可以根據 HTTP 狀態碼 (200, 400, 401, 500) 進行更精確的錯誤處理。
     */
    @PostMapping("/api/schedule")
    @ResponseBody
    public ResponseEntity<?> saveSchedule(
            HttpServletRequest request,
            @RequestBody Map<String, String> payload) {

        Integer memId = authStrategyService.getCurrentUserId(request);
        if (memId == null)
            return ResponseEntity.status(401).body(Map.of("message", "請先登入"));

        try {
            String dateStr = payload.get("date");
            String status = payload.get("status");

            if (dateStr == null || status == null || status.length() != 24) {
                return ResponseEntity.badRequest().body(Map.of("message", "資料格式錯誤"));
            }

            java.time.LocalDate date = java.time.LocalDate.parse(dateStr);

            sitterService.updateScheduleForMember(memId, date, status);

            return ResponseEntity.ok(Map.of("message", "儲存成功"));

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("message", "儲存失敗: " + e.getMessage()));
        }
    }
}
