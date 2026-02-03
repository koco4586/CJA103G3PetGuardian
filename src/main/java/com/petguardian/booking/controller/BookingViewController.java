package com.petguardian.booking.controller;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.petguardian.booking.model.BookingDisplayDTO;
import com.petguardian.booking.model.BookingFavoriteVO;
import com.petguardian.booking.model.BookingOrderVO;
import com.petguardian.booking.service.BookingDataIntegrationService;
import com.petguardian.booking.service.BookingService;
import com.petguardian.common.service.AuthStrategyService;
import com.petguardian.member.model.Member;
import com.petguardian.pet.model.PetRepository;
import com.petguardian.pet.model.PetVO;
import com.petguardian.pet.model.PetserItemrepository;
import com.petguardian.petsitter.model.PetSitterServiceVO;
import com.petguardian.sitter.model.SitterRepository;
import com.petguardian.sitter.model.SitterVO;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 前台預約頁面顯示控制器
 * 負責：所有 GET 請求，回傳 HTML 頁面
 */
@Controller
@RequestMapping("/booking")
public class BookingViewController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private BookingDataIntegrationService dataService;

    @Autowired
    private AuthStrategyService authStrategyService;

    @Autowired
    private SitterRepository sitterRepository;

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private PetserItemrepository petServiceItemRepository;

    @Autowired
    private com.petguardian.petsitter.model.PetSitterServiceRepository petSitterServiceRepository;

    /**
     * 【顯示保姆服務列表頁面】
     * 1. 從資料庫撈取所有保姆資料
     * 2. 如果使用者已登入，查詢該使用者的收藏清單
     * 3. 將保姆資料與收藏狀態包裝成 BookingDisplayDTO
     * 4. 送到前端頁面顯示
     */
    @GetMapping("/services")
    public String listSitters(HttpServletRequest request, Model model) {
        // 1. 取得所有保母資料
        List<SitterVO> rawSitters = sitterRepository.findAll();

        // 2. 取得當前登入使用者 ID
        Integer memId = authStrategyService.getCurrentUserId(request);

        // 3. 建立收藏保姆 ID 的集合
        Set<Integer> favSitterIds = new HashSet<>();

        // 4. 如果使用者已登入，查詢他收藏了哪些保姆
        if (memId != null) {
            favSitterIds = bookingService.getSitterFavoritesByMember(memId)
                    .stream()
                    .map(BookingFavoriteVO::getSitterId)
                    .collect(Collectors.toSet());
        }

        java.util.Map<Integer, java.util.List<PetSitterServiceVO>> allServicesBySitter = petSitterServiceRepository
                .findAll().stream()
                .collect(Collectors.groupingBy(svc -> svc.getSitter().getSitterId()));
        final Set<Integer> finalFavIds = favSitterIds;
        List<BookingDisplayDTO> displayList = rawSitters.stream().map(s -> {
            BookingDisplayDTO dto = new BookingDisplayDTO(s, finalFavIds.contains(s.getSitterId()));

            // 從 Map 取得該保母的服務
            List<PetSitterServiceVO> myServices = allServicesBySitter.getOrDefault(s.getSitterId(),
                    new java.util.ArrayList<>());

            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < myServices.size(); i++) {
                PetSitterServiceVO svc = myServices.get(i);
                Integer svcId = svc.getServiceItemId();

                // [關鍵修改] 直接透過關聯取得服務名稱！不用再查表了
                String svcName = "未知服務";
                if (svc.getServiceItem() != null) {
                    svcName = svc.getServiceItem().getServiceType();
                }

                json.append(String.format("{\"id\":%d,\"name\":\"%s\"}", svcId, svcName));
                if (i < myServices.size() - 1)
                    json.append(",");
            }
            json.append("]");

            dto.setServicesJson(json.toString());
            return dto;
        }).collect(Collectors.toList());

        // 6. 將資料傳給前端頁面
        model.addAttribute("sitters", displayList);
        addCommonAttributes(request, model);
        return "frontend/services";
    }

    /**
     * 【顯示保姆搜尋頁面】
     * 1. 根據地區關鍵字搜尋保姆
     * 2. 過濾啟用中的保姆
     * 3. 排除登入者本人
     */
    @GetMapping("/search")
    public String searchSitters(
            @RequestParam(required = false) String area,
            HttpServletRequest request,
            Model model) {

        // 1. 從資料庫撈出所有保姆
        List<SitterVO> allSitters = sitterRepository.findAll();

        // 2. 取得目前登入者 ID
        Integer currentMemId = authStrategyService.getCurrentUserId(request);

        // 3. 執行過濾條件：
        // - 必須是啟用中 (sitterStatus == 0)
        // - 排除登入者本人
        // - 如果有地區參數，地址必須包含該關鍵字
        List<SitterVO> filteredSitters = allSitters.stream()
                .filter(s -> s.getSitterStatus() == 0) // 只顯示啟用中的保姆
                .filter(s -> currentMemId == null || !s.getMemId().equals(currentMemId)) // 不顯示自己
                .filter(s -> {
                    // 如果沒有輸入地區，顯示全部
                    if (area == null || area.trim().isEmpty()) {
                        return true;
                    }
                    // 有輸入地區，則地址必須包含該關鍵字
                    return s.getSitterAdd() != null && s.getSitterAdd().contains(area);
                })
                .toList();

        // 4. 傳遞資料給前端
        model.addAttribute("sitters", filteredSitters);
        model.addAttribute("order", new BookingOrderVO());
        addCommonAttributes(request, model); // 加入共用資料（如寵物清單）

        return "frontend/services";
    }

    /**
     * 【顯示預約表單頁面】
     * 1. 檢查使用者是否登入
     * 2. 根據參數載入會員、寵物資料
     * 3. 建立空白的預約單物件
     * 4. 顯示預約表單頁面
     */
    @GetMapping("/add")
    public String showAddForm(
            @RequestParam Integer sitterId,
            @RequestParam Integer petId,
            @RequestParam Integer serviceItemId,
            HttpServletRequest request,
            Model model) {

        try {
            // 1. 檢查是否登入
            Integer memId = authStrategyService.getCurrentUserId(request);
            if (memId == null) {
                return "redirect:/front/loginpage"; // 未登入，導向登入頁
            }

            // 2. 載入會員和寵物資料
            Member member = dataService.getMemberInfo(memId);
            PetVO pet = dataService.getPetInfo(petId);

            // 3. 建立預約單物件並填入參數
            BookingOrderVO order = new BookingOrderVO();
            order.setSitterId(sitterId);
            order.setMemId(memId);
            order.setPetId(petId);
            order.setServiceItemId(serviceItemId);

            // 4. 將資料傳給前端表單
            model.addAttribute("order", order);
            model.addAttribute("memberName", member.getMemName());
            model.addAttribute("petName", pet.getPetName());
            model.addAttribute("errorMessage", null);

        } catch (Exception e) {
            // 5. 如果資料載入失敗，顯示錯誤訊息
            model.addAttribute("errorMessage", "資料讀取失敗。");
            model.addAttribute("order", new BookingOrderVO());
        }

        return "frontend/booking/add-booking";
    }

    /**
     * 【顯示會員的預約列表】
     * 路徑：GET /booking/list/member/{memId}
     * 功能：
     * 1. 查詢該會員所有進行中的預約
     * 2. 為每筆預約載入保姆姓名
     * 3. 顯示預約列表頁面
     */
    @GetMapping("/list/member/{memId}")
    public String memberBookingList(@PathVariable Integer memId, Model model) {

        // 1. 查詢該會員所有進行中的預約
        List<BookingOrderVO> list = bookingService.getActiveOrdersByMemberId(memId);

        // 2. 為每筆預約補上保姆姓名
        for (BookingOrderVO order : list) {
            try {
                PetSitterServiceVO service = dataService.getSitterServiceInfo(
                        order.getSitterId(),
                        order.getServiceItemId());
                order.setSitterName(service.getSitter().getSitterName());
                order.setSitterMemId(service.getSitter().getMemId());
            } catch (Exception e) {
                order.setSitterName("未知保母");
            }
        }

        // 3. 傳遞資料給前端
        model.addAttribute("bookingList", list);
        model.addAttribute("memId", memId);

        return "frontend/booking/list-booking";
    }

    /**
     * 【顯示會員中心的預約管理頁面】
     * 1. 檢查使用者是否登入
     * 2. 根據狀態參數過濾預約（可選）
     * 3. 為每筆預約載入保姆姓名
     * 4. 顯示會員的預約管理頁面
     */
    @GetMapping("/memberOrders")
    public String listMemberOrders(
            @RequestParam(required = false) Integer status,
            HttpServletRequest request,
            Model model) {

        // 1. 取得當前登入使用者 ID
        Integer memId = authStrategyService.getCurrentUserId(request);
        if (memId == null) {
            return "redirect:/front/loginpage"; // 未登入，導向登入頁
        }

        // 2. 根據狀態查詢預約（有 status 參數則過濾，沒有則查全部）
        List<BookingOrderVO> bookingList = (status != null)
                ? bookingService.findByMemberAndStatus(memId, status)
                : bookingService.getOrdersByMemberId(memId);

        // 3. 為每筆預約補上保姆姓名
        for (BookingOrderVO order : bookingList) {
            try {
                PetSitterServiceVO service = dataService.getSitterServiceInfo(
                        order.getSitterId(),
                        order.getServiceItemId());
                order.setSitterName(service.getSitter().getSitterName());
                order.setSitterMemId(service.getSitter().getMemId());
            } catch (Exception e) {
                order.setSitterName("未知保母");
            }
        }

        // 4. 傳遞資料給前端
        model.addAttribute("bookingList", bookingList);
        model.addAttribute("currentStatus", status);
        model.addAttribute("memId", memId);
        model.addAttribute("memName", authStrategyService.getCurrentUserName(request));

        return "frontend/dashboard-bookings";
    }

    /**
     * 【共用方法：加入頁面常用資料】
     * 1. 如果使用者已登入，載入他的寵物清單
     * 2. 加入測試用的假寵物
     */
    private void addCommonAttributes(HttpServletRequest request, Model model) {
        Integer memId = authStrategyService.getCurrentUserId(request);

        // 獲取原本的寵物清單
        List<PetVO> myPets = (memId != null)
                ? petRepository.findByMemId(memId)
                : new java.util.ArrayList<>();

        model.addAttribute("myPets", myPets);

        // List<PetServiceItem> serviceItems =
        // petServiceItemRepository.findByServiceStatus(1);
        // model.addAttribute("serviceItems", serviceItems);
    }
}