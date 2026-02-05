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
import com.petguardian.pet.model.PetRepository;
import com.petguardian.pet.model.PetVO;
import com.petguardian.pet.model.PetserItemrepository;
import com.petguardian.sitter.model.SitterRepository;
import com.petguardian.sitter.model.SitterVO;
import com.petguardian.member.repository.register.MemberRegisterRepository;
import com.petguardian.member.model.Member;
import com.petguardian.petsitter.model.PetSitterServiceRepository;

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
    private MemberRegisterRepository memberRepository;

    @Autowired
    private PetserItemrepository petServiceItemRepository;

    @Autowired
    private PetSitterServiceRepository petSitterServiceRepository;

    /**
     * 【顯示保姆服務列表頁面】
     * 1. 從資料庫撈取所有保姆資料
     * 2. 如果使用者已登入，查詢該使用者的收藏清單
     * 3. 將保姆資料與收藏狀態包裝成 BookingDisplayDTO
     * 4. 送到前端頁面顯示
     */
    @GetMapping("/services")
    public String listSitters(HttpServletRequest request, Model model) {
        List<Object[]> rawData = sitterRepository.findSitterBasicInfo();

        List<SitterVO> rawSitters = rawData.stream().map(row -> {
            SitterVO s = new SitterVO();
            s.setSitterId((Integer) row[0]);
            s.setSitterName((String) row[1]);
            s.setSitterAdd((String) row[2]);
            s.setSitterStarCount((Integer) row[3]);
            s.setSitterRatingCount((Integer) row[4]);
            s.setMemId((Integer) row[5]);
            return s;
        }).collect(Collectors.toList());

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

        // java.util.Map<Integer, java.util.List<PetSitterServiceVO>>
        // allServicesBySitter = petSitterServiceRepository
        // .findAll().stream()
        // .collect(Collectors.groupingBy(svc -> svc.getSitter().getSitterId()));
        final Set<Integer> finalFavIds = favSitterIds;
        List<BookingDisplayDTO> displayList = rawSitters.stream().map(s -> {
            BookingDisplayDTO dto = new BookingDisplayDTO(s, finalFavIds.contains(s.getSitterId()));
            dto.setServicesJson("[]");

            // 從 Map 取得該保母的服務
            // List<PetSitterServiceVO> myServices =
            // allServicesBySitter.getOrDefault(s.getSitterId(), new
            // java.util.ArrayList<>());
            //
            // StringBuilder json = new StringBuilder("[");
            // for (int i = 0; i < myServices.size(); i++) {
            // PetSitterServiceVO svc = myServices.get(i);
            // Integer svcId = svc.getServiceItemId();
            //
            // // 直接透過關聯取得服務名稱！不用再查表了
            // String svcName = "未知服務";
            // if (svc.getServiceItem() != null) {
            // svcName = svc.getServiceItem().getServiceType();
            // }
            //
            // json.append(String.format("{\"id\":%d,\"name\":\"%s\"}", svcId, svcName));
            // if (i < myServices.size() - 1) json.append(",");
            // }
            // json.append("]");
            //
            // dto.setServicesJson(json.toString());
            return dto;
        }).collect(Collectors.toList());

        // 6. 將資料傳給前端頁面
        model.addAttribute("currentMemId", memId);
        model.addAttribute("sitters", displayList);
        addCommonAttributes(request, model);
        return "frontend/services";
    }

    @GetMapping("/member/favorites")
    public String listMyFavorites(HttpServletRequest request, Model model) {
        Integer memId = authStrategyService.getCurrentUserId(request);

        // 這裡才呼叫詳細版，因為這頁就是要看保母名字
        List<BookingFavoriteVO> detailFavs = bookingService.getSitterFavoritesWithDetail(memId);

        model.addAttribute("sitterFavorites", detailFavs);
        return "frontend/member-favorites";
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

        // 3.傳遞資料給前端
        model.addAttribute("bookingList", bookingList);
        model.addAttribute("currentStatus", status);
        model.addAttribute("memId", memId);
        model.addAttribute("memName", authStrategyService.getCurrentUserName(request));

        // [NEW] 查詢會員資料供側邊欄顯示頭像
        Member currentMember = memberRepository.findById(memId).orElse(null);
        if (currentMember != null) {
            model.addAttribute("currentMember", currentMember);
        }

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

    /**
     * [新增] API: 動態查詢保母的服務項目
     * 用途：當使用者點擊「立即預約」時才呼叫此 API，避免一開始載入過多資料
     */
    @GetMapping("/api/sitter/{sitterId}/services")
    @org.springframework.web.bind.annotation.ResponseBody // 回傳 JSON 資料
    public List<java.util.Map<String, Object>> getSitterServices(@PathVariable Integer sitterId) {
        // 使用既有的 Repository 查詢該保母的服務
        return petSitterServiceRepository.findBySitter_SitterId(sitterId).stream()
                .map(svc -> {
                    java.util.Map<String, Object> map = new java.util.HashMap<>();
                    map.put("id", svc.getServiceItemId());
                    // 透過關聯取得服務名稱 (若無關聯則顯示預設文字)
                    String name = (svc.getServiceItem() != null) ? svc.getServiceItem().getServiceType() : "一般服務";
                    map.put("name", name);
                    return map;
                })
                .collect(Collectors.toList());
    }
}