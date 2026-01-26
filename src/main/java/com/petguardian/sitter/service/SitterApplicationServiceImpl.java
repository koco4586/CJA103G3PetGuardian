package com.petguardian.sitter.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.petguardian.sitter.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 保姆申請業務邏輯實作
 * 
 * 提供會員申請成為保姆、管理員審核申請等功能的實作
 */
@Service("sitterApplicationService")
public class SitterApplicationServiceImpl implements SitterApplicationService {

    @Autowired
    private SitterApplicationRepository repository;

    @Autowired
    private SitterMemberRepository sitterMemberRepository;

    @Autowired
    private SitterService sitterService;

    /**
     * 會員申請成為保姆
     * 
     * @param memId      會員編號
     * @param intro      自我介紹
     * @param experience 經驗說明
     * @return SitterApplicationVO 申請記錄
     * @throws IllegalStateException 若已有待審核或通過的申請
     */
    @Override
    @Transactional
    public SitterApplicationVO createApplication(Integer memId, String intro, String experience) {
        // 1. 檢查是否已有待審核或通過的申請
        // 註: 暫時不驗證會員是否存在,待會員登入功能完成後再整合
        List<SitterApplicationVO> existing = repository.findByMemId(memId);
        for (SitterApplicationVO app : existing) {
            if (app.getAppStatus() == 0) {
                throw new IllegalStateException("您已有待審核的申請,請等待審核結果");
            }
            if (app.getAppStatus() == 1) {
                throw new IllegalStateException("您已通過審核,無需重複申請");
            }
        }

        // 2. 建立申請
        SitterApplicationVO vo = new SitterApplicationVO();
        vo.setMemId(memId);
        vo.setAppIntro(intro);
        vo.setAppExperience(experience);
        // appStatus, appCreatedAt 由資料庫預設值處理
        return repository.save(vo);
    }

    /**
     * 管理員審核保姆申請
     * 
     * @param appId      申請編號
     * @param status     審核狀態 (0:待審核, 1:通過, 2:拒絕)
     * @param reviewNote 審核備註
     * @return SitterApplicationVO 審核後的申請記錄
     * @throws IllegalArgumentException 若申請不存在
     */
    @Override
    @Transactional
    public SitterApplicationVO reviewApplication(Integer appId, Byte status, String reviewNote) {
        // 1. 查詢申請
        Optional<SitterApplicationVO> optional = repository.findById(appId);
        if (!optional.isPresent()) {
            throw new IllegalArgumentException("申請不存在: " + appId);
        }

        SitterApplicationVO vo = optional.get();

        // 2. 更新審核狀態
        vo.setAppStatus(status);
        vo.setAppReviewNote(reviewNote);
        vo.setAppReviewAt(LocalDateTime.now());

        // 3. 審核通過 (status == 1)
        if (status == 1) {
            Integer memId = vo.getMemId();

            // 3.1 檢查是否已是保姆
            SitterVO existingSitter = sitterService.getSitterByMemId(memId);
            if (existingSitter == null) {
                // 3.2 取得會員資料 (使用低耦合設計)
                Optional<SitterMemberVO> memberOpt = sitterMemberRepository.findById(memId);

                String sitterName;
                String sitterAdd;

                if (memberOpt.isPresent()) {
                    // 會員資料存在,使用實際資料
                    SitterMemberVO member = memberOpt.get();
                    sitterName = member.getMemName();
                    sitterAdd = member.getMemAdd() != null ? member.getMemAdd() : "未設定地址";
                } else {
                    // 會員資料不存在,使用預設值 (待會員功能完成後可移除此邏輯)
                    sitterName = "會員" + memId;
                    sitterAdd = "未設定地址";
                }

                // 3.3 建立保姆資料
                SitterVO newSitter = sitterService.createSitter(memId, sitterName, sitterAdd);
                vo.setSitterId(newSitter.getSitterId());
            } else {
                vo.setSitterId(existingSitter.getSitterId());
            }

            /*
             * =================================================================
             * [PENDING INTEGRATION] 等待會員模組整合
             * =================================================================
             * 目標：當審核通過時，同步更新 Member 資料表的 mem_sitter_status 欄位
             * 
             * 啟用步驟：
             * 1. 注入 MemberRepository (需由會員模組提供)
             * 2. 解除以下程式碼的註解
             * 
             * 預期程式碼：
             * Optional<Member> memberOpt = memberRepository.findById(memId);
             * if (memberOpt.isPresent()) {
             * Member member = memberOpt.get();
             * member.setMemSitterStatus(1); // 1: 啟用保姆權限
             * memberRepository.save(member);
             * }
             * =================================================================
             */
        }

        return repository.save(vo);
    }

    /**
     * 查詢會員的所有申請記錄
     * 
     * @param memId 會員編號
     * @return List<SitterApplicationVO> 該會員的所有申請
     */
    @Override
    public List<SitterApplicationVO> getApplicationsByMember(Integer memId) {
        return repository.findByMemId(memId);
    }

    /**
     * 查詢特定狀態的所有申請
     * 
     * @param status 審核狀態 (0:待審核, 1:通過, 2:拒絕)
     * @return List<SitterApplicationVO> 符合狀態的所有申請
     */
    @Override
    public List<SitterApplicationVO> getApplicationsByStatus(Byte status) {
        return repository.findByAppStatus(status);
    }

    /**
     * 查詢單筆申請記錄
     * 
     * @param appId 申請編號
     * @return SitterApplicationVO 申請記錄,若不存在則返回 null
     */
    @Override
    public SitterApplicationVO getApplicationById(Integer appId) {
        return repository.findById(appId).orElse(null);
    }

    /**
     * 查詢所有申請記錄
     * 
     * @return List<SitterApplicationVO> 所有申請
     */
    @Override
    public List<SitterApplicationVO> getAllApplications() {
        return repository.findAll();
    }

    /**
     * 檢查會員是否已經擁有有效的保姆資格 (即通過審核)
     * 
     * @param memId 會員編號
     * @return true 若已是保姆
     */
    @Override
    public boolean isSitter(Integer memId) {
        if (memId == null) {
            return false;
        }

        List<SitterApplicationVO> applications = repository.findByMemId(memId);
        for (SitterApplicationVO app : applications) {
            if (app.getAppStatus() == 1) { // 1 = 已通過
                return true;
            }
        }
        return false;
    }
}
