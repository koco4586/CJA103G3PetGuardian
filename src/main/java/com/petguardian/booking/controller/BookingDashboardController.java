package com.petguardian.booking.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import com.petguardian.booking.model.BookingFavoriteVO;
import com.petguardian.booking.service.BookingService;
import com.petguardian.common.service.AuthStrategyService;
import jakarta.servlet.http.HttpServletRequest;
@Controller
@RequestMapping("/dashboard")
public class BookingDashboardController {
    @Autowired
    private BookingService bookingService;
    @Autowired
    private AuthStrategyService authStrategyService;
    @GetMapping("/favorites/sitters") // 收藏的網址，可以改
    public String showSitterFavorites(HttpServletRequest request, Model model) {
    	// 1. 取得當前登入會員 ID
    	Integer memId = authStrategyService.getCurrentUserId(request);
        
        if (memId == null) {
            return "redirect:/front/loginpage";
        }
        // 2. 取得收藏清單 (建議在 Service 處理好「補完保母資料」的邏輯)
        List<BookingFavoriteVO> favorites = bookingService.getSitterFavoritesByMember(memId);
        // 3. 傳遞給前端
        model.addAttribute("sitterFavorites", favorites);
        
        return "frontend/dashboard-favorites-sitters";
    }
}
