package com.petguardian.pet.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.petguardian.complaint.model.ComplaintVO;
import com.petguardian.complaint.model.Complaintservice;

import jakarta.servlet.http.HttpSession;
import com.petguardian.evaluate.model.EvaluateVO;
import com.petguardian.evaluate.model.EvaluateRepository;
import com.petguardian.sitter.model.SitterRepository;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/pet")
public class PetComplaintController {

    @Autowired
    private Complaintservice complaintservice;

    @Autowired
    private EvaluateRepository evaluateRepository;

    @Autowired
    private SitterRepository sitterRepository;

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
    public ResponseEntity<?> handleComplaint(ComplaintVO vo, HttpSession session) {
        System.out.println(">>> 收到檢舉請求: bookingOrderId=" + vo.getBookingOrderId() + ", reason=" + vo.getReportReason());
        try {
            if (vo.getBookingOrderId() == null) {
                return ResponseEntity.badRequest().body("遺失訂單編號 (bookingOrderId is null)");
            }

            Integer memId = (Integer) session.getAttribute("memId");
            if (memId == null) {
                return ResponseEntity.status(401).body("請先登入");
            }

            // 自動補齊被檢舉人 (toReportedMemId)
            if (vo.getToReportedMemId() == null) {
                List<EvaluateVO> reviews = evaluateRepository.findByBookingOrderId(vo.getBookingOrderId());
                System.out.println(">>> 查詢關聯評價數量: " + (reviews != null ? reviews.size() : 0));

                if (reviews != null && !reviews.isEmpty()) {
                    for (EvaluateVO review : reviews) {
                        // 1. 如果是「對方寫的評價」，則檢舉對方 (Sender)
                        if (review.getSenderId() != null && !review.getSenderId().equals(memId)) {
                            Integer targetId = review.getSenderId();

                            // 關鍵修正：判斷 Sender 是保姆還是會員
                            // RoleType 0: 保姆評會員 -> Sender 是 SitterId
                            // RoleType 1: 會員評保姆 -> Sender 是 MemId
                            if (review.getRoleType() != null && review.getRoleType() == 0) {
                                // 當前發言者是保姆 (SitterId)，需轉為 MemId
                                Optional<com.petguardian.sitter.model.SitterVO> sitterOpt = sitterRepository
                                        .findById(targetId);
                                if (sitterOpt.isPresent()) {
                                    vo.setToReportedMemId(sitterOpt.get().getMemId());
                                }
                            } else {
                                // 當前發言者是會員，直接使用其 MemId
                                vo.setToReportedMemId(targetId);
                            }
                            break;
                        }
                    }

                    // 2. 如果沒找到「對方寫的」（例如檢舉自己給對方的評分），則對象是 Receiver
                    if (vo.getToReportedMemId() == null) {
                        EvaluateVO firstReview = reviews.get(0);
                        Integer receiverId = firstReview.getReceiverId();
                        // RoleType 1: 會員評保姆 -> Receiver 是 SitterId
                        if (firstReview.getRoleType() != null && firstReview.getRoleType() == 1) {
                            Optional<com.petguardian.sitter.model.SitterVO> sitterOpt = sitterRepository
                                    .findById(receiverId);
                            if (sitterOpt.isPresent()) {
                                vo.setToReportedMemId(sitterOpt.get().getMemId());
                            }
                        } else {
                            vo.setToReportedMemId(receiverId);
                        }
                    }
                }
            }

            System.out.println(">>> 最終推導被檢舉人 MemberID: " + vo.getToReportedMemId());

            // 設置檢舉人與預設狀態
            vo.setReportMemId(memId);
            vo.setReportStatus(0);

            if (vo.getReportReason() == null || vo.getReportReason().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("請填寫檢舉理由");
            }

            // 再次檢查必要欄位，防止資料庫外鍵約束失敗
            if (vo.getToReportedMemId() == null) {
                return ResponseEntity.badRequest().body("無法識別被檢舉人的會員身份，請確認該保姆/會員連結有效");
            }

            // 🔥 檢舉功能：檢舉送出時，只隱藏「被檢舉人」撰寫的評論
            // 📌 重要：不要隱藏該訂單的所有評論，只隱藏被檢舉的那則
            if (vo.getToReportedMemId() != null) {
                List<EvaluateVO> reviews = evaluateRepository.findByBookingOrderId(vo.getBookingOrderId());
                if (reviews != null && !reviews.isEmpty()) {
                    for (EvaluateVO review : reviews) {
                        // 只隱藏被檢舉人撰寫的評價 (senderId == toReportedMemId)
                        if (review.getSenderId() != null && review.getSenderId().equals(vo.getToReportedMemId())) {
                            review.setIsHidden(1); // 標記為已隱藏
                            evaluateRepository.save(review);
                            System.out.println(">>> 已隱藏被檢舉評價 (EvaluateID: " + review.getEvaluateId() +
                                    ", SenderId: " + review.getSenderId() + ")");
                            break; // 找到後就停止，一個人在一個訂單只會有一則評價
                        }
                    }
                }
            }

            complaintservice.insert(vo);
            return ResponseEntity.ok("success");

        } catch (Exception e) {
            e.printStackTrace();
            String errorMsg = e.getClass().getSimpleName() + ": " + e.getMessage();
            return ResponseEntity.status(500).body("後端存檔失敗：" + errorMsg);
        }
    }
}
