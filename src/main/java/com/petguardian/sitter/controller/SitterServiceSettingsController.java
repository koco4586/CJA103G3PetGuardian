package com.petguardian.sitter.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.petguardian.common.service.AuthStrategyService;
import com.petguardian.petsitter.service.PetSitterService;
import com.petguardian.petsitter.service.PetSitterServicePetTypeService;
import com.petguardian.sitter.model.SitterVO;
import com.petguardian.sitter.service.SitterService;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 保母服務設定 Controller
 * 
 * 負責處理保母的服務項目(價格)與服務對象(寵物種類/體型)的設定
 * 
 * [Refactored] 從 SitterProfileController 拆分出來
 * URL 前綴: /sitter/profile/service
 */
@Controller
@RequestMapping("/sitter/profile/service")
public class SitterServiceSettingsController {

    @Autowired
    private AuthStrategyService authStrategyService;

    @Autowired
    private SitterService sitterService;

    @Autowired
    private PetSitterService petSitterService;

    @Autowired
    private PetSitterServicePetTypeService petSitterServicePetTypeService;

    /**
     * 設定服務價格
     * URL: POST /sitter/profile/service/set
     */
    @PostMapping("/set")
    public String setServicePrice(
            @RequestParam Integer serviceItemId,
            @RequestParam Integer price,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        try {
            Integer memId = authStrategyService.getCurrentUserId(request);
            if (memId == null) {
                return "redirect:/front/loginpage";
            }

            SitterVO sitter = sitterService.getSitterByMemId(memId);
            if (sitter == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "您尚未成為保母");
                return "redirect:/sitter/apply";
            }

            // 驗證價格範圍
            if (price < 400 || price > 1000) {
                redirectAttributes.addFlashAttribute("errorMessage", "價格必須在 400-1000 元之間");
                return "redirect:/sitter/profile/settings";
            }

            // 設定服務價格
            petSitterService.setServicePriceForMember(memId, serviceItemId, price);

            redirectAttributes.addFlashAttribute("successMessage", "服務價格設定成功");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "設定失敗：" + e.getMessage());
        }

        return "redirect:/sitter/profile/settings";
    }

    /**
     * 刪除服務
     * URL: POST /sitter/profile/service/delete
     */
    @PostMapping("/delete")
    public String deleteService(
            @RequestParam Integer serviceItemId,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        try {
            Integer memId = authStrategyService.getCurrentUserId(request);
            if (memId == null) {
                return "redirect:/front/loginpage";
            }

            SitterVO sitter = sitterService.getSitterByMemId(memId);
            if (sitter == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "您尚未成為保母");
                return "redirect:/sitter/apply";
            }

            // 刪除服務
            petSitterService.deleteServiceForMember(memId, serviceItemId);

            redirectAttributes.addFlashAttribute("successMessage", "服務已移除");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "移除失敗：" + e.getMessage());
        }

        return "redirect:/sitter/profile/settings";
    }

    /**
     * 新增服務對象（寵物種類+體型）
     * URL: POST /sitter/profile/service/pet-type/add
     */
    @PostMapping("/pet-type/add")
    public String addServicePetType(
            @RequestParam Integer serviceItemId,
            @RequestParam Integer typeId,
            @RequestParam Integer sizeId,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        try {
            Integer memId = authStrategyService.getCurrentUserId(request);
            if (memId == null) {
                return "redirect:/front/loginpage";
            }

            SitterVO sitter = sitterService.getSitterByMemId(memId);
            if (sitter == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "您尚未成為保母");
                return "redirect:/sitter/apply";
            }

            // 新增服務對象
            petSitterServicePetTypeService.addServicePetTypeForMember(memId, serviceItemId, typeId, sizeId);

            redirectAttributes.addFlashAttribute("successMessage", "服務對象新增成功");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "新增失敗：" + e.getMessage());
        }

        return "redirect:/sitter/profile/settings";
    }

    /**
     * 刪除服務對象
     * URL: POST /sitter/profile/service/pet-type/delete
     */
    @PostMapping("/pet-type/delete")
    public String deleteServicePetType(
            @RequestParam Integer servicePetId,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        try {
            Integer memId = authStrategyService.getCurrentUserId(request);
            if (memId == null) {
                return "redirect:/front/loginpage";
            }
            // 刪除服務對象
            petSitterServicePetTypeService.deleteServicePetTypeForMember(memId, servicePetId);

            redirectAttributes.addFlashAttribute("successMessage", "服務對象已移除");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "移除失敗：" + e.getMessage());
        }

        return "redirect:/sitter/profile/settings";
    }
}
