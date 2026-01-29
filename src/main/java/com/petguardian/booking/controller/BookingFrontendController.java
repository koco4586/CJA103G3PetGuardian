package com.petguardian.booking.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.petguardian.booking.model.BookingOrderVO;
import com.petguardian.common.service.AuthStrategyService;
import com.petguardian.pet.model.PetRepository;
import com.petguardian.pet.model.PetVO;
import com.petguardian.sitter.model.SitterRepository;
import com.petguardian.sitter.model.SitterVO;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/booking")
public class BookingFrontendController {

    @Autowired
    private SitterRepository sitterRepository;
    
    @Autowired
    private PetRepository petRepository;

    @Autowired
    private AuthStrategyService authStrategyService;
    
    private void addCommonAttributes(HttpServletRequest request, Model model) {
        Integer memId = authStrategyService.getCurrentUserId(request);
//        if (memId != null) {
//            List<PetVO> myPets = petRepository.findByMemId(memId);
//            model.addAttribute("myPets", myPets);
//        }
     // --- 測試用代碼：手動加入一隻假寵物 ---
        PetVO dummyPet = new PetVO();
        dummyPet.setPetId(999); // 假 ID
        dummyPet.setPetName("測試小黑");
        
        // 獲取原本的寵物清單
        List<PetVO> myPets = (memId != null) ? petRepository.findByMemId(memId) : new java.util.ArrayList<>();
        
        // 把假寵物塞進去
        myPets.add(dummyPet);
        
        model.addAttribute("myPets", myPets);
    }

    @GetMapping("/services")
    public String showServicesPage(HttpServletRequest request, Model model) {
        // 先撈出所有啟用中的保母，讓頁面有資料顯示
        List<SitterVO> allSitters = sitterRepository.findBySitterStatus((byte) 0);
        
        // [New] 排除自己 (若登入者同時也是保姆，不該在列表看到自己)
        Integer currentMemId = authStrategyService.getCurrentUserId(request);
        if (currentMemId != null) {
            allSitters = allSitters.stream()
                    .filter(s -> !s.getMemId().equals(currentMemId))
                    .toList();
        }
        
        model.addAttribute("sitters", allSitters);
        addCommonAttributes(request, model);
        return "frontend/services";
    }

    @GetMapping("/search")
    public String searchSitters(@RequestParam(required = false) String area, HttpServletRequest request, Model model) {
        // 從資料庫撈出所有保姆 (排除手動擴充的 Repository 方法，直接使用 findAll)
        List<SitterVO> allSitters = sitterRepository.findAll();

        Integer currentMemId = authStrategyService.getCurrentUserId(request);//// 取得目前登入者 ID
        // 執行複核過濾：
        // 1. 必須是啟用中 (sitterStatus == 0)
        // 2. 如果有傳入地區關鍵字，則地址必須包含該關鍵字
        List<SitterVO> filteredSitters = allSitters.stream()
                .filter(s -> s.getSitterStatus() == 0)
                .filter(s -> currentMemId == null || !s.getMemId().equals(currentMemId))
                .filter(s -> {
                    if (area == null || area.trim().isEmpty()) {
                        return true;
                    }
                    return s.getSitterAdd() != null && s.getSitterAdd().contains(area);
                })
                .toList();

        model.addAttribute("sitters", filteredSitters);
        model.addAttribute("order", new BookingOrderVO());
        addCommonAttributes(request, model);
        return "frontend/services";
    }
}