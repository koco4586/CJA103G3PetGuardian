package com.petguardian.sitter.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public String showSettingsPage(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        // 1. 檢查登入
        Integer memId = (Integer) session.getAttribute("memId");
        if (memId == null) {
            // TODO: 測試模式，使用假資料
            memId = 1001;
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
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        try {
            Integer memId = (Integer) session.getAttribute("memId");
            if (memId == null) {
                memId = 1001; // TODO: 測試模式
            }

            SitterVO sitter = sitterService.getSitterByMemId(memId);
            if (sitter == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "您尚未成為保母");
                return "redirect:/sitter/apply";
            }

            // 更新保母名稱
            sitterService.updateSitterInfo(sitter.getSitterId(), sitterName, sitter.getSitterAdd());

            redirectAttributes.addFlashAttribute("successMessage", "保母名稱更新成功");

        } catch (Exception e) {
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
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        try {
            Integer memId = (Integer) session.getAttribute("memId");
            if (memId == null) {
                memId = 1001; // TODO: 測試模式
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
            petSitterService.setServicePrice(sitter.getSitterId(), serviceItemId, price);

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
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        try {
            Integer memId = (Integer) session.getAttribute("memId");
            if (memId == null) {
                memId = 1001; // TODO: 測試模式
            }

            SitterVO sitter = sitterService.getSitterByMemId(memId);
            if (sitter == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "您尚未成為保母");
                return "redirect:/sitter/apply";
            }

            // 刪除服務
            petSitterService.deleteService(sitter.getSitterId(), serviceItemId);

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
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        try {
            Integer memId = (Integer) session.getAttribute("memId");
            if (memId == null) {
                memId = 1001; // TODO: 測試模式
            }

            SitterVO sitter = sitterService.getSitterByMemId(memId);
            if (sitter == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "您尚未成為保母");
                return "redirect:/sitter/apply";
            }

            // 新增服務對象
            petSitterServicePetTypeService.addServicePetType(
                    sitter.getSitterId(), serviceItemId, typeId, sizeId);

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
            RedirectAttributes redirectAttributes) {

        try {
            // 刪除服務對象
            petSitterServicePetTypeService.deleteServicePetType(servicePetId);

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
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        try {
            Integer memId = (Integer) session.getAttribute("memId");
            if (memId == null) {
                memId = 1001; // TODO: 測試模式
            }

            SitterVO sitter = sitterService.getSitterByMemId(memId);
            if (sitter == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "您尚未成為保母");
                return "redirect:/sitter/apply";
            }

            // 新增服務地區
            serviceAreaService.addServiceArea(sitter.getSitterId(), areaId);

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
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        try {
            Integer memId = (Integer) session.getAttribute("memId");
            if (memId == null) {
                memId = 1001; // TODO: 測試模式
            }

            SitterVO sitter = sitterService.getSitterByMemId(memId);
            if (sitter == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "您尚未成為保母");
                return "redirect:/sitter/apply";
            }

            // 刪除服務地區
            serviceAreaService.deleteServiceArea(sitter.getSitterId(), areaId);

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
    public Map<String, Object> updateSchedule(@RequestBody Map<String, Object> requestData, HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 1. 檢查登入
            Integer memId = (Integer) session.getAttribute("memId");
            if (memId == null) {
                memId = 1001; // TODO: 測試模式
            }

            // 2. 查詢保母資料
            SitterVO sitter = sitterService.getSitterByMemId(memId);
            if (sitter == null) {
                response.put("success", false);
                response.put("message", "找不到保母資料");
                return response;
            }

            // 3. 解析前端傳來的資料
            // 注意：service_time 只能存 24 小時，但前端傳來的是一週七天的資料
            // 策略：如果任何一天的某個時段是「可預約」，就設為可預約
            @SuppressWarnings("unchecked")
            Map<String, Map<String, String>> scheduleData = (Map<String, Map<String, String>>) requestData
                    .get("scheduleData");

            // 4. 建立 24 小時的狀態字串（合併七天的資料）
            System.out.println("=== 解析前端資料 (Daily Mode) ===");
            System.out.println("scheduleData keys: " + scheduleData.keySet());

            char[] serviceTimeArray = new char[24];
            // 初始化為全部不可預約
            for (int i = 0; i < 24; i++) {
                serviceTimeArray[i] = '0';
            }

            // 遍歷七天的資料
            for (int day = 0; day < 7; day++) {
                String dayKey = String.valueOf(day);
                if (scheduleData.containsKey(dayKey)) {
                    Map<String, String> daySchedule = scheduleData.get(dayKey);

                    for (int hour = 0; hour < 24; hour++) {
                        String hourStr = String.valueOf(hour);
                        if (daySchedule.containsKey(hourStr)) {
                            String status = daySchedule.get(hourStr);
                            // 0: 可預約
                            // service_time: 0=不可預約, 1=可預約
                            if (status.equals("0")) {
                                // 只要任何一天這個時段是可預約，就設為可預約
                                serviceTimeArray[hour] = '1';
                            }
                        }
                    }
                }
            }

            String serviceTime = new String(serviceTimeArray);

            // Debug: 印出要儲存的資料
            System.out.println("=== 儲存行程 Debug ===");
            System.out.println("memId: " + memId);
            System.out.println("sitterId: " + sitter.getSitterId());
            System.out.println("serviceTime: " + serviceTime);
            System.out.println("=====================");

            // 5. 更新資料庫
            sitterService.updateServiceTime(sitter.getSitterId(), serviceTime);

            response.put("success", true);
            response.put("message", "儲存成功");

        } catch (Exception e) {
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
