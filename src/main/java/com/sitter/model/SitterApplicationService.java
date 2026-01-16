package com.sitter.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional // 確保資料操作有保護
public class SitterApplicationService {
	private final SitterApplicationRepository sitterApplicationRepository;
	/**
     * 檢查會員是否已經提交過保姆申請
     * 
     * @param memId 會員ID
     * @return true 如果已經申請過
	*/
	
	// ===== 檢查會員是否有申請過保姆 =====
	public boolean hasApplied(Integer memId) {
        return sitterApplicationRepository.existsByMember_MemId(memId);
    }
	 /**
     * 會員提交新的保姆申請
     * 
     * @param member 會員資訊
     * @param intro 個人簡介
     * @param experience 相關經驗
     * @return 新增的 SitterApplicationVO
     * @throws IllegalStateException 如果會員已經申請過
     */
	
	// ===== 會員提交新的保姆申請 =====
	 public SitterApplicationVO submitApplication(MemberVO member, String intro, String experience) {
	        if (hasApplied(member.getMemId())) {
	            throw new IllegalStateException("會員已經提交過保姆申請");
	        }
	        SitterApplicationVO application = new SitterApplicationVO();
	        application.setMember(member);
	        application.setAppIntro(intro);
	        application.setAppExperience(experience);
	        // appStatus 預設由資料庫設定（insertable=false）
	        return sitterApplicationRepository.save(application);     
	 }
	 
	 /**
	     * @param memId 會員ID
	     * @return SitterApplicationVO 或 null 如果沒有申請
	     */
	 // ===== 會員查詢自己的保姆申請 =====
	    @Transactional(readOnly = true)
	    public SitterApplicationVO getMemberApplication(Integer memId) {
	        return sitterApplicationRepository.findByMember_MemId(memId);
	    }
	    
	    /**
	     * @param pendingStatus 待審核狀態值，例如 0 表示待審核
	     * @return 待審核申請列表，依申請時間倒序排序
	     */
	 // ===== 管理員查詢所有待審核的保姆申請 =====
	    @Transactional(readOnly = true)
	    public List<SitterApplicationVO> getPendingApplications(Byte pendingStatus) {
	        return sitterApplicationRepository.findByAppStatusOrderByAppCreatedAtDesc(pendingStatus);
	    }
	    
	    /**
	     * @param appId 申請ID
	     * @param approve 是否通過
	     * @param reviewNote 審核意見
	     * @return 更新後的 SitterApplicationVO
	     * @throws IllegalArgumentException 如果申請ID不存在
	     */
	    // ===== 管理員審核會員申請 =====
	    public SitterApplicationVO reviewApplication(Integer appId, Boolean approve, String reviewNote) {
	        Optional<SitterApplicationVO> optional = sitterApplicationRepository.findById(appId);

	        if (optional.isEmpty()) {
	            throw new IllegalArgumentException("申請ID不存在: " + appId);
	        }

	        SitterApplicationVO application = optional.get();
	        // 如果 approve 為 null，預設拒絕
	        if (approve == null) {
	            approve = false;
	        }
	       

	        
	        // 設定審核狀態，假設 1 = 通過，2 = 拒絕
	        application.setAppStatus((byte) (approve ? 1 : 2));
	        application.setAppReviewNote(reviewNote);
	        application.setAppReviewAt(LocalDateTime.now());

	        return sitterApplicationRepository.save(application);
	    }
}
