package com.petguardian.evaluate.service;

import java.util.List;

import com.petguardian.evaluate.model.EvaluateDTO;

import com.petguardian.evaluate.model.EvaluateVO;

public interface EvaluateService {
    void handleSubmission(EvaluateVO vo, String currentRole);

    List<EvaluateDTO> getGroupedReviews();

    List<EvaluateDTO> getByBookingOrderId(Integer bookingOrderId);

    /**
     * 根據保姆 ID 查詢所有評價
     * 
     * @param sitterId 保姆 ID
     * @return 該保姆的所有評價列表
     */
    List<EvaluateVO> getReviewsBySitterId(Integer sitterId);
}
