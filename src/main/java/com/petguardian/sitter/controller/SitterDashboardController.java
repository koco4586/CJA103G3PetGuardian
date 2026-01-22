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
}
