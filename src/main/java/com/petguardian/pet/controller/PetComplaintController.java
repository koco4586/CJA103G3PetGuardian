package com.petguardian.pet.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.petguardian.complaint.model.ComplaintVO;
import com.petguardian.complaint.model.Complaintservice;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/pet")
public class PetComplaintController {

    @Autowired
    private Complaintservice complaintservice;

    @GetMapping("/review")
    public String showReviewPage(Model model) {
        return "/frontend/review";
    }

    @PostMapping("/review")
    public String userComplaint(HttpSession session, Model model, ComplaintVO vo) {
        if (vo.getBookingOrderId() == null) {
            vo.setBookingOrderId(1);
        }

        if (vo.getReportMemId() == null) {
            vo.setReportMemId(1001);
        }

        if (vo.getToReportedMemId() == null) {
            vo.setToReportedMemId(1002);
        }

        if (vo.getReportReason() == null || vo.getReportReason().trim().isEmpty()) {
            vo.setReportReason("使用者未填寫內容 (系統預設)");
        }

        vo.setReportStatus(0);

        complaintservice.insert(vo);
        return "frontend/review";
    }

    @PostMapping("/submitComplaint")
    @ResponseBody
    public ResponseEntity<?> handleComplaint(ComplaintVO vo) {
        try {
            if (vo.getBookingOrderId() == null) {
                vo.setBookingOrderId(1);
            }

            vo.setReportStatus(0);
            vo.setReportMemId(1001);
            vo.setToReportedMemId(1002);

            complaintservice.insert(vo);
            return ResponseEntity.ok("success");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("後端存檔失敗：" + e.getMessage());
        }
    }
}
