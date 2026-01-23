package com.petguardian.area.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.petguardian.area.model.AreaVO;
import com.petguardian.area.service.AreaService;

/**
 * 地區管理 Controller
 * 
 * 提供地區查詢功能 (主要用於開發/測試)
 * URL 前綴: /area
 */
@Controller
@RequestMapping("/area")
public class AreaController {

    @Autowired
    AreaService areaService;

    /**
     * 顯示所有地區列表頁面
     * URL: GET /area/list-all-area
     * 
     * @param model Spring MVC Model,用於傳遞資料給前端
     * @return Thymeleaf 模板路徑
     */
    @GetMapping("list-all-area")
    public String listAllArea(Model model) {
        List<AreaVO> areaList = areaService.getAll();
        model.addAttribute("areaList", areaList);
        return "frontend/area/list-all-area";
    }

    /**
     * 根據縣市查詢地區
     * URL: GET /area/get-districts-by-city?city=台北市
     * 
     * @param city  縣市名稱 (從 URL 參數取得)
     * @param model Spring MVC ModelMap,用於傳遞資料給前端
     * @return Thymeleaf 模板路徑
     */
    @GetMapping("get-districts-by-city")
    public String getDistrictsByCity(
            @RequestParam("city") String city,
            ModelMap model) {

        // 驗證輸入
        if (city == null || city.trim().isEmpty()) {
            model.addAttribute("errorMsgs", "請輸入縣市名稱");
            model.addAttribute("areaList", areaService.getAll());
            return "frontend/area/list-all-area";
        }

        // 查詢資料
        List<AreaVO> areaList = areaService.getDistrictsByCity(city);

        // 查無資料處理
        if (areaList == null || areaList.isEmpty()) {
            model.addAttribute("errorMsgs", "查無「" + city + "」的相關地區");
            model.addAttribute("areaList", areaService.getAll());
            return "frontend/area/list-all-area";
        }

        // 有資料,顯示結果
        model.addAttribute("areaList", areaList);
        model.addAttribute("selectedCity", city);
        return "frontend/area/list-all-area";
    }
}
