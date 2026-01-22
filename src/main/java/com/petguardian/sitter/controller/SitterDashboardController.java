package com.petguardian.sitter.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.petguardian.petsitter.model.PetSitterServiceVO;
import com.petguardian.petsitter.service.PetSitterService;
import com.petguardian.service.model.ServiceAreaVO;
import com.petguardian.service.service.ServiceAreaService;
import com.petguardian.sitter.model.SitterVO;
import com.petguardian.sitter.service.SitterService;

import jakarta.servlet.http.HttpSession;

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
    private PetSitterService petSitterService;

    @Autowired
    private ServiceAreaService serviceAreaService;

    /**
     * 保姆主頁 (Dashboard)
     * URL: /sitter/dashboard
     * 
     * @param session HttpSession 用於取得登入會員 ID
     * @param model   Spring Model 用於傳遞保姆資料與統計數據
     * @return 儀表板頁面路徑 (frontend/sitter/dashboard) 或重導向至申請頁
     */
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        // 1. 檢查登入
        Integer memId = (Integer) session.getAttribute("memId");
        if (memId == null) {
            memId = 1001; // TODO: 測試模式 (周小禾)
        }

        // 2. 查詢保母資料
        SitterVO sitter = sitterService.getSitterByMemId(memId);

        // 如果還不是保母，導向申請頁
        if (sitter == null) {
            return "redirect:/sitter/apply";
        }

        // 3. 查詢重點數據
        // 服務數量
        List<PetSitterServiceVO> services = petSitterService.getServicesBySitter(sitter.getSitterId());
        int serviceCount = services.size();

        // 服務地區數量
        List<ServiceAreaVO> areas = serviceAreaService.getServiceAreasBySitter(sitter.getSitterId());
        int areaCount = areas.size();

        // 4. 準備 Model
        System.out.println("=== Dashboard 載入 Debug ===");
        System.out.println("sitter.getSitterId(): " + sitter.getSitterId());
        System.out.println("sitter.getSitterName(): " + sitter.getSitterName());
        System.out.println("sitter.getServiceTime(): " + sitter.getServiceTime());
        System.out.println("===========================");

        model.addAttribute("sitter", sitter);
        model.addAttribute("serviceTime", sitter.getServiceTime()); // 單獨傳遞，避免物件狀態問題
        model.addAttribute("serviceCount", serviceCount);
        model.addAttribute("areaCount", areaCount);
        model.addAttribute("services", services); // 新增詳細列表
        model.addAttribute("areas", areas); // 新增詳細列表

        return "frontend/sitter/dashboard";
    }

    @Autowired
    private com.petguardian.booking.model.BookingScheduleRepository bookingScheduleRepository;

    /**
     * API: 獲取保母特定月份的排程狀態
     * URL: /sitter/api/schedule?year=2026&month=1
     */
    @GetMapping("/api/schedule")
    @org.springframework.web.bind.annotation.ResponseBody
    public List<com.petguardian.booking.model.BookingScheduleVO> getSchedule(
            HttpSession session,
            @org.springframework.web.bind.annotation.RequestParam int year,
            @org.springframework.web.bind.annotation.RequestParam int month) {

        Integer memId = (Integer) session.getAttribute("memId");
        if (memId == null)
            memId = 1001; // 測試用

        SitterVO sitter = sitterService.getSitterByMemId(memId);
        if (sitter == null)
            return List.of();

        // 計算該月份的開始與結束日期
        java.time.LocalDate startDate = java.time.LocalDate.of(year, month, 1);
        java.time.LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        // 使用 Repository 查詢區間內的資料 (需確認 Repository 是否支援區間查詢，若無則暫時用迴圈或 findAll 過濾，
        // 但為了效能建議在 Repository 加方法。這邊先示範用 findAll 過濾，或者若您 Repository 沒這方法，我先略過
        // Repository 修改，
        // 改用單日查詢拼湊? 不，這樣效能太差。
        // 讓我檢查一下 BookingScheduleRepository 是否有區間查詢。
        // 剛才的 file_view 顯示 BookingScheduleRepository 只有 findBySitterIdAndScheduleDate。
        // 為了不更動 Repository (用戶只准改這兩支)，我這裡先用 findAll 並在 Java 層過濾 (雖然效能較差但符合「只改兩支檔案」的限制)。
        // 或者更聰明的方式：因為資料量不大，這樣做是可以接受的。

        List<com.petguardian.booking.model.BookingScheduleVO> allSchedules = bookingScheduleRepository.findAll();
        return allSchedules.stream()
                .filter(s -> s.getSitterId().equals(sitter.getSitterId()))
                .filter(s -> !s.getScheduleDate().isBefore(startDate) && !s.getScheduleDate().isAfter(endDate))
                .toList();
    }

    /**
     * API: 儲存單日排程
     * URL: POST /sitter/api/schedule
     * Payload: { "date": "2026-01-01", "status": "00000..." }
     */
    @org.springframework.web.bind.annotation.PostMapping("/api/schedule")
    @org.springframework.web.bind.annotation.ResponseBody
    public String saveSchedule(
            HttpSession session,
            @org.springframework.web.bind.annotation.RequestBody java.util.Map<String, String> payload) {

        Integer memId = (Integer) session.getAttribute("memId");
        if (memId == null)
            memId = 1001;

        SitterVO sitter = sitterService.getSitterByMemId(memId);
        if (sitter == null)
            return "fail: not authorized";

        String dateStr = payload.get("date");
        String status = payload.get("status");

        if (dateStr == null || status == null || status.length() != 24) {
            return "fail: invalid data";
        }

        java.time.LocalDate date = java.time.LocalDate.parse(dateStr);

        com.petguardian.booking.model.BookingScheduleVO schedule = bookingScheduleRepository
                .findBySitterIdAndScheduleDate(sitter.getSitterId(), date)
                .orElse(new com.petguardian.booking.model.BookingScheduleVO());

        if (schedule.getScheduleId() == null) {
            schedule.setSitterId(sitter.getSitterId());
            schedule.setScheduleDate(date);
        }

        schedule.setBookingStatus(status);
        bookingScheduleRepository.save(schedule);

        return "success";
    }
}
