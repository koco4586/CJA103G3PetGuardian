package com.petguardian.sitter.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Arrays;
import java.util.stream.Collectors;

import com.petguardian.petsitter.model.PetType;
import com.petguardian.petsitter.model.PetSize;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.petguardian.area.model.AreaVO;
import com.petguardian.area.service.AreaService;
import com.petguardian.petsitter.model.PetSitterServicePetTypeVO;
import com.petguardian.petsitter.model.PetSitterServiceVO;
import com.petguardian.petsitter.service.PetSitterService;
import com.petguardian.petsitter.service.PetSitterServicePetTypeService;
import com.petguardian.service.model.ServiceAreaVO;
import com.petguardian.service.service.ServiceAreaService;
import com.petguardian.sitter.model.SitterVO;
import com.petguardian.sitter.service.SitterService;

import jakarta.servlet.http.HttpSession;

/**
 * 保母個人設定 Controller
 * 
 * 負責保母個人資料、服務項目、服務對象、服務地區的設定
 * URL 前綴: /sitter/profile
 */
@Controller
@RequestMapping("/sitter/profile")
public class SitterProfileController {

    @Autowired
    private com.petguardian.common.service.AuthStrategyService authStrategyService;

    @Autowired
    private SitterService sitterService;

    @Autowired
    private PetSitterService petSitterService;

    @Autowired
    private PetSitterServicePetTypeService petSitterServicePetTypeService;

    @Autowired
    private ServiceAreaService serviceAreaService;

    @Autowired
    private AreaService areaService;

    /**
     * 顯示保母個人設定頁面
     * URL: GET /sitter/profile/settings
     * 
     * @param session            HttpSession 用於取得登入會員 ID
     * @param model              Spring Model 用於傳遞保姆資料、服務、地區等資訊
     * @param redirectAttributes RedirectAttributes 用於傳遞錯誤或成功訊息
     * @return String 設定頁面路徑 "frontend/sitter/profile-settings"
     */
    @GetMapping("/settings")
    public String showSettingsPage(jakarta.servlet.http.HttpServletRequest request, Model model,
            RedirectAttributes redirectAttributes) {
        // 1. 檢查登入
        Integer memId = authStrategyService.getCurrentUserId(request);
        if (memId == null) {
            return "redirect:/front/loginpage";
        }

        // 2. 查詢保母資料
        SitterVO sitter = sitterService.getSitterByMemId(memId);
        if (sitter == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "您尚未成為保母");
            return "redirect:/sitter/apply";
        }

        // 3. 查詢保母的所有服務
        List<PetSitterServiceVO> services = petSitterService.getServicesBySitter(sitter.getSitterId());

        // 4. 查詢保母的所有服務對象
        List<PetSitterServicePetTypeVO> servicePetTypes = petSitterServicePetTypeService
                .getServicePetTypesBySitter(sitter.getSitterId());

        // 5. 查詢保母的所有服務地區
        List<ServiceAreaVO> serviceAreas = serviceAreaService.getServiceAreasBySitter(sitter.getSitterId());

        // 6. 查詢所有可用縣市 (改為縣市 + AJAX 連動地區)
        List<String> availableCities = areaService.getAllCities();
        model.addAttribute("availableCities", availableCities);

        // [NEW] 準備 Enum 資料供前端選單與列表顯示
        model.addAttribute("petTypeOptions", PetType.values());
        model.addAttribute("petSizeOptions", PetSize.values());

        Map<Integer, String> petTypeMap = Arrays.stream(PetType.values())
                .collect(Collectors.toMap(PetType::getId, PetType::getLabel));
        Map<Integer, String> petSizeMap = Arrays.stream(PetSize.values())
                .collect(Collectors.toMap(PetSize::getId, PetSize::getLabel));

        // 傳統寫法對照
        // Map<Integer, String> petSizeMap = new HashMap<>();
        // for (PetSize size : PetSize.values()) {
        // petSizeMap.put(size.getId(), size.getLabel());
        // }

        model.addAttribute("petTypeMap", petTypeMap);
        model.addAttribute("petSizeMap", petSizeMap);

        // 7. 準備 Model
        model.addAttribute("sitter", sitter);
        model.addAttribute("services", services);
        model.addAttribute("servicePetTypes", servicePetTypes);
        model.addAttribute("serviceAreas", serviceAreas);

        return "frontend/sitter/profile-settings";
    }

    /**
     * 更新保母名稱
     * URL: POST /sitter/profile/update-name
     * 
     * @param sitterName         String 新的保姆名稱
     * @param session            HttpSession 用於取得登入會員 ID
     * @param redirectAttributes RedirectAttributes 用於傳遞操作結果訊息
     * @return String 重導向回設定頁面
     */
    @PostMapping("/update-name")
    public String updateSitterName(
            @RequestParam String sitterName,
            jakarta.servlet.http.HttpServletRequest request,
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

            // 更新保母名稱
            sitterService.updateSitterInfo(sitter.getSitterId(), sitterName, sitter.getSitterAdd());

            redirectAttributes.addFlashAttribute("successMessage", "保母名稱更新成功");

        } catch (

        Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "更新失敗：" + e.getMessage());
        }

        return "redirect:/sitter/profile/settings";
    }

    /**
     * 設定服務價格
     * URL: POST /sitter/profile/service/set
     * 
     * @param serviceItemId      Integer 服務項目 ID
     * @param price              Integer 設定的價格 (需在 400-1000 之間)
     * @param session            HttpSession 用於取得登入會員 ID
     * @param redirectAttributes RedirectAttributes 用於傳遞操作結果訊息
     * @return String 重導向回設定頁面
     */
    @PostMapping("/service/set")
    public String setServicePrice(
            @RequestParam Integer serviceItemId,
            @RequestParam Integer price,
            jakarta.servlet.http.HttpServletRequest request,
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
     * 
     * @param serviceItemId      Integer 欲刪除的服務項目 ID
     * @param session            HttpSession 用於驗證保姆身分
     * @param redirectAttributes RedirectAttributes 用於傳遞操作結果訊息
     * @return String 重導向回設定頁面
     */
    @PostMapping("/service/delete")
    public String deleteService(
            @RequestParam Integer serviceItemId,
            jakarta.servlet.http.HttpServletRequest request,
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
     * 
     * @param serviceItemId      Integer 服務項目 ID
     * @param typeId             Integer 寵物種類 ID
     * @param sizeId             Integer 體型 ID
     * @param session            HttpSession 用於驗證保姆身分
     * @param redirectAttributes RedirectAttributes 用於傳遞操作結果訊息
     * @return String 重導向回設定頁面
     */
    @PostMapping("/service/pet-type/add")
    public String addServicePetType(
            @RequestParam Integer serviceItemId,
            @RequestParam Integer typeId,
            @RequestParam Integer sizeId,
            jakarta.servlet.http.HttpServletRequest request,
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
     * 
     * @param servicePetId       Integer 服務對象關聯 ID
     * @param redirectAttributes RedirectAttributes 用於傳遞操作結果訊息
     * @return String 重導向回設定頁面
     */
    @PostMapping("/service/pet-type/delete")
    public String deleteServicePetType(
            @RequestParam Integer servicePetId,
            jakarta.servlet.http.HttpServletRequest request,
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

    /**
     * 新增服務地區
     * URL: POST /sitter/profile/area/add
     * 
     * @param areaId             Integer 地區 ID
     * @param session            HttpSession 用於驗證保姆身分
     * @param redirectAttributes RedirectAttributes 用於傳遞操作結果訊息
     * @return String 重導向回設定頁面
     */
    @PostMapping("/area/add")
    public String addServiceArea(
            @RequestParam Integer areaId,
            jakarta.servlet.http.HttpServletRequest request,
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
     * 
     * @param areaId             Integer 欲刪除的地區 ID
     * @param session            HttpSession 用於驗證保姆身分
     * @param redirectAttributes RedirectAttributes 用於傳遞操作結果訊息
     * @return String 重導向回設定頁面
     */
    @PostMapping("/area/delete")
    public String deleteServiceArea(
            @RequestParam Integer areaId,
            jakarta.servlet.http.HttpServletRequest request,
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
     * 更新保母行程設定（營業時間）
     * URL: POST /sitter/profile/update-schedule
     * 
     * @param requestData JSON 格式資料，包含 "scheduleData" (七天 24 小時的預約狀態)
     * @param session     HttpSession 用於取得登入會員 ID
     * @return Map&lt;String, Object&gt; 回傳 JSON 結果 (success, message)
     */
    @PostMapping("/update-schedule")
    @ResponseBody
    public Map<String, Object> updateSchedule(@RequestBody Map<String, Object> requestData,
            jakarta.servlet.http.HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 1. 檢查登入
            Integer memId = authStrategyService.getCurrentUserId(request);
            if (memId == null) {
                response.put("success", false);
                response.put("message", "請先登入");
                return response;
            }

            // 2. 查詢保母資料
            SitterVO sitter = sitterService.getSitterByMemId(memId);
            if (sitter == null) {
                response.put("success", false);
                response.put("message", "找不到保母資料");
                return response;
            }

            // 3. 解析前端傳來的資料 (邏輯移至 Service)
            @SuppressWarnings("unchecked")
            Map<String, Map<String, String>> scheduleData = (Map<String, Map<String, String>>) requestData
                    .get("scheduleData");

            // 4. Update via Service
            sitterService.updateWeeklySchedule(sitter.getSitterId(), scheduleData);

            response.put("success", true);
            response.put("message", "儲存成功");

        } catch (

        Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return response;
    }

    /**
     * 根據縣市查詢地區（AJAX API）
     * URL: GET /sitter/profile/districts?city=台北市
     * 
     * @param city String 縣市名稱
     * @return List&lt;AreaVO&gt; 該縣市的行政區列表 JSON
     */
    @GetMapping("/districts")
    @ResponseBody
    public List<AreaVO> getDistrictsByCity(@RequestParam String city) {
        return areaService.getDistrictsByCity(city);
    }
}
