package com.sitter.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sitter.model.MemberVO;
import com.sitter.model.SitterApplicationService;
import com.sitter.model.SitterApplicationVO;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/sitter") //api
@RequiredArgsConstructor //Lombok 自動生成建構子，注入 Service
public class SitterApplicationController {
	
	private final SitterApplicationService sitterApplicationService;
	
	 //檢查會員是否已經申請過保姆GET/sitter/check-application/{memId}
	@GetMapping("/check-application/{memId}")
    public ResponseEntity<Boolean> hasApplied(@PathVariable Integer memId) {
        boolean applied = sitterApplicationService.hasApplied(memId);
        return ResponseEntity.ok(applied);
    }
	
	//會員提交保姆申請POST/sitter/submit-application
	@PostMapping("/submit-application")
	public ResponseEntity<SitterApplicationVO> submitApplication(
			@RequestParam Integer memId,
			@RequestParam String intro,
            @RequestParam String experience 
			){
		// 模擬取得會員資訊，實際應該從登入 Session 或 Token 取得
		MemberVO member = new MemberVO();
        member.setMemId(memId);
        try {
            SitterApplicationVO application = sitterApplicationService.submitApplication(member, intro, experience);
            return ResponseEntity.ok(application);
        } catch (IllegalStateException e) {
        	// 如果會員已經申請過，回傳 400 Bad Request
            return ResponseEntity.badRequest().body(null);
        }
	}
	
	//會員查詢自己的保姆申請GET/sitter/my-application/{memId}
	@GetMapping("/my-application/{memId}")
	 public ResponseEntity<SitterApplicationVO> getMyApplication(@PathVariable Integer memId){
		SitterApplicationVO application = sitterApplicationService.getMemberApplication(memId);
		if(application == null) {
			return ResponseEntity.notFound().build();// 沒申請回傳 404
		}
		return ResponseEntity.ok(application);
	}
	
	// 查詢待審核保姆申請GET/sitter/rpending-applications
		@GetMapping("/pending-applications")
		public ResponseEntity<List<SitterApplicationVO>> getPendingApplications(
				 @RequestParam(defaultValue = "0") Byte pendingStatus // 預設 0 = 待審核
				){
			List<SitterApplicationVO> list = sitterApplicationService.getPendingApplications(pendingStatus);
			 return ResponseEntity.ok(list);
		}
		
		/**
	     * 管理員審核申請
	     * POST /sitter/review-application
	     */
	    @PostMapping("/review-application")
	    public ResponseEntity<SitterApplicationVO> reviewApplication(
	            @RequestParam Integer appId,
	            @RequestParam (required = false, defaultValue = "false") Boolean approve,
	            @RequestParam(required = false) String reviewNote
	    ) 
	    
	    {
	    	try {
	    	    SitterApplicationVO updatedApp = sitterApplicationService.reviewApplication(appId, approve, reviewNote);
	    	    return ResponseEntity.ok(updatedApp);
	    	} catch (IllegalArgumentException e) {
	    		// 如果申請ID不存在，回傳 404 Not Found
	    	    return ResponseEntity.notFound().build();
	    	}
	    }
}


