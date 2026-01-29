package com.petguardian.evaluate.service;



import java.util.List;

import com.petguardian.evaluate.model.EvaluateDTO;

import com.petguardian.evaluate.model.EvaluateVO;



public interface EvaluateService {
    void handleSubmission(EvaluateVO vo, String currentRole);
    List<EvaluateDTO> getGroupedReviews();
    
    List<EvaluateDTO> getByBookingOrderId(Integer bookingOrderId);
}
	
	

