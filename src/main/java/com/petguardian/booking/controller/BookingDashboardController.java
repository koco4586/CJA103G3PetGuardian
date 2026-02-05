package com.petguardian.booking.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.petguardian.booking.model.BookingFavoriteVO;
import com.petguardian.booking.model.BookingOrderVO;
import com.petguardian.booking.service.BookingService;
import com.petguardian.common.service.AuthStrategyService;
import com.petguardian.member.model.Member;
import com.petguardian.member.repository.register.MemberRegisterRepository;

import jakarta.servlet.http.HttpServletRequest;
@Controller
@RequestMapping("/dashboard")
public class BookingDashboardController {
    @Autowired
    private BookingService bookingService;
    @Autowired
    private AuthStrategyService authStrategyService;
    @Autowired
    private MemberRegisterRepository memberRepository;
    @GetMapping("/favorites/sitters") // 收藏的網址，可以改
    public String showSitterFavorites(HttpServletRequest request, Model model) {
    	// 1. 取得當前登入會員 ID
    	Integer memId = authStrategyService.getCurrentUserId(request);

        // 2. 取得收藏清單 (建議在 Service 處理好「補完保母資料」的邏輯)
    	List<BookingFavoriteVO> favorites = bookingService.getSitterFavoritesWithDetail(memId);
        // 3. 傳遞給前端
        model.addAttribute("sitterFavorites", favorites);
        
        return "frontend/dashboard-favorites-sitters";
    }
    
    @GetMapping("/bookings")
    public String listMemberOrders(
            @RequestParam(required = false) Integer status,
            HttpServletRequest request,
            Model model) {

        Integer memId = authStrategyService.getCurrentUserId(request);

        List<BookingOrderVO> bookingList;
        
        if (status != null) {
            // 只查詢該狀態的預約如：只查詢「待確認」的預約
            bookingList = bookingService.findByMemberAndStatus(memId, status);
        } else {
            // 查詢該會員的所有預約
            bookingList = bookingService.getOrdersByMemberId(memId);
        }

        // 用於側邊欄顯示會員頭像與基本資訊
        Member currentMember = memberRepository.findById(memId).orElse(null);
        if (currentMember != null) {
            model.addAttribute("currentMember", currentMember);
        }

        model.addAttribute("bookingList", bookingList);                           // 預約列表
        model.addAttribute("currentStatus", status);                              // 當前篩選的狀態（用於 UI 高亮）
        model.addAttribute("memId", memId);                                      // 會員 ID
        model.addAttribute("memName", authStrategyService.getCurrentUserName(request)); // 會員姓名

        return "frontend/dashboard-bookings";
    }
    @GetMapping("/booking/memberOrders")
    public String listMyFavoritesLegacy(HttpServletRequest request, Model model) {
        // 取得當前登入會員 ID
        Integer memId = authStrategyService.getCurrentUserId(request);

        // 查詢收藏清單（帶有保姆詳細資訊）
        List<BookingFavoriteVO> detailFavs = bookingService.getSitterFavoritesWithDetail(memId);

        // 傳遞給前端
        model.addAttribute("sitterFavorites", detailFavs);
        
        return "frontend/member-favorites";
    }
}
