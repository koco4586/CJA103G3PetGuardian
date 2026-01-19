package com.petguardian.productfavoritelist.controller;

import com.petguardian.productfavoritelist.service.ProductFavoriteListService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 商品收藏控制器
 * 處理收藏相關的頁面路由與表單提交
 */
@Controller
public class ProductFavoriteListController {

    private static final Integer TEST_MEM_ID = 1001;

    @Autowired
    private ProductFavoriteListService favoriteService;

    /**
     * 取得當前會員 ID（含模擬登入邏輯）
     */
    private Integer getCurrentMemId(HttpSession session) {
        Integer memId = (Integer) session.getAttribute("memId");
        if (memId == null) {
            memId = TEST_MEM_ID;
            session.setAttribute("memId", memId);
        }
        return memId;
    }

    /**
     * 收藏列表頁面（會員中心）
     * 注意：此路由已在 ProductPageController 定義，此處僅供參考
     */
    // @GetMapping("/dashboard/favorites")
    // public String favoritesPage(Model model, HttpSession session) {
    // Integer memId = getCurrentMemId(session);
    // List<Map<String, Object>> favorites =
    // favoriteService.getFavoritesWithProductInfo(memId);
    // model.addAttribute("favorites", favorites);
    // model.addAttribute("memId", memId);
    // return "frontend/orders/dashboard-favorites";
    // }

    /**
     * 加入收藏 (API 備用)
     */
    @PostMapping("/api/favorites/add")
    @ResponseBody
    public Map<String, Object> addFavoriteApi(@RequestParam Integer proId,
            HttpSession session) {
        Integer memId = getCurrentMemId(session);
        return favoriteService.toggleFavorite(memId, proId);
    }

    /**
     * 取消收藏 (API 備用)
     */
    @PostMapping("/api/favorites/remove")
    @ResponseBody
    public Map<String, Object> removeFavoriteApi(@RequestParam Integer proId,
            HttpSession session) {
        Integer memId = getCurrentMemId(session);
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
        Integer memId = getCurrentMemId(session);
        return favoriteService.toggleFavorite(memId, proId);
    }
}
