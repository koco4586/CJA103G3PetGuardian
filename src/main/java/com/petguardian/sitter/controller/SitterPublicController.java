package com.petguardian.sitter.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.petguardian.sitter.model.SitterSearchCriteria;
import com.petguardian.sitter.model.SitterSearchDTO;
import com.petguardian.sitter.model.SitterVO;
import com.petguardian.sitter.model.SitterMemberVO;
import com.petguardian.sitter.model.SitterMemberDTO;
import com.petguardian.sitter.service.SitterService;

import com.petguardian.petsitter.service.PetSitterService;
import com.petguardian.service.service.ServiceAreaService;
import com.petguardian.booking.model.BookingOrderRepository;
import com.petguardian.petsitter.model.PetSitterServiceVO;
import com.petguardian.service.model.ServiceAreaVO;
import com.petguardian.booking.model.BookingOrderVO;
import com.petguardian.common.service.AuthStrategyService;

// [Refactored] Use Service interfaces instead of Repositories
import com.petguardian.area.service.AreaService;
import com.petguardian.petsitter.service.PetSitterServicePetTypeService;
import com.petguardian.petsitter.model.PetSitterServicePetTypeVO;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 公開的保姆搜尋功能 Controller
 * 提供不需登入即可訪問的保姆搜尋和查看功能
 */
@Controller
@RequestMapping("/public/sitter")
public class SitterPublicController {

    @Autowired
    private SitterService sitterService;

    @Autowired
    private AuthStrategyService authStrategyService;

    @Autowired
    private PetSitterService petSitterService;

    @Autowired
    private ServiceAreaService serviceAreaService;

    @Autowired
    private BookingOrderRepository bookingOrderRepository;

    @Autowired
    private AreaService areaService;

    @Autowired
    private PetSitterServicePetTypeService petSitterServicePetTypeService;

    /**
     * 顯示公開的保姆搜尋頁面
     * URL: /public/sitter/search
     * 
     * @param model Spring Model 用於傳遞目前登入會員資訊(currentMember/currentMemberId)
     * @return String 保姆搜尋頁面路徑 "frontend/sitter/public-search"
     */
    /*
     * @GetMapping("/search")
     * public String showSearchPage(HttpServletRequest request, Model model) {
     * // 從資料庫撈取會員資料 (如果已登入)
     * Integer memId = authStrategyService.getCurrentUserId(request);
     * 
     * if (memId != null) {
     * SitterMemberVO memberVO = sitterService.getSitterMemberById(memId);
     * if (memberVO != null) {
     * SitterMemberDTO fakeMember = SitterMemberDTO.fromEntity(memberVO);
     * model.addAttribute("currentMember", fakeMember);
     * }
     * model.addAttribute("currentMemberId", memId);
     * 
     * // [NEW] 查詢是否為保姆，並傳遞 sitterId 供前端隱藏自己卡片
     * SitterVO mySitter = sitterService.getSitterByMemId(memId);
     * if (mySitter != null) {
     * model.addAttribute("currentSitterId", mySitter.getSitterId());
     * }
     * }
     * 
     * return "frontend/sitter/public-search";
     * }
     */

    /**
     * AJAX API：根據條件搜尋保姆
     * URL: /public/sitter/search/api
     * 
     * @param criteria SitterSearchCriteria 包含搜尋條件的 DTO (如地區、服務類型等)
     * @return ResponseEntity<List<SitterSearchDTO>> 符合條件的保姆列表 JSON
     */
    @PostMapping("/search/api")
    @ResponseBody
    public ResponseEntity<List<SitterSearchDTO>> searchSitters(@RequestBody SitterSearchCriteria criteria) {
        try {
            List<SitterSearchDTO> results;

            if (!criteria.hasFilters()) {
                results = sitterService.getAllActiveSitters();
            } else {
                results = sitterService.searchSitters(criteria);
            }

            return ResponseEntity.ok(results);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * AJAX API：取得所有啟用的保姆（無篩選）
     * URL: /public/sitter/search/all
     * 
     * @return ResponseEntity<List<SitterSearchDTO>> 所有狀態為啟用的保姆列表 JSON
     */
    @GetMapping("/search/all")
    @ResponseBody
    public ResponseEntity<List<SitterSearchDTO>> getAllSitters() {
        try {
            List<SitterSearchDTO> results = sitterService.getAllActiveSitters();
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * AJAX API：取得所有縣市列表
     * URL: /public/sitter/cities
     * 
     * @return ResponseEntity<List<String>> 資料庫中所有不重複的縣市名稱列表
     */
    @GetMapping("/cities")
    @ResponseBody
    public ResponseEntity<List<String>> getAllCities() {
        try {
            List<String> cities = areaService.getAllCities();
            return ResponseEntity.ok(cities);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * AJAX API：根據縣市取得所有區域
     * URL: /public/sitter/districts
     * 
     * @param city String 縣市名稱 (例如: "臺北市")
     * @return ResponseEntity<List<Map<String, Object>>>
     *         該縣市的所有行政區列表 (包含 areaId, district)
     */
    @GetMapping("/districts")
    @ResponseBody
    public ResponseEntity<List<java.util.Map<String, Object>>> getDistrictsByCity(
            @org.springframework.web.bind.annotation.RequestParam String city) {
        try {
            List<com.petguardian.area.model.AreaVO> areas = areaService.getDistrictsByCity(city);
            List<java.util.Map<String, Object>> result = new java.util.ArrayList<>();

            for (com.petguardian.area.model.AreaVO area : areas) {
                java.util.Map<String, Object> map = new java.util.HashMap<>();
                map.put("areaId", area.getAreaId());
                map.put("district", area.getDistrict());
                result.add(map);
            }

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 顯示保姆詳情頁面
     * URL: /public/sitter/detail/{sitterId}
     */
    @GetMapping("/detail/{sitterId}")
    public String showSitterDetail(@PathVariable Integer sitterId, HttpServletRequest request,
            Model model) {
        try {
            // 1. 查詢保姆基本資料 (若無則導回列表)
            SitterVO sitter = sitterService.getSitterById(sitterId);
            if (sitter == null) {
                return "redirect:/public/sitter/search";
            }

            // 2. 查詢完整資料 (新增部分)
            // 服務項目
            List<PetSitterServiceVO> services = petSitterService.getServicesBySitter(sitterId);
            // 服務地區
            List<ServiceAreaVO> serviceAreas = serviceAreaService.getServiceAreasBySitter(sitterId);

            // [NEW] 服務對象 (寵物種類與體型)
            List<PetSitterServicePetTypeVO> petTypes = petSitterServicePetTypeService
                    .getServicePetTypesBySitter(sitterId);

            // [NEW] 建立服務項目名稱對照表 (模擬資料庫)
            // 對應前端 profile-settings.html: 1=散步, 2=餵食, 3=洗澡
            Map<Integer, String> serviceNameMap = new HashMap<>();
            serviceNameMap.put(1, "散步");
            serviceNameMap.put(2, "餵食");
            serviceNameMap.put(3, "洗澡");

            // [NEW] 建立服務價格對照表 (Service ID -> Price)
            Map<Integer, Integer> servicePriceMap = new HashMap<>();
            if (services != null) {
                for (PetSitterServiceVO service : services) {
                    servicePriceMap.put(service.getServiceItemId(), service.getDefaultPrice());
                }
            }

            /*
             * [FUTURE] 未來功能：查詢保姆的會員頭像
             * SitterMemberVO sitterMember =
             * sitterService.getSitterMemberById(sitter.getMemId());
             * if (sitterMember != null) {
             * model.addAttribute("sitterMember", sitterMember);
             * }
             */

            // 歷史評價 (僅查詢有評分的訂單)
            // List<BookingOrderVO> reviews = bookingOrderRepository
            // .findBySitterIdAndSitterRatingNotNullOrderByEndTimeDesc(sitterId);

            // 3. 處理會員登入資訊 (保留原有邏輯)
            Integer memId = authStrategyService.getCurrentUserId(request);
            if (memId != null) {
                SitterMemberVO memberVO = sitterService.getSitterMemberById(memId);
                if (memberVO != null) {
                    SitterMemberDTO fakeMember = SitterMemberDTO.fromEntity(memberVO);
                    model.addAttribute("currentMember", fakeMember);
                }
            }

            // 4. 將所有資料加入 Model 傳遞給前端
            model.addAttribute("sitter", sitter);
            model.addAttribute("services", services);
            model.addAttribute("serviceAreas", serviceAreas);
            model.addAttribute("petTypes", petTypes); // [NEW] 傳遞服務對象
            model.addAttribute("serviceNameMap", serviceNameMap); // [NEW] 傳遞服務名稱對照表
            model.addAttribute("servicePriceMap", servicePriceMap); // [NEW] 傳遞服務價格對照表
            // model.addAttribute("reviews", reviews);

            return "frontend/sitter/sitter-detail";

        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/public/sitter/search";
        }
    }
}
