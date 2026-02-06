package com.petguardian.complaint.model;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class Complaintservice {
    @Autowired
    private Complaintrepository repository;

    @Autowired
    private com.petguardian.member.repository.management.MemberManagementRepository memberRepository;

    @Autowired
    private com.petguardian.evaluate.model.EvaluateRepository evaluateRepository;

    public void insert(ComplaintVO vo) {
        repository.save(vo);
    }

    public List<ComplaintVO> getAll() {
        List<ComplaintVO> list = repository.findAll();
        for (ComplaintVO vo : list) {
            populateTransientFields(vo);
        }
        return list;
    }

    public ComplaintVO getOne(Integer id) {
        Optional<ComplaintVO> optional = repository.findById(id);
        ComplaintVO vo = optional.orElse(null);
        if (vo != null) {
            populateTransientFields(vo);
        }
        return vo;
    }

    private void populateTransientFields(ComplaintVO vo) {
        // 1. 抓取檢舉人姓名
        if (vo.getReportMemId() != null) {
            memberRepository.findById(vo.getReportMemId()).ifPresent(m -> vo.setReporterName(m.getMemName()));
        }

        // 2. 抓取被檢舉人姓名
        if (vo.getToReportedMemId() != null) {
            memberRepository.findById(vo.getToReportedMemId()).ifPresent(m -> vo.setAccusedName(m.getMemName()));
        }

        // 3. 抓取被檢舉的評價內容
        // 📌 重要邏輯：被檢舉的評價是「被檢舉人」寫的那則評價
        // - toReportedMemId = 被檢舉人（寫評價的人）
        // - senderId = 評價的發送者
        // - 因此要找 senderId == toReportedMemId 的評價
        if (vo.getBookingOrderId() != null && vo.getToReportedMemId() != null) {
            List<com.petguardian.evaluate.model.EvaluateVO> evals = evaluateRepository
                    .findByBookingOrderId(vo.getBookingOrderId());

            if (!evals.isEmpty()) {
                // 🔥 關鍵修正：根據被檢舉人來精確匹配評價
                // 被檢舉人 (toReportedMemId) = 評價的發送者 (senderId)
                com.petguardian.evaluate.model.EvaluateVO targetEval = evals.stream()
                        .filter(e -> e.getSenderId() != null && e.getSenderId().equals(vo.getToReportedMemId()))
                        .findFirst()
                        .orElse(null);

                if (targetEval != null) {
                    vo.setReportedContent(targetEval.getContent());
                } else {
                    // 如果找不到對應的評價，設定提示訊息
                    vo.setReportedContent("[系統提示] 找不到被檢舉人的評價內容 (訂單ID: " + vo.getBookingOrderId() +
                            ", 被檢舉人ID: " + vo.getToReportedMemId() + ")");
                    System.err.println("⚠️ 檢舉案件 #" + vo.getBookingReportId() +
                            " 找不到對應的評價 (訂單:" + vo.getBookingOrderId() +
                            ", 被檢舉人:" + vo.getToReportedMemId() + ")");
                }
            } else {
                vo.setReportedContent("[系統提示] 此訂單沒有任何評價");
            }
        }
    }

    // 更新狀態
    public void updateStatus(Integer id, Integer newStatus) {
        ComplaintVO vo = getOne(id);
        if (vo != null) {
            vo.setReportStatus(newStatus);
            repository.save(vo); // JPA 會根據 ID 自動執行 Update
        }
    }
}