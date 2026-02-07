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

        // 🔥 批次獲取檢舉次數以提升效能 (解決 N+1 問題)
        java.util.List<Integer> evalIds = list.stream()
                .map(ComplaintVO::getEvaluateId)
                .filter(id -> id != null)
                .distinct()
                .collect(java.util.stream.Collectors.toList());

        java.util.Map<Integer, Long> countMap = new java.util.HashMap<>();
        if (!evalIds.isEmpty()) {
            java.util.List<Object[]> counts = repository.countComplaintsByEvaluateIds(evalIds);
            for (Object[] obj : counts) {
                countMap.put((Integer) obj[0], (Long) obj[1]);
            }
        }

        for (ComplaintVO vo : list) {
            if (vo.getEvaluateId() != null) {
                vo.setEvaluationComplaintCount(countMap.getOrDefault(vo.getEvaluateId(), 0L));
            }
            populateTransientFields(vo);
        }

        // 🔥 新增：計算每一筆紀錄的「案發序號」(Sequence Number)
        // 依據 evaluateId 分組，並依據 bookingReportId 排序
        java.util.Map<Integer, java.util.List<ComplaintVO>> grouped = list.stream()
                .filter(vo -> vo.getEvaluateId() != null)
                .collect(java.util.stream.Collectors.groupingBy(ComplaintVO::getEvaluateId));

        grouped.forEach((evalId, subList) -> {
            subList.sort(java.util.Comparator.comparing(ComplaintVO::getBookingReportId));
            for (int i = 0; i < subList.size(); i++) {
                subList.get(i).setReportSequence(i + 1);
            }
        });

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

        // 3. 抓取被檢舉的評價內容 (根據 bookingOrderId)
        if (vo.getBookingOrderId() != null) {
            List<com.petguardian.evaluate.model.EvaluateVO> evals = evaluateRepository
                    .findByBookingOrderId(vo.getBookingOrderId());
            if (!evals.isEmpty()) {
                // 🔥 根據被檢舉人來判斷應該取哪個評論
                // 被檢舉人是評論的發送者（senderId）
                com.petguardian.evaluate.model.EvaluateVO targetEval = evals.stream()
                        .filter(e -> e.getSenderId().equals(vo.getToReportedMemId()))
                        .findFirst()
                        .orElse(evals.get(0)); // 如果找不到，就取第一個（向後相容）

                vo.setReportedContent(targetEval.getContent());
            }
        }

        // 4. 計算被檢舉的評價總次數 (供 getOne 使用)
        if (vo.getEvaluateId() != null
                && (vo.getEvaluationComplaintCount() == null || vo.getEvaluationComplaintCount() == 0)) {
            java.util.List<Object[]> counts = repository
                    .countComplaintsByEvaluateIds(java.util.List.of(vo.getEvaluateId()));
            if (!counts.isEmpty()) {
                vo.setEvaluationComplaintCount((Long) counts.get(0)[1]);
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