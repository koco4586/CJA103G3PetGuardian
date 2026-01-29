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
import com.petguardian.booking.service.BookingDataIntegrationService;
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
    private BookingDataIntegrationService dataService;

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

            Member member = dataService.getMemberInfo(memId);
            PetVO pet = dataService.getPetInfo(petId);

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
                PetSitterServiceVO service = dataService.getSitterServiceInfo(order.getSitterId(),
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
    public String listMemberOrders(@RequestParam(required = false) Integer status, HttpServletRequest request, Model model) {
        Integer memId = authStrategyService.getCurrentUserId(request);
        if (memId == null) {
            return "redirect:/loginpage";
        }
        
        List<BookingOrderVO> bookingList = (status != null) 
            ? bookingService.findByMemberAndStatus(memId, status)
            : bookingService.getOrdersByMemberId(memId);
        
     // 2. 統一為這些訂單填入保母姓名 (這段邏輯移到這，確保過濾後的訂單都有姓名)
        for (BookingOrderVO order : bookingList) {
            try {
                PetSitterServiceVO service = dataService.getSitterServiceInfo(order.getSitterId(),
                        order.getServiceItemId());
                order.setSitterName(service.getSitter().getSitterName());
            } catch (Exception e) {
                order.setSitterName("未知保母");
            }
        }

        if (status != null) {
            bookingList = bookingService.findByMemberAndStatus(memId, status);
        } else {
            bookingList = bookingService.getActiveOrdersByMemberId(memId);
        }
        
        model.addAttribute("bookingList", bookingList);
        model.addAttribute("currentStatus", status);
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
     * 【6. AJAX 取消預約並處理退款】
     * 對應前端 fetch("/booking/refund/{orderId}")
     */
    @PostMapping("/refund/{orderId}")
    @ResponseBody
    public ResponseEntity<String> handleRefund(@PathVariable Integer orderId) {
        try {
            // 呼叫 Service 執行取消邏輯（包含退款審核、更改訂單狀態、釋出保母排程）
            // 建議在 Service 裡根據取消時間判斷退款比例
            bookingService.cancelBooking(orderId, "會員自行取消");
            
            return ResponseEntity.ok("取消成功");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body("取消失敗：" + e.getMessage());
        }
    }
    
    /**
     * 當表單提交失敗回原頁面時，重新加載顯示用的名稱資料。
     */
    private void reloadMemberPetData(BookingOrderVO order, Model model) {
        try {
            Member member = dataService.getMemberInfo(order.getMemId());
            PetVO pet = dataService.getPetInfo(order.getPetId());
            model.addAttribute("memberName", member != null ? member.getMemName() : "未知使用者");
            model.addAttribute("petName", pet != null ? pet.getPetName() : "未知寵物");
        } catch (Exception ex) {
            model.addAttribute("memberName", "未知使用者");
            model.addAttribute("petName", "未知寵物");
        }
    }
}
