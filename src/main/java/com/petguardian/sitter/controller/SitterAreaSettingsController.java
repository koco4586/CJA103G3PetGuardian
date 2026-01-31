package com.petguardian.sitter.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.petguardian.area.model.AreaVO;
import com.petguardian.area.service.AreaService;
import com.petguardian.common.service.AuthStrategyService;
import com.petguardian.service.service.ServiceAreaService;
import com.petguardian.sitter.model.SitterVO;
import com.petguardian.sitter.service.SitterService;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 保母服務地區設定 Controller
 * 
 * 負責處理保母的服務地區新增、刪除以及行政區查詢
 * 
 * [Refactored] 從 SitterProfileController 拆分出來
 * URL 前綴: /sitter/profile (保持原路徑相容性)
 */
@Controller
@RequestMapping("/sitter/profile")
public class SitterAreaSettingsController {

    @Autowired
    private AuthStrategyService authStrategyService;

    @Autowired
    private SitterService sitterService;

    @Autowired
    private ServiceAreaService serviceAreaService;

    @Autowired
    private AreaService areaService;

    /**
     * 新增服務地區
     * URL: POST /sitter/profile/area/add
     */
    @PostMapping("/area/add")
    public String addServiceArea(
            @RequestParam Integer areaId,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        try {
            Integer memId = authStrategyService.getCurrentUserId(request);
            if (memId == null) {
                return "redirect:/front/loginpage";
            }

            // 新增服務地區
            serviceAreaService.addServiceAreaForMember(memId, areaId);

            redirectAttributes.addFlashAttribute("successMessage", "服務地區新增成功");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "新增失敗：" + e.getMessage());
        }

        return "redirect:/sitter/profile/settings";
    }

    /**
     * 刪除服務地區
     * URL: POST /sitter/profile/area/delete
     */
    @PostMapping("/area/delete")
    public String deleteServiceArea(
            @RequestParam Integer areaId,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        try {
            Integer memId = authStrategyService.getCurrentUserId(request);
            if (memId == null) {
                return "redirect:/front/loginpage";
            }

            // 刪除服務地區
            serviceAreaService.deleteServiceAreaForMember(memId, areaId);

            redirectAttributes.addFlashAttribute("successMessage", "服務地區已移除");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "移除失敗：" + e.getMessage());
        }

        return "redirect:/sitter/profile/settings";
    }

    /**
     * 根據縣市查詢地區（AJAX API）
     * URL: GET /sitter/profile/districts?city=台北市
     */
    @GetMapping("/districts")
    @ResponseBody
    public List<AreaVO> getDistrictsByCity(@RequestParam String city) {
        return areaService.getDistrictsByCity(city);
    }
}
