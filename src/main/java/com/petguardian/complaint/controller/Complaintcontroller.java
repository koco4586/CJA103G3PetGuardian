package com.petguardian.complaint.controller;

import jakarta.servlet.http.HttpSession;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.petguardian.complaint.model.ComplaintVO;
import com.petguardian.complaint.model.Complaintservice;

@Controller
@RequestMapping("/admin")
public class Complaintcontroller {

    @Autowired
    private Complaintservice complaintservice;

    @Autowired
    private com.petguardian.evaluate.model.EvaluateRepository evaluateRepository;

    // 🔹 後台管理頁面（管理員審核用）
    @GetMapping("/reviews1")
    public String adminReviews(HttpSession session, Model model) {
        // 檢查是否為管理員
        // Integer userRole = (Integer) session.getAttribute("userRole");

        // if (userRole == null || userRole != 1) {
        // return "redirect:/member/login";
        // }\r
        List<ComplaintVO> list = complaintservice.getAll();
        model.addAttribute("complaintList", list);
        return "backend/reviews1"; // 對應 templates/backend/reviews.html
    }

    @PostMapping("/pet/updateReportStatus")
    @ResponseBody
    public ResponseEntity<?> updateStatus(@RequestParam Integer reportId, @RequestParam Integer status) {
        try {
            // 1. 先從資料庫撈出原始資料 (確保 ID 存在)
            ComplaintVO vo = complaintservice.getOne(reportId);
            if (vo == null) {
                return ResponseEntity.status(404).body("找不到該筆申訴");
            }

            // 2. 更新檢舉狀態（不處理評論，評論保持隱藏）
            vo.setReportStatus(status);
            complaintservice.insert(vo); // 調用你 Service 裡已有的 save/insert 邏輯

            // 3. 回傳簡單的 Map
            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("success", true);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("伺服器錯誤");
        }
    }

    // 🔥 新增：處理已結案紀錄的評論（刪除或解除隱藏）
    @PostMapping("/pet/handleReview")
    @ResponseBody
    public ResponseEntity<?> handleReview(@RequestParam Integer reportId, @RequestParam String action) {
        try {
            // 1. 先從資料庫撈出原始資料
            ComplaintVO vo = complaintservice.getOne(reportId);
            if (vo == null) {
                return ResponseEntity.status(404).body("找不到該筆申訴");
            }

            // 2. 根據 action 處理評論
            List<com.petguardian.evaluate.model.EvaluateVO> reviews = evaluateRepository
                    .findByBookingOrderId(vo.getBookingOrderId());

            if (reviews != null && !reviews.isEmpty()) {
                for (com.petguardian.evaluate.model.EvaluateVO review : reviews) {
                    if ("delete".equals(action)) {
                        review.setIsHidden(2); // 刪除評論
                    } else if ("unhide".equals(action)) {
                        review.setIsHidden(0); // 解除隱藏
                    }
                    evaluateRepository.save(review);
                }
            }

            // 3. 回傳成功
            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("success", true);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("伺服器錯誤");
        }
    }

}
