package com.petguardian.booking.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.petguardian.booking.model.BookingDisplayDTO;
import com.petguardian.booking.model.BookingFavoriteVO;
import com.petguardian.booking.model.BookingOrderVO;
import com.petguardian.booking.service.BookingService;
import com.petguardian.common.service.AuthStrategyService;
import com.petguardian.member.model.Member;
import com.petguardian.member.repository.register.MemberRegisterRepository;
import com.petguardian.pet.model.PetRepository;
import com.petguardian.pet.model.PetVO;
import com.petguardian.petsitter.model.PetSitterServiceRepository;
import com.petguardian.sitter.model.SitterMemberRepository;
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
    private AuthStrategyService authStrategyService;

    @Autowired
    private SitterRepository sitterRepository;

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private PetSitterServiceRepository petSitterServiceRepository;

    @Autowired
    private SitterMemberRepository sitterMemberRepository;

    @Autowired
    private MemberRegisterRepository memberRepository;

    @Autowired
    private com.petguardian.evaluate.service.EvaluateService evaluateService;

    /**
     * 【顯示保姆服務列表頁面】
     * 1. 從資料庫撈取所有保姆資料
     * 2. 如果使用者已登入，查詢該使用者的收藏清單
     * 3. 將保姆資料與收藏狀態包裝成 BookingDisplayDTO
     * 4. 送到前端頁面顯示
     */
    @GetMapping("/services")
    public String listSitters(
            @RequestParam(defaultValue = "0") int page, // 正確放置 RequestParam
            HttpServletRequest request,
            Model model) { // 這裡只需一組括號

        // 1. 先查出該頁面的 6 位保母 (分頁關鍵)
        Pageable pageable = PageRequest.of(page, 6);
        Page<SitterVO> sitterPage = sitterRepository.findAllActive(pageable);

        // 2. 蒐集這 6 位保母的 ID 並補齊「服務地區」
        List<Integer> sitterIds = sitterPage.getContent().stream()
                .map(SitterVO::getSitterId)
                .collect(Collectors.toList());
        // 3. 蒐集這 6 位保母的會員 ID 並補齊「頭像」
        List<SitterVO> fullSitters = sitterRepository.findAllWithAreasByIds(sitterIds);

        List<Integer> memIds = fullSitters.stream()
                .map(SitterVO::getMemId)
                .collect(Collectors.toList());

        Map<Integer, String> memberImageMap = new HashMap<>();
        sitterMemberRepository.findAllById(memIds).forEach(m -> {
            String img = (m.getMemImage() != null) ? m.getMemImage() : "/images/default-avatar.png";
            memberImageMap.put(m.getMemId(), img);
        });

        Integer currentMemId = authStrategyService.getCurrentUserId(request);

        // 4. 建立收藏保姆 ID 的集合 (修正變數名稱與類型)
        java.util.Set<Integer> favSitterIds = new java.util.HashSet<>();

        // 5. 如果使用者已登入，查詢他收藏了哪些保姆
        if (currentMemId != null) {
            favSitterIds = bookingService.getSitterFavoritesByMember(currentMemId)
                    .stream()
                    .map(BookingFavoriteVO::getSitterId)
                    .collect(Collectors.toSet());
        }

        final java.util.Set<Integer> finalFavIds = favSitterIds;
        List<BookingDisplayDTO> displayList = fullSitters.stream()
                .filter(s -> currentMemId == null || !s.getMemId().equals(currentMemId))
                .map(s -> {
                    BookingDisplayDTO dto = new BookingDisplayDTO(s, finalFavIds.contains(s.getSitterId()));
                    dto.setMemImage(memberImageMap.getOrDefault(s.getMemId(), "/images/default-avatar.png"));

                    dto.setMemImage(memberImageMap.getOrDefault(
                            s.getMemId(),
                            "/images/default-avatar.png"));

                    String city = "沒有設定服務";
                    if (s.getServiceAreas() != null && !s.getServiceAreas().isEmpty()) {
                        // 取得第一個服務地區的城市名稱 (例如：台北市)
                        city = s.getServiceAreas().get(0).getArea().getCityName();
                    }
                    dto.setServicesJson(city);

                    // 🔥 注入平均星數
                    Double avgRating = evaluateService.getAverageRatingBySitterId(s.getSitterId());
                    dto.setAvgRating(avgRating);

                    return dto;
                }).collect(Collectors.toList());

        // 6. 將資料傳給前端頁面
        model.addAttribute("sitters", displayList);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", sitterPage.getTotalPages()); // 修正 odel -> model
        model.addAttribute("currentMemId", currentMemId);
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

        // 查詢會員資料供側邊欄顯示頭像
        Member currentMember = memberRepository.findById(memId).orElse(null);
        if (currentMember != null) {
            model.addAttribute("currentMember", currentMember);
        }

        return "frontend/dashboard-bookings";
    }

    /**
     * 【加入頁面常用資料】
     * 如果使用者已登入，載入他的寵物清單
     */
    private void addCommonAttributes(HttpServletRequest request, Model model) {
        Integer memId = authStrategyService.getCurrentUserId(request);

        // 獲取原本的寵物清單
        List<PetVO> myPets = (memId != null)
                ? petRepository.findByMemId(memId)
                : new java.util.ArrayList<>();

        model.addAttribute("myPets", myPets);

    }

    /**
     * API: 動態查詢保母的服務項目
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