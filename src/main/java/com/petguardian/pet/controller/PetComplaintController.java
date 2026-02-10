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

import org.springframework.transaction.annotation.Transactional;
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
        vo.setCreatedAt(new java.sql.Timestamp(System.currentTimeMillis()));
        vo.setUpdatedAt(new java.sql.Timestamp(System.currentTimeMillis()));

        complaintservice.insert(vo);
        return "frontend/review";
    }

    @PostMapping("/submitComplaint")
    @ResponseBody
    @Transactional
    public ResponseEntity<?> handleComplaint(
            @RequestParam Integer bookingOrderId,
            @RequestParam(required = false) Integer evaluateId, // 🔥 新增：被檢舉的評價ID
            @RequestParam String reportReason,
            HttpSession session) {

        System.out.println(">>> [DEBUG] 收到檢舉請求: bookingOrderId=" + bookingOrderId +
                ", evaluateId=" + evaluateId + ", reason=" + reportReason);
        try {
            if (bookingOrderId == null) {
                return ResponseEntity.badRequest().body("遺失訂單編號 (bookingOrderId is null)");
            }

            // 安全獲取 memId
            Object sessionMemId = session.getAttribute("memId");
            System.out.println(">>> [DEBUG] Session memId type: "
                    + (sessionMemId != null ? sessionMemId.getClass().getName() : "null"));

            Integer memId = null;
            if (sessionMemId instanceof Integer) {
                memId = (Integer) sessionMemId;
            } else if (sessionMemId instanceof String) {
                memId = Integer.valueOf((String) sessionMemId);
            } else if (sessionMemId instanceof Long) {
                memId = ((Long) sessionMemId).intValue();
            }

            if (memId == null) {
                return ResponseEntity.status(401).body("請先登入");
            }

            ComplaintVO vo = new ComplaintVO();
            vo.setBookingOrderId(bookingOrderId);
            vo.setEvaluateId(evaluateId); // 🔥 儲存被檢舉的評價ID
            vo.setReportReason(reportReason);
            vo.setReportMemId(memId);
            vo.setReportStatus(0);
<<<<<<< HEAD

            // 💡 註：createdAt 與 updatedAt 現在由資料庫自動產生 (ComplaintVO 設為 insertable=false)

            System.out.println(">>> [DEBUG] 開始計算被檢舉人...");
=======
            vo.setCreatedAt(new java.sql.Timestamp(System.currentTimeMillis()));
            vo.setUpdatedAt(new java.sql.Timestamp(System.currentTimeMillis()));
>>>>>>> master

            // 自動補齊被檢舉人 (toReportedMemId)
            if (evaluateId != null) {
                // 🔥 如果有 evaluateId，直接從該評價取得被檢舉人
                Optional<EvaluateVO> reviewOpt = evaluateRepository.findById(evaluateId);
                if (reviewOpt.isPresent()) {
                    EvaluateVO review = reviewOpt.get();
                    Integer targetId = review.getSenderId();
                    System.out.println(">>> [DEBUG] 找到評價: senderId=" + targetId + ", roleType=" + review.getRoleType());

                    // 判斷 Sender 是保姆還是會員
                    if (review.getRoleType() != null && review.getRoleType() == 0) {
                        // 保姆評會員 -> Sender 是 SitterId，需轉為 MemId
                        Optional<com.petguardian.sitter.model.SitterVO> sitterOpt = sitterRepository.findById(targetId);
                        if (sitterOpt.isPresent()) {
                            vo.setToReportedMemId(sitterOpt.get().getMemId());
                        } else {
                            System.out.println(">>> [DEBUG] 找不到保姆資料 (targetId=" + targetId + ")");
                        }
                    } else {
                        // 會員評保姆 -> Sender 是 MemId
                        vo.setToReportedMemId(targetId);
                    }
                } else {
                    System.out.println(">>> [DEBUG] 找不到指定評價 (evaluateId=" + evaluateId + ")");
                }
            } else {
                // 🔥 舊邏輯：如果沒有 evaluateId，用訂單ID查找
                System.out.println(">>> [DEBUG] 使用舊邏輯，由 bookingOrderId 查找評價...");
                List<EvaluateVO> reviews = evaluateRepository.findByBookingOrderId(bookingOrderId);
                if (reviews != null && !reviews.isEmpty()) {
                    for (EvaluateVO review : reviews) {
                        if (review.getSenderId() != null && !review.getSenderId().equals(memId)) {
                            Integer targetId = review.getSenderId();
                            if (review.getRoleType() != null && review.getRoleType() == 0) {
                                Optional<com.petguardian.sitter.model.SitterVO> sitterOpt = sitterRepository
                                        .findById(targetId);
                                if (sitterOpt.isPresent()) {
                                    vo.setToReportedMemId(sitterOpt.get().getMemId());
                                }
                            } else {
                                vo.setToReportedMemId(targetId);
                            }
                            break;
                        }
                    }
                }
            }

            if (vo.getToReportedMemId() == null) {
                System.out.println(">>> [DEBUG] 無法識別被檢舉人，回傳 400");
                return ResponseEntity.badRequest().body("無法識別被檢舉人的會員身份，請確認該保姆/會員連結有效");
            }

            System.out.println(">>> [DEBUG] 被檢舉人 ID: " + vo.getToReportedMemId());

            // 🔥 檢舉功能：立即隱藏被檢舉的評價
            if (evaluateId != null) {
                // 🔥 新邏輯：只隱藏被檢舉的那一條評價
                Optional<EvaluateVO> reviewOpt = evaluateRepository.findById(evaluateId);
                if (reviewOpt.isPresent()) {
                    EvaluateVO review = reviewOpt.get();

                    // 防止自我檢舉
                    if (review.getSenderId() != null && review.getSenderId().equals(memId)) {
                        return ResponseEntity.badRequest().body("您不能檢舉自己的評價");
                    }

                    System.out.println(">>> [DEBUG] 正在隱藏評價...");
                    review.setIsHidden(1); // 標記為已隱藏
                    evaluateRepository.save(review);
                }
            } else {
                // 🔥 舊邏輯：隱藏該訂單的所有評價 (向後兼容)
                List<EvaluateVO> reviews = evaluateRepository.findByBookingOrderId(bookingOrderId);
                boolean hasOtherPartyReview = false;
                if (reviews != null) {
                    for (EvaluateVO review : reviews) {
                        if (review.getSenderId() != null && !review.getSenderId().equals(memId)) {
                            hasOtherPartyReview = true;
                            break;
                        }
                    }
                }

                if (!hasOtherPartyReview) {
                    return ResponseEntity.badRequest().body("您不能檢舉自己的評價 (或對方尚未發表評價)");
                }

                if (reviews != null && !reviews.isEmpty()) {
                    for (EvaluateVO review : reviews) {
                        review.setIsHidden(1);
                        evaluateRepository.save(review);
                    }
                }
            }

            System.out.println(">>> [DEBUG] 正在存入檢舉紀錄...");
            complaintservice.insert(vo);
            System.out.println(">>> [DEBUG] 檢舉成功存檔");
            return ResponseEntity.ok("success");

        } catch (Exception e) {
            System.err.println(">>> [ERROR] 檢舉處理發生異常!");
            e.printStackTrace();
            String errorMsg = e.getClass().getSimpleName() + ": " + e.getMessage();
            // 如果有深刻的原因，嘗試獲取 Cause
            if (e.getCause() != null) {
                errorMsg += " -> Cause: " + e.getCause().getMessage();
            }
            return ResponseEntity.status(500).body("後端處理失敗：" + errorMsg);
        }
    }
}
