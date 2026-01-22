package com.petguardian.productfavoritelist.controller;

import com.petguardian.common.service.AuthService;
import com.petguardian.productfavoritelist.service.ProductFavoriteListService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 商品收藏控制器
 * 處理收藏相關的頁面路由與表單提交
 */
@Controller
public class ProductFavoriteListController {

//    private static final Integer TEST_MEM_ID = 1001;

    @Autowired
    private ProductFavoriteListService favoriteService;

    @Autowired
    private AuthService authService;

    /**
     * 取得當前會員 ID（含模擬登入邏輯）
     */
//    private Integer getCurrentMemId(HttpSession session) {
//        Integer memId = (Integer) session.getAttribute("memId");
//        if (memId == null) {
//            memId = TEST_MEM_ID;
//            session.setAttribute("memId", memId);
//        }
//        return memId;
//    }

    /**
     * 會員中心 - 收藏列表
     * GET /dashboard/favorites
     */
    @GetMapping("/dashboard/favorites")
    public String dashboardFavoritesPage(Model model, HttpSession session) {
        // 檢查是否已登入
        if (!authService.isLoggedIn(session)) {
            return "redirect:/store";
        }
        Integer memId = authService.getCurrentMemId(session);

        List<Map<String, Object>> favorites = favoriteService.getFavoritesWithProductInfo(memId);
        model.addAttribute("favorites", favorites);
        model.addAttribute("memId", memId);

        return "frontend/orders/dashboard-favorites";
    }

    /**
     * 加入收藏
     * POST /favorites/add
     */
    @PostMapping("/favorites/add")
    public String addFavorite(@RequestParam Integer proId,
            @RequestParam(required = false, defaultValue = "/store") String redirectUrl,
            HttpSession session,
            RedirectAttributes redirectAttr) {
        Integer memId = authService.getCurrentMemId(session);

        try {
            favoriteService.addFavorite(memId, proId);
            redirectAttr.addFlashAttribute("message", "已加入收藏");
        } catch (Exception e) {
            redirectAttr.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:" + redirectUrl;
    }

    /**
     * 取消收藏
     * POST /favorites/remove
     */
    @PostMapping("/favorites/remove")
    public String removeFavorite(@RequestParam Integer proId,
            @RequestParam(required = false, defaultValue = "/dashboard/favorites") String redirectUrl,
            HttpSession session,
            RedirectAttributes redirectAttr) {
        Integer memId = authService.getCurrentMemId(session);

        try {
            favoriteService.removeFavorite(memId, proId);
            redirectAttr.addFlashAttribute("message", "已取消收藏");
        } catch (Exception e) {
            redirectAttr.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:" + redirectUrl;
    }

    /**
     * 加入收藏 (API 備用)
     */
    @PostMapping("/api/favorites/add")
    @ResponseBody
    public Map<String, Object> addFavoriteApi(@RequestParam Integer proId,
            HttpSession session) {
        Integer memId = authService.getCurrentMemId(session);
        return favoriteService.toggleFavorite(memId, proId);
    }

    /**
     * 取消收藏 (API 備用)
     */
    @PostMapping("/api/favorites/remove")
    @ResponseBody
    public Map<String, Object> removeFavoriteApi(@RequestParam Integer proId,
            HttpSession session) {
        Integer memId = authService.getCurrentMemId(session);
        favoriteService.removeFavorite(memId, proId);
        return Map.of("success", true, "favorited", false);
    }

    /**
     * 切換收藏狀態 (API 備用)
     */
    @PostMapping("/api/favorites/toggle")
    @ResponseBody
    public Map<String, Object> toggleFavoriteApi(@RequestParam Integer proId,
            HttpSession session) {
        Integer memId = authService.getCurrentMemId(session);
        return favoriteService.toggleFavorite(memId, proId);
    }
}
