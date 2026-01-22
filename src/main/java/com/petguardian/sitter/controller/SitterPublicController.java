package com.petguardian.sitter.controller;

import java.util.List;
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
import com.petguardian.sitter.model.SitterMemberRepository;
import com.petguardian.sitter.model.SitterMemberVO;
import com.petguardian.sitter.model.SitterMemberDTO;
import com.petguardian.sitter.service.SitterService;

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
    private SitterMemberRepository sitterMemberRepository;

    @Autowired
    private com.petguardian.common.service.AuthStrategyService authStrategyService;

    @Autowired
    private com.petguardian.area.model.AreaRepository areaRepository;

    /**
     * 顯示公開的保姆搜尋頁面
     * URL: /public/sitter/search
     * 
     * @param model Spring Model 用於傳遞目前登入會員資訊(currentMember/currentMemberId)
     * @return String 保姆搜尋頁面路徑 "frontend/sitter/public-search"
     */
    @GetMapping("/search")
    public String showSearchPage(jakarta.servlet.http.HttpServletRequest request, Model model) {
        // 從資料庫撈取會員資料 (如果已登入)
        Integer memId = authStrategyService.getCurrentUserId(request);

        if (memId != null) {
            Optional<SitterMemberVO> memberVO = sitterMemberRepository.findById(memId);
            if (memberVO.isPresent()) {
                SitterMemberDTO fakeMember = SitterMemberDTO.fromEntity(memberVO.get());
                model.addAttribute("currentMember", fakeMember);
            }
            model.addAttribute("currentMemberId", memId);
        }

        return "frontend/sitter/public-search";
    }

    /**
     * AJAX API：根據條件搜尋保姆
     * URL: /public/sitter/search/api
     * 
     * @param criteria SitterSearchCriteria 包含搜尋條件的 DTO (如地區、服務類型等)
     * @return ResponseEntity&lt;List&lt;SitterSearchDTO&gt;&gt; 符合條件的保姆列表 JSON
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
     * @return ResponseEntity&lt;List&lt;SitterSearchDTO&gt;&gt; 所有狀態為啟用的保姆列表 JSON
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
     * @return ResponseEntity&lt;List&lt;String&gt;&gt; 資料庫中所有不重複的縣市名稱列表
     */
    @GetMapping("/cities")
    @ResponseBody
    public ResponseEntity<List<String>> getAllCities() {
        try {
            List<String> cities = areaRepository.findAllCities();
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
     * @return ResponseEntity&lt;List&lt;Map&lt;String, Object&gt;&gt;&gt;
     *         該縣市的所有行政區列表 (包含 areaId, district)
     */
    @GetMapping("/districts")
    @ResponseBody
    public ResponseEntity<List<java.util.Map<String, Object>>> getDistrictsByCity(
            @org.springframework.web.bind.annotation.RequestParam String city) {
        try {
            List<com.petguardian.area.model.AreaVO> areas = areaRepository.findByCityName(city);
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
     * 
     * @param sitterId Integer 保姆編號 (Path Variable)
     * @param model    Spring Model 用於傳遞保姆詳細資訊(sitter)與會員資訊
     * @return String 保姆詳情頁面路徑 "frontend/sitter/sitter-detail"，若保姆不存在則導回搜尋頁
     */
    @GetMapping("/detail/{sitterId}")
    public String showSitterDetail(@PathVariable Integer sitterId, jakarta.servlet.http.HttpServletRequest request,
            Model model) {
        try {
            SitterVO sitter = sitterService.getSitterById(sitterId);

            if (sitter == null) {
                return "redirect:/public/sitter/search";
            }

            // 撈取會員資料 (如果已登入)
            Integer memId = authStrategyService.getCurrentUserId(request);
            if (memId != null) {
                Optional<SitterMemberVO> memberVO = sitterMemberRepository.findById(memId);
                if (memberVO.isPresent()) {
                    SitterMemberDTO fakeMember = SitterMemberDTO.fromEntity(memberVO.get());
                    model.addAttribute("currentMember", fakeMember);
                }
            }

            model.addAttribute("sitter", sitter);

            return "frontend/sitter/sitter-detail";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/public/sitter/search";
        }
    }
}
