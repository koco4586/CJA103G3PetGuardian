package com.petguardian.evaluate.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.petguardian.evaluate.model.EvaluateDTO;
import com.petguardian.evaluate.model.EvaluateRepository;
import com.petguardian.evaluate.model.EvaluateVO;
import com.petguardian.sitter.model.SitterMemberRepository;
import com.petguardian.sitter.model.SitterMemberVO;
import com.petguardian.sitter.model.SitterRepository;
import com.petguardian.sitter.model.SitterVO;
import com.petguardian.complaint.model.Complaintrepository;

@Service
public class EvaluateServiceImpl implements EvaluateService {

    @Autowired
    private EvaluateRepository repo;

    @Autowired
    private SitterMemberRepository sitterMemberRepository;

    @Autowired
    private SitterRepository sitterRepository;

    @Autowired
    private Complaintrepository complaintRepository;

    @Override
    public void handleSubmission(EvaluateVO vo, String currentRole) {
        if (vo.getRoleType() == null) {
            if ("SITTER".equals(currentRole)) {
                vo.setRoleType(0); // 0 = 保姆
            } else {
                vo.setRoleType(1); // 1 = 會員
            }
        }

        // 🔥 限制：檢查同一方向評價次數是否已達上限 (2次)
        if (vo.getBookingOrderId() != null) {
            List<EvaluateVO> existing = repo.findByBookingOrderId(vo.getBookingOrderId());
            long count = existing.stream()
                    .filter(e -> e.getRoleType() != null && e.getRoleType().equals(vo.getRoleType()))
                    .count();
            if (count >= 2) {
                throw new RuntimeException("該訂單評價次數已達上限 (2次)");
            }
        }

        repo.save(vo);
    }

    @Override
    public List<EvaluateDTO> getByBookingOrderId(Integer bookingOrderId) {
        List<EvaluateVO> vos = repo.findByBookingOrderId(bookingOrderId);
        List<EvaluateDTO> result = new ArrayList<>();
        if (vos == null || vos.isEmpty())
            return result;

        EvaluateDTO dto = new EvaluateDTO();
        dto.setBookingOrderId(bookingOrderId);

        for (EvaluateVO vo : vos) {
            if (vo.getRoleType() != null && vo.getRoleType() == 1) { // 1 = 會員
                fillMemberData(dto, vo);
            } else { // 0 = 保姆
                fillSitterData(dto, vo);
            }
        }
        result.add(dto);
        return result;
    }

    @Override
    public List<EvaluateDTO> getGroupedReviews() {
        List<EvaluateVO> allReviews = repo.findAll();
        if (allReviews == null)
            return new ArrayList<>();

        // 分組並過濾 null 的 OrderId
        Map<Integer, List<EvaluateVO>> grouped = allReviews.stream()
                .filter(vo -> vo.getBookingOrderId() != null)
                .collect(Collectors.groupingBy(EvaluateVO::getBookingOrderId));

        List<EvaluateDTO> result = new ArrayList<>();

        grouped.forEach((orderId, reviews) -> {
            EvaluateDTO dto = new EvaluateDTO();
            dto.setBookingOrderId(orderId);

            for (EvaluateVO vo : reviews) {
                if (vo.getRoleType() != null && vo.getRoleType() == 1) { // 統一角色：1 = 會員
                    fillMemberData(dto, vo);
                } else if (vo.getRoleType() != null) { // 0 = 保姆
                    fillSitterData(dto, vo);
                }
            }
            // 只有當 DTO 裡至少有一方資料才加入
            if (dto.getMemberId() != null || dto.getSitterId() != null) {
                result.add(dto);
            }
        });
        return result;
    }

    // 抽離重複代碼，確保邏輯一致
    private void fillMemberData(EvaluateDTO dto, EvaluateVO vo) {
        dto.setMemberId(vo.getSenderId());
        dto.setMemberName(vo.getSenderName());
        dto.setMemberRating(vo.getStarRating());
        dto.setMemberContent(vo.getContent());
        dto.setMemberCreateTime(vo.getCreateTimeText());
        dto.setMemberEvaluateId(vo.getEvaluateId());
    }

    private void fillSitterData(EvaluateDTO dto, EvaluateVO vo) {
        dto.setSitterId(vo.getSenderId());
        dto.setSitterName(vo.getSenderName());
        dto.setSitterRating(vo.getStarRating());
        dto.setSitterContent(vo.getContent());
        dto.setSitterCreateTime(vo.getCreateTimeText());
        dto.setSitterEvaluateId(vo.getEvaluateId());
    }

    @Override
    public List<EvaluateVO> getReviewsBySitterId(Integer sitterId) {
        List<EvaluateVO> reviews = repo.findByReceiverId(sitterId);

        // 過濾條件：
        // 1. 只保留 roleType=1 (會員評保母)
        // 2. 過濾隱藏與刪除的評論
        reviews = reviews.stream()
                .filter(r -> r.getRoleType() != null && r.getRoleType() == 1)
                .filter(r -> r.getIsHidden() == null || r.getIsHidden() == 0)
                .collect(Collectors.toList());

        fillSenderNames(reviews);
        fillComplaintCounts(reviews);
        return reviews;
    }

    @Override
    public List<EvaluateVO> getReviewsByMemberId(Integer memberId) {
        // 查詢該會員收到的評價 (roleType = 0 表示保姆評會員)
        List<EvaluateVO> reviews = repo.findByReceiverIdAndRoleType(memberId, 0);

        // 過濾隱藏與刪除的評論
        reviews = reviews.stream()
                .filter(r -> r.getIsHidden() == null || r.getIsHidden() == 0)
                .collect(Collectors.toList());

        fillSenderNames(reviews);
        fillComplaintCounts(reviews);
        return reviews;
    }

    /**
     * 填充評價者名字
     */
    private void fillSenderNames(List<EvaluateVO> reviews) {
        if (reviews == null || reviews.isEmpty()) {
            return;
        }

        for (EvaluateVO review : reviews) {
            try {
                if (review.getSenderId() == null || review.getRoleType() == null) {
                    continue;
                }

                String senderName = null;
                Integer senderMemId = null;

                if (review.getRoleType() == 0) {
                    SitterVO sitter = sitterRepository.findById(review.getSenderId()).orElse(null);
                    if (sitter != null) {
                        senderName = sitter.getSitterName();
                        senderMemId = sitter.getMemId();
                    }
                } else if (review.getRoleType() == 1) {
                    SitterMemberVO member = sitterMemberRepository.findById(review.getSenderId()).orElse(null);
                    if (member != null) {
                        senderName = member.getMemName();
                        senderMemId = review.getSenderId();
                    }
                }

                review.setSenderName(senderName);
                review.setSenderMemId(senderMemId);
            } catch (Exception e) {
                System.err.println("❌ 填充評價發送者資料時出錯: " + e.getMessage());
                review.setSenderName("未知用戶");
            }
        }
    }

    /**
     * 批次填充評價的檢舉計數 (解決 N+1)
     */
    private void fillComplaintCounts(List<EvaluateVO> reviews) {
        if (reviews == null || reviews.isEmpty()) {
            return;
        }

        List<Integer> evalIds = reviews.stream()
                .map(EvaluateVO::getEvaluateId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());

        if (evalIds.isEmpty()) {
            return;
        }

        List<Object[]> counts = complaintRepository.countComplaintsByEvaluateIds(evalIds);
        Map<Integer, Long> countMap = counts.stream()
                .collect(Collectors.toMap(row -> (Integer) row[0], row -> (Long) row[1], (a, b) -> a));

        for (EvaluateVO review : reviews) {
            review.setComplaintCount(countMap.getOrDefault(review.getEvaluateId(), 0L));
        }
    }

    @Override
    public Double getAverageRatingBySitterId(Integer sitterId) {
        Double avg = repo.getAverageRatingBySitterId(sitterId);
        if (avg == null) {
            return null;
        }
        return Math.round(avg * 10.0) / 10.0;
    }

    public Long getReviewCountBySitterId(Integer sitterId) {
        return repo.getReviewCountBySitterId(sitterId);
    }

    @Override
    public EvaluateVO getById(Integer evalId) {
        return repo.findById(evalId).orElse(null);
    }
}
