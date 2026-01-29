package com.petguardian.home.controller;

import com.petguardian.seller.model.Product;
import com.petguardian.sitter.model.SitterVO;
import com.petguardian.sitter.service.SitterService;
import com.petguardian.store.service.StoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * 首頁控制器
 *
 * 負責處理首頁路由，並提供熱門保母與熱門商品資料
 */
@Controller
public class HomeController {

    @Autowired
    private SitterService sitterService;

    @Autowired
    private StoreService storeService;

    /**
     * 首頁
     * URL: / 或 /index
     * 模板: templates/frontend/index.html
     */
    @GetMapping({ "/", "/index" })
    public String home(Model model) {
        // 取得所有啟用中的保母（前3位作為熱門保母）
        List<SitterVO> allSitters = sitterService.getSittersByStatus((byte) 0);
        List<SitterVO> featuredSitters = (allSitters != null)
                ? allSitters.stream().limit(3).toList()
                : List.of();

        // 取得所有上架商品（前3件作為熱門商品）
        List<Product> allProducts = storeService.getAllActiveProducts();
        List<Product> featuredProducts = (allProducts != null)
                ? allProducts.stream().limit(3).toList()
                : List.of();

        model.addAttribute("featuredSitters", featuredSitters);
        model.addAttribute("featuredProducts", featuredProducts);

        return "frontend/index";
    }
}
