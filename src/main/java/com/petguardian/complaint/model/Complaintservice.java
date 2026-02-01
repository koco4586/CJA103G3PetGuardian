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

        // 3. 抓取被檢舉的評價內容 (根據 bookingOrderId)
        if (vo.getBookingOrderId() != null) {
            List<com.petguardian.evaluate.model.EvaluateVO> evals = evaluateRepository
                    .findByBookingOrderId(vo.getBookingOrderId());
            if (!evals.isEmpty()) {
                vo.setReportedContent(evals.get(0).getContent());
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