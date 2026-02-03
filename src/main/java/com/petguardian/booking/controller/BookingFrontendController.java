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
import com.petguardian.petsitter.model.PetSitterServiceRepository;
import com.petguardian.pet.model.PetServiceItem;
import com.petguardian.pet.model.PetserItemrepository;

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

    @Autowired
    private PetserItemrepository petserItemrepository; // 注入搜尋服務項目的 Repo

    @Autowired
    private PetSitterServiceRepository petSitterServiceRepo; // 注入保姆服務關聯的 Repo

    private void addCommonAttributes(HttpServletRequest request, Model model) {
        Integer memId = authStrategyService.getCurrentUserId(request);
        if (memId != null) {
            List<PetVO> myPets = petRepository.findByMemId(memId);
            model.addAttribute("myPets", myPets);
        }
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
    public String searchSitters(
            @RequestParam(required = false) String area,
            @RequestParam(required = false) String service, // 新增服務參數
            HttpServletRequest request, Model model) {

        // 1. 抓取所有保姆
        List<SitterVO> allSitters = sitterRepository.findAll();
        Integer currentMemId = authStrategyService.getCurrentUserId(request);

        // 2. 處理服務過濾：如果傳入服務名稱，先找出對應的 ID
        List<Integer> sitterIdsProvidingService = null;
        if (service != null && !service.trim().isEmpty()) {
            List<PetServiceItem> items = petserItemrepository.findByServiceTypeContaining(service);
            if (!items.isEmpty()) {
                Integer serviceId = items.get(0).getServiceItemId();
                // 找出有提供這項服務的保姆 ID 列表
                sitterIdsProvidingService = petSitterServiceRepo.findByServiceItemId(serviceId)
                        .stream()
                        .map(ps -> ps.getSitter().getSitterId())
                        .toList();
            } else {
                // 如果查不到該服務名稱，預設給一個空列表
                sitterIdsProvidingService = List.of();
            }
        }

        // 3. 執行複合過濾
        final List<Integer> finalSitterIds = sitterIdsProvidingService;
        List<SitterVO> filteredSitters = allSitters.stream()
                .filter(s -> s.getSitterStatus() == 0) // 必須啟用
                .filter(s -> currentMemId == null || !s.getMemId().equals(currentMemId)) // 排除自己
                .filter(s -> {
                    // 地區過濾
                    if (area == null || area.trim().isEmpty())
                        return true;
                    return s.getSitterAdd() != null && s.getSitterAdd().contains(area);
                })
                .filter(s -> {
                    // 服務過濾
                    if (finalSitterIds == null)
                        return true; // 沒傳服務參數就不過濾
                    return finalSitterIds.contains(s.getSitterId());
                })
                .toList();

        model.addAttribute("sitters", filteredSitters);
        model.addAttribute("order", new BookingOrderVO());
        addCommonAttributes(request, model);
        return "frontend/services";
    }
}