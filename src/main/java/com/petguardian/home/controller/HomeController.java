package com.petguardian.home.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 首頁控制器
 *
 * 負責處理首頁路由
 */
@Controller
public class HomeController {

    /**
     * 首頁
     * URL: / 或 /index
     * 模板: templates/frontend/index.html
     */
    @GetMapping({"/", "/index"})
    public String home() {
        return "frontend/index";
    }
}
