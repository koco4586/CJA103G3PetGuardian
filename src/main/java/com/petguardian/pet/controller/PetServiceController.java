package com.petguardian.pet.controller;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.petguardian.pet.model.PetserItemrepository;
import com.petguardian.pet.service.PetserItemService;
import com.petguardian.pet.model.PetserItemVO;
import com.petguardian.pet.model.PetServiceItem;
import com.petguardian.sitter.model.SitterRepository;
import com.petguardian.sitter.model.SitterVO;
import com.petguardian.petsitter.model.PetSitterServiceRepository;
import com.petguardian.petsitter.model.PetSitterServiceVO;
import com.petguardian.booking.model.BookingDisplayDTO;
import com.petguardian.booking.model.BookingOrderVO;
import com.petguardian.common.service.AuthStrategyService;
import com.petguardian.booking.service.BookingService;
import com.petguardian.booking.model.BookingFavoriteVO;
import com.petguardian.pet.model.PetRepository;
import com.petguardian.pet.model.PetVO;

@Controller
@RequestMapping("/pet")
public class PetServiceController {

    @Autowired
    private PetserItemrepository petserItemrepository;

    @Autowired
    private PetserItemService petserItemService;

    @Autowired
    private SitterRepository sitterRepository;

    @Autowired
    private PetSitterServiceRepository petSitterServiceRepository;

    @Autowired
    private AuthStrategyService authStrategyService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private PetRepository petRepository;

    /**
     * 顯示寵物保姆服務介紹頁面
     * URL: GET /pet/services
     */
    @GetMapping("/services")
    public String showPetServices(Model model) {
        // 使用 Service 獲取經過處理的 VO 清單，確保顯示效果
        List<PetserItemVO> serviceList = petserItemService.getAllItemsForDisplay();

        // 如果沒有資料，嘗試直接從 Repo 拿取所有資料 (防止 status 為空時顯示不到)
        if (serviceList == null || serviceList.isEmpty()) {
            model.addAttribute("serviceList", petserItemrepository.findAll());
        } else {
            model.addAttribute("serviceList", serviceList);
        }

        return "frontend/pet/Petser_item";
    }

    /**
     * 顯示寵物服務細項 (相容舊路徑)
     * URL: GET /pet/Petser_item
     */
    @GetMapping("/Petser_item")
    public String showpetset(Model model) {
        // 同步跳轉邏輯，確保這個網址也能看到資料
        return showPetServices(model);
    }

    /**
     * 根據服務類型搜尋保姆 (全新實作)
     * URL: GET /pet/sitters/search
     * 邏輯：有符合服務的保母就顯示符合的，沒有就顯示全部
     */
    @GetMapping("/sitters/search")
    public String searchSittersByService(@RequestParam(required = false) String service, HttpServletRequest request,
            Model model) {

        List<SitterVO> filteredSitters = new ArrayList<>();

        if (service != null && !service.trim().isEmpty()) {
            // 1. 找服務項目
            List<PetServiceItem> items = petserItemrepository.findByServiceTypeContaining(service.trim());
            if (!items.isEmpty()) {
                Integer serviceId = items.get(0).getServiceItemId();
                // 2. 找提供該服務的保母
                List<PetSitterServiceVO> pssList = petSitterServiceRepository.findByServiceItemId(serviceId);
                filteredSitters = pssList.stream()
                        .map(PetSitterServiceVO::getSitter)
                        .filter(s -> s.getSitterStatus() == 0) // 僅限啟用
                        .distinct()
                        .collect(Collectors.toList());
            }
        }

        // 3. 如果沒找到符合的保母，就查全部啟用的保母
        if (filteredSitters.isEmpty()) {
            filteredSitters = sitterRepository.findBySitterStatus((byte) 0);
        }

        // 4. 包裝成前端頁面需要的 BookingDisplayDTO (比照 BookingViewController 邏輯)
        Integer memId = authStrategyService.getCurrentUserId(request);
        Set<Integer> favSitterIds = new HashSet<>();
        if (memId != null) {
            favSitterIds = bookingService.getSitterFavoritesByMember(memId)
                    .stream()
                    .map(BookingFavoriteVO::getSitterId)
                    .collect(Collectors.toSet());
        }

        // 載入所有服務對照 (用於產生 JSON)
        Map<Integer, List<PetSitterServiceVO>> allServicesBySitter = petSitterServiceRepository.findAll().stream()
                .collect(Collectors.groupingBy(svc -> svc.getSitter().getSitterId()));

        final Set<Integer> finalFavIds = favSitterIds;
        List<BookingDisplayDTO> displayList = filteredSitters.stream().map(s -> {
            BookingDisplayDTO dto = new BookingDisplayDTO(s, finalFavIds.contains(s.getSitterId()));
            List<PetSitterServiceVO> myServices = allServicesBySitter.getOrDefault(s.getSitterId(), new ArrayList<>());

            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < myServices.size(); i++) {
                PetSitterServiceVO svc = myServices.get(i);
                String svcName = (svc.getServiceItem() != null) ? svc.getServiceItem().getServiceType() : "未知服務";
                json.append(String.format("{\"id\":%d,\"name\":\"%s\"}", svc.getServiceItemId(), svcName));
                if (i < myServices.size() - 1)
                    json.append(",");
            }
            json.append("]");
            dto.setServicesJson(json.toString());
            return dto;
        }).collect(Collectors.toList());

        // 5. 傳遞必要的 Model 屬性供 frontend/services.html 使用
        model.addAttribute("sitters", displayList);
        model.addAttribute("order", new BookingOrderVO());

        // 寵物清單
        List<PetVO> myPets = (memId != null) ? petRepository.findByMemId(memId) : new ArrayList<>();
        model.addAttribute("myPets", myPets);

        return "frontend/services";
    }
}
