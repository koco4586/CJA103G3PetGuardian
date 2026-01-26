package com.petguardian.booking.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.petguardian.booking.model.BookingOrderVO;
import com.petguardian.booking.service.BookingExternalDataService;
import com.petguardian.booking.service.BookingService;
import com.petguardian.common.service.AuthStrategyService;
import com.petguardian.member.model.Member;
import com.petguardian.pet.model.PetVO;
import com.petguardian.petsitter.model.PetSitterServiceVO;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 前台預約流程控制器：處理預約建立、列表查詢與取消訂單等功能
 */
@Controller
@RequestMapping("/booking")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private BookingExternalDataService externalDataService;

    @Autowired
    private AuthStrategyService authStrategyService;

    /**
     * 【1. 顯示預約表單頁面】
     */
    @GetMapping("/add")
    public String showAddForm(
            @RequestParam Integer sitterId,
            @RequestParam Integer petId,
            @RequestParam Integer serviceItemId,
            HttpServletRequest request,
            Model model) {
        try {
            Integer memId = authStrategyService.getCurrentUserId(request);
            if (memId == null) {
                return "redirect:/member/login";
            }

            Member member = externalDataService.getMemberInfo(memId);
            PetVO pet = externalDataService.getPetInfo(petId);

            BookingOrderVO order = new BookingOrderVO();
            order.setSitterId(sitterId);
            order.setMemId(memId);
            order.setPetId(petId);
            order.setServiceItemId(serviceItemId);

            model.addAttribute("order", order);
            model.addAttribute("memberName", member.getMemName());
            model.addAttribute("petName", pet.getPetName());
            model.addAttribute("errorMessage", null);

        } catch (Exception e) {
            model.addAttribute("errorMessage", "資料讀取失敗。");
            model.addAttribute("order", new BookingOrderVO());
            return "frontend/booking/add-booking";
        }
        return "frontend/booking/add-booking";
    }

    /**
     * 【2. 接收預約表單提交】
     */
    @PostMapping("/submit")
    @ResponseBody // 確保回傳 JSON 而非頁面路徑
    public ResponseEntity<?> submitBooking(@ModelAttribute("order") BookingOrderVO order, HttpServletRequest request) {
        try {
            Integer currentUserId = authStrategyService.getCurrentUserId(request);
            if (currentUserId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("請先登入");
            }

            order.setMemId(currentUserId);
            BookingOrderVO savedOrder = bookingService.createBooking(order);

            // 回傳成功訊息與訂單 ID，讓前端決定去哪裡
            return ResponseEntity.ok(savedOrder.getBookingOrderId());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 【3. 查詢會員預約列表】
     */
    @GetMapping("/list/member/{memId}")
    public String memberBookingList(@PathVariable Integer memId, Model model) {
        List<BookingOrderVO> list = bookingService.getActiveOrdersByMemberId(memId);
        for (BookingOrderVO order : list) {
            try {
                PetSitterServiceVO service = externalDataService.getSitterInfo(order.getSitterId(),
                        order.getServiceItemId());
                order.setSitterName(service.getSitter().getSitterName());
            } catch (Exception e) {
                order.setSitterName("未知保母");
            }
        }
        model.addAttribute("bookingList", list);
        model.addAttribute("memId", memId);
        return "frontend/booking/booking-member-list";
    }

    /**
     * 【4. 會員中心預約管理】
     */
    @GetMapping("/memberOrders")
    public String listMemberOrders(HttpServletRequest request, Model model) {
        Integer memId = authStrategyService.getCurrentUserId(request);
        if (memId == null) {
            return "redirect:/login";
        }

        List<BookingOrderVO> allOrders = bookingService.getActiveOrdersByMemberId(memId);
        List<BookingOrderVO> activeOrders = allOrders.stream()
                .filter(order -> order.getOrderStatus() != 2 &&
                        order.getOrderStatus() != 3 &&
                        order.getOrderStatus() != 5)
                .toList();

        for (BookingOrderVO order : activeOrders) {
            try {
                PetSitterServiceVO service = externalDataService.getSitterInfo(order.getSitterId(),
                        order.getServiceItemId());
                order.setSitterName(service.getSitter().getSitterName());
            } catch (Exception e) {
                order.setSitterName("未知保母");
            }
        }

        model.addAttribute("bookingList", activeOrders);
        model.addAttribute("memId", memId);
        model.addAttribute("memName", authStrategyService.getCurrentUserName(request));

        return "frontend/dashboard-bookings";
    }

    /**
     * 【5. 取消預約】
     */
    @PostMapping("/cancel")
    public String cancelBooking(@RequestParam Integer orderId, @RequestParam String reason, RedirectAttributes ra) {
        try {
            bookingService.cancelBooking(orderId, reason);
            ra.addFlashAttribute("successMessage", "訂單已成功取消");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "取消失敗：" + e.getMessage());
        }
        return "redirect:/booking/memberOrders";
    }

    /**
     * 當表單提交失敗回原頁面時，重新加載顯示用的名稱資料。
     */
    private void reloadMemberPetData(BookingOrderVO order, Model model) {
        try {
            Member member = externalDataService.getMemberInfo(order.getMemId());
            PetVO pet = externalDataService.getPetInfo(order.getPetId());
            model.addAttribute("memberName", member != null ? member.getMemName() : "未知使用者");
            model.addAttribute("petName", pet != null ? pet.getPetName() : "未知寵物");
        } catch (Exception ex) {
            model.addAttribute("memberName", "未知使用者");
            model.addAttribute("petName", "未知寵物");
        }
    }
}
