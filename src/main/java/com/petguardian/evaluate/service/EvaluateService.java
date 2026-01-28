package com.petguardian.evaluate.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.petguardian.evaluate.model.EvaluateDTO;
import com.petguardian.evaluate.model.EvaluateRepository;
import com.petguardian.evaluate.model.EvaluateVO;

import jakarta.servlet.http.HttpSession;

public class EvaluateService {

	@Autowired
    private EvaluateRepository evaluateRepository;
	
	public void handleSubmission(EvaluateVO vo, String currentRole) {
        // 根據身分設定 ROLE_TYPE
        if ("SITTER".equals(currentRole)) {
            vo.setRoleType(1); // 1: 保姆評會員 (回覆)
            // 在回覆邏輯中，SENDER 是保姆，RECEIVER 是會員
        } else {
            vo.setRoleType(0); // 0: 會員評保姆
        }
        
        // 執行存檔，這會把資料寫入你的資料庫表格
        evaluateRepository.save(vo); 
    }

	@PostMapping("/evaluate/save")
	@ResponseBody // 使用 AJAX 送出，所以回傳字串或 JSON
	public String saveEvaluate(@RequestBody EvaluateVO vo, HttpSession session) {
	    
	    // 1. 從 Session 判斷身分 (假設登入時有存 userRole)
	    // 0 代表會員評保姆，1 代表保姆評會員
	    Integer roleType = (Integer) session.getAttribute("userRoleType"); 
	    Integer currentUserId = (Integer) session.getAttribute("userId");

	    if (vo == null || currentUserId == null) {
	        return "error: 未登入或資料錯誤";
	    }

	    // 2. 根據身分設定 SENDER 與 RECEIVER
	    vo.setRoleType(roleType);
	    vo.setSenderId(currentUserId);
	    vo.setCreateTime(new java.sql.Timestamp(System.currentTimeMillis()));

	    // 如果是保姆回覆 (roleType == 1)，前端傳來的 receiverId 就會是會員 ID
	    // 如果是會員評論 (roleType == 0)，前端傳來的 receiverId 就會是保姆 ID

	    // 3. 執行存檔 (非 static 呼叫)
	    evaluateRepository.save(vo); 

	    return "success";
	}
	
	
	@Autowired
    private EvaluateRepository repo;
	public List<EvaluateDTO> getGroupedReviews() {
	    
		
		
		
		// 假設從 DB 拿到所有評價
	    List<EvaluateVO> allReviews = repo.findAll(); 
	    
	    // 使用 Map 根據 Order ID 分組
	    Map<Integer, List<EvaluateVO>> grouped = allReviews.stream()
	        .collect(Collectors.groupingBy(EvaluateVO::getBookingOrderId));

	    List<EvaluateDTO> result = new ArrayList<>();
	    
	    grouped.forEach((orderId, reviews) -> {
	    	EvaluateDTO dto = new EvaluateDTO();
	        dto.setBookingOrderId(orderId);
	        
	        for (EvaluateVO vo : reviews) {
	            if (vo.getRoleType() == 0) { // 假設 0 是會員
	                dto.setMemberName(vo.getSenderName());
	                dto.setMemberRating(vo.getStarRating());
	                dto.setMemberContent(vo.getContent());
	                dto.setMemberCreateTime(vo.getCreateTimeText());
	            } else { // 1 是保姆
	                dto.setSitterName(vo.getSenderName());
	                dto.setSitterRating(vo.getStarRating());
	                dto.setSitterContent(vo.getContent());
	                dto.setSitterCreateTime(vo.getCreateTimeText());
	            }
	        }
	        result.add(dto);
	    });
	    
	    return result;
	}
}
