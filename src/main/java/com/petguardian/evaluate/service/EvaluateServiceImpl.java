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

@Service
public class EvaluateServiceImpl implements EvaluateService {

    @Autowired
    private EvaluateRepository repo;

    @Override
    public void handleSubmission(EvaluateVO vo, String currentRole) {
        if (vo.getRoleType() == null) {
            if ("SITTER".equals(currentRole)) {
                vo.setRoleType(0); // 0 = 保姆
            } else {
                vo.setRoleType(1); // 1 = 會員
            }
        }
        repo.save(vo);
    }

    @Override
    public List<EvaluateDTO> getByBookingOrderId(Integer bookingOrderId) {
        List<EvaluateVO> vos = repo.findByBookingOrderId(bookingOrderId);
        List<EvaluateDTO> result = new ArrayList<>();
        if (vos == null || vos.isEmpty()) return result;

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
        if (allReviews == null) return new ArrayList<>();

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
    }

    private void fillSitterData(EvaluateDTO dto, EvaluateVO vo) {
        dto.setSitterId(vo.getSenderId());
        dto.setSitterName(vo.getSenderName());
        dto.setSitterRating(vo.getStarRating());
        dto.setSitterContent(vo.getContent());
        dto.setSitterCreateTime(vo.getCreateTimeText());
    }
}

		
	

