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

    /**
     * 根據會員 ID 查詢所有保母對該會員的評價
     * 
     * @param memberId 會員 ID
     * @return 該會員被保母評價的所有紀錄
     */
    List<EvaluateVO> getReviewsByMemberId(Integer memberId);

    /**
     * 根據保姆 ID 計算平均評分
     * 
     * @param sitterId 保姆 ID
     * @return 平均評分 (若無評價則回傳 0.0 或 null)
     */
    Double getAverageRatingBySitterId(Integer sitterId);
}
