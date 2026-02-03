package com.petguardian.pet.controller;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.petguardian.booking.model.BookingOrderVO;
import com.petguardian.booking.service.BookingService;
import com.petguardian.evaluate.model.EvaluateDTO;
import com.petguardian.evaluate.model.EvaluateVO;
import com.petguardian.evaluate.service.EvaluateService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/pet")
public class PetEvaluateController {

    @Autowired
    private EvaluateService evaluateService;

    @Autowired
    private BookingService bookingOrderSvc;

    /**
     * API 端點：根據保姆 ID 撈取所有評價資料
     * URL: /pet/evaluate/list/{sitterId}
     */
    @GetMapping("/evaluate/list/{sitterId}")
    @ResponseBody
    public ResponseEntity<List<EvaluateVO>> getReviewsBySitterId(@PathVariable Integer sitterId) {
        try {
            List<EvaluateVO> reviews = evaluateService.getReviewsBySitterId(sitterId);
            return ResponseEntity.ok(reviews);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/evaluate")
    public String showEvaluatePage(HttpSession session, Model model, @RequestParam Integer orderId) {
        Integer memId = (Integer) session.getAttribute("memId");
        Integer roleId = (Integer) session.getAttribute("roleId");

        BookingOrderVO order = bookingOrderSvc.getOrderById(orderId);

        if (order != null) {
            boolean isSitterOfOrder = memId.equals(order.getSitterId());
            model.addAttribute("isSitter", isSitterOfOrder);
            model.addAttribute("currentOrderId", order.getBookingOrderId());
            model.addAttribute("sitterId", order.getSitterId());
            model.addAttribute("orderInfo", order);

            List<EvaluateDTO> reviewGroups = evaluateService.getByBookingOrderId(orderId);
            model.addAttribute("reviewGroups", reviewGroups);
        }

        if (memId == null) {
            System.out.println("評價頁面攔截：未登入會員");
            return "redirect:/front/loginpage";
        }

        model.addAttribute("memId", memId);
        model.addAttribute("currentRole", roleId);
        model.addAttribute("memName", session.getAttribute("memName"));

        return "frontend/evaluate";
    }

    @PostMapping("/evaluate/save")
    @ResponseBody
    public String saveEvaluate(@RequestBody Map<String, Object> payload, HttpSession session) {
        System.out.println("當前 session 中的 roleId 是: " + session.getAttribute("roleId"));
        try {
            EvaluateVO vo = new EvaluateVO();

            Object orderObj = payload.get("bookingOrderId");
            String orderStr = (orderObj == null) ? "" : orderObj.toString().trim();

            if (orderStr.isEmpty() || "null".equals(orderStr) || "undefined".equals(orderStr)) {
                return "error: 遺失訂單編號 (bookingOrderId)";
            }

            Integer orderId = Double.valueOf(orderStr).intValue();
            vo.setBookingOrderId(orderId);

            BookingOrderVO order = bookingOrderSvc.getOrderById(orderId);
            if (order == null) {
                return "error: 找不到訂單資料";
            }

            Integer memId = (Integer) session.getAttribute("memId");
            if (memId == null) {
                return "error: 請先登入";
            }

            Object roleTypeObj = payload.get("roleType");
            int roleType = (roleTypeObj != null) ? Integer.parseInt(roleTypeObj.toString()) : 1;
            String currentRole;

            if (roleType == 0) {
                currentRole = "SITTER";
                vo.setSenderId(order.getSitterId());
                vo.setReceiverId(order.getMemId());
                vo.setRoleType(0);
            } else {
                currentRole = "MEMBER";
                vo.setSenderId(memId);
                vo.setReceiverId(order.getSitterId());
                vo.setRoleType(1);
            }

            vo.setContent(String.valueOf(payload.getOrDefault("content", "")));
            String starRating = String.valueOf(payload.getOrDefault("starRating", "5"));
            vo.setStarRating(Double.valueOf(starRating).intValue());

            evaluateService.handleSubmission(vo, currentRole);

            return "success";
        } catch (Exception e) {
            e.printStackTrace();
            return "error: " + e.getMessage();
        }
    }

    /**
     * API 端點：根據會員 ID 撈取所有保姆對該會員的評價
     */
    @GetMapping("/evaluate/member/{memberId}")
    @ResponseBody
    public ResponseEntity<?> getReviewsByMemberId(@PathVariable Integer memberId, HttpSession session) {
        try {
            Integer currentMemId = (Integer) session.getAttribute("memId");
            Integer roleId = (Integer) session.getAttribute("roleId");

            if (currentMemId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("請先登入");
            }

            boolean isOwner = currentMemId.equals(memberId);
            boolean isSitter = (roleId != null && roleId == 0);

            if (!isOwner && !isSitter) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("無權限查看此評價");
            }

            List<EvaluateVO> reviews = evaluateService.getReviewsByMemberId(memberId);
            return ResponseEntity.ok(reviews);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("查詢失敗：" + e.getMessage());
        }
    }
}
