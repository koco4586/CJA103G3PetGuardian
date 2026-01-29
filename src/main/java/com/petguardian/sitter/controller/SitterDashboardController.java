//package com.petguardian.sitter.controller;
//
//import java.util.List;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//
//import com.petguardian.booking.model.BookingOrderVO;
//import com.petguardian.booking.service.BookingService;
//import com.petguardian.petsitter.model.PetSitterServiceVO;
//import com.petguardian.petsitter.service.PetSitterService;
//import com.petguardian.service.model.ServiceAreaVO;
//import com.petguardian.service.service.ServiceAreaService;
//import com.petguardian.sitter.model.SitterVO;
//import com.petguardian.sitter.service.SitterService;
//
///**
// * 保姆主頁面控制器
// * 
// * 負責顯示保姆儀表板資訊，包含服務數量、地區數量等統計數據
// */
//@Controller
//@RequestMapping("/sitter")
//public class SitterDashboardController {
//
//    @Autowired
//    private SitterService sitterService;
//
//    @Autowired
//    private PetSitterService petSitterService;
//
//    @Autowired
//    private ServiceAreaService serviceAreaService;
//
//    @Autowired
//    private com.petguardian.common.service.AuthStrategyService authStrategyService;
//
//    @Autowired
//    private BookingService bookingService;
//
//    /**
//     * 保母主頁 (Dashboard)
//     * URL: /sitter/dashboard
//     * 
//     * @param request HttpServletRequest 用於取得登入會員 ID
//     * @param model   Spring Model 用於傳遞保姆資料與統計數據
//     * @return 儀表板頁面路徑 (frontend/sitter/dashboard) 或重導向至申請頁
//     */
//    @GetMapping("/dashboard")
//    public String dashboard(jakarta.servlet.http.HttpServletRequest request, Model model) {
//        // 1. 檢查登入
//        Integer memId = authStrategyService.getCurrentUserId(request);
//        if (memId == null) {
//            return "redirect:/front/loginpage";
//        }
//
//        // 2. 查詢保母資料
//        SitterVO sitter = sitterService.getSitterByMemId(memId);
//
//        // 如果還不是保母，導向申請頁
//        if (sitter == null) {
//            return "redirect:/sitter/apply";
//        }
//
//        // 3. 查詢重點數據
//        // 服務數量
//        List<PetSitterServiceVO> services = petSitterService.getServicesBySitter(sitter.getSitterId());
//        int serviceCount = services.size();
//
//        // 服務地區數量
//        List<ServiceAreaVO> areas = serviceAreaService.getServiceAreasBySitter(sitter.getSitterId());
//        int areaCount = areas.size();
//
//        // 4. 準備 Model
//        System.out.println("=== Dashboard 載入 Debug ===");
//        System.out.println("sitter.getSitterId(): " + sitter.getSitterId());
//        System.out.println("sitter.getSitterName(): " + sitter.getSitterName());
//        System.out.println("sitter.getServiceTime(): " + sitter.getServiceTime());
//        System.out.println("===========================");
//
//        // 6. 計算平均評分 (改用 SitterVO 方法)
//        double averageRating = sitter.getAverageRating();
//
//        model.addAttribute("sitter", sitter);
//        model.addAttribute("serviceTime", sitter.getServiceTime()); // 單獨傳遞，避免物件狀態問題
//        model.addAttribute("serviceCount", serviceCount);
//        model.addAttribute("areaCount", areaCount);
//        model.addAttribute("services", services); // 新增詳細列表
//        model.addAttribute("areas", areas); // 新增詳細列表
//        model.addAttribute("averageRating", averageRating); // 新增平均評分
//
//        // [New] 查詢待確認訂單數量 (狀態=0)
//        List<BookingOrderVO> pendingOrders = bookingService.findOrdersBySitterAndStatus(sitter.getSitterId(), 0);
//        int pendingCount = (pendingOrders != null) ? pendingOrders.size() : 0;
//        model.addAttribute("pendingCount", pendingCount);
//
//        return "frontend/sitter/dashboard";
//    }
//
//    /**
//     * API: 獲取保母特定月份的排程狀態
//     * URL: /sitter/api/schedule?year=2026&month=1
//     */
//    @GetMapping("/api/schedule")
//    @org.springframework.web.bind.annotation.ResponseBody
//    public List<com.petguardian.booking.model.BookingScheduleVO> getSchedule(
//            jakarta.servlet.http.HttpServletRequest request,
//            @org.springframework.web.bind.annotation.RequestParam int year,
//            @org.springframework.web.bind.annotation.RequestParam int month) {
//
//        Integer memId = authStrategyService.getCurrentUserId(request);
//        if (memId == null)
//            return List.of();
//
//        return sitterService.getScheduleByMember(memId, year, month);
//    }
//
//    /**
//     * API: 儲存單日排程
//     * URL: POST /sitter/api/schedule
//     * Payload: { "date": "2026-01-01", "status": "00000..." }
//     */
//    @org.springframework.web.bind.annotation.PostMapping("/api/schedule")
//    @org.springframework.web.bind.annotation.ResponseBody
//    public String saveSchedule(
//            jakarta.servlet.http.HttpServletRequest request,
//            @org.springframework.web.bind.annotation.RequestBody java.util.Map<String, String> payload) {
//
//        Integer memId = authStrategyService.getCurrentUserId(request);
//        if (memId == null)
//            return "fail: not authorized";
//
//        try {
//            String dateStr = payload.get("date");
//            String status = payload.get("status");
//
//            if (dateStr == null || status == null || status.length() != 24) {
//                return "fail: invalid data";
//            }
//
//            java.time.LocalDate date = java.time.LocalDate.parse(dateStr);
//
//            sitterService.updateScheduleForMember(memId, date, status);
//
//            return "success";
//
//        } catch (Exception e) {
//            return "fail: " + e.getMessage();
//        }
//    }
//}
