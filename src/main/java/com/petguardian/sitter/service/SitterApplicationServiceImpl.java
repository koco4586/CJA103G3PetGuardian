package com.petguardian.sitter.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
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
@Transactional(readOnly = true)
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

                    // 檢查會員地址是否有效
                    String memberAddress = member.getMemAdd();
                    if (memberAddress != null && memberAddress.trim().length() >= 5) {
                        sitterAdd = memberAddress;
                    } else {
                        // 會員地址為空或太短，使用預設值（符合驗證規則：至少5字元）
                        sitterAdd = "台北市大安區";
                    }
                } else {
                    // 會員資料不存在,使用預設值 (待會員功能完成後可移除此邏輯)
                    sitterName = "會員" + memId;
                    sitterAdd = "台北市大安區";
                }

                // 3.3 建立保姆資料
                SitterVO newSitter = sitterService.createSitter(memId, sitterName, sitterAdd);
                vo.setSitterId(newSitter.getSitterId());
            } else {
                vo.setSitterId(existingSitter.getSitterId());
            }

            /*
             * =================================================================
             * [INTEGRATION COMPLETED] 會員模組整合完成
             * =================================================================
             * 目標：當審核通過時，同步更新 Member 資料表的 mem_sitter_status 欄位
             */
            sitterMemberRepository.updateMemSitterStatus(memId, 1); // 1: 啟用保姆權限
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

    /**
     * [Refactor] 檢查會員申請狀態，回傳對應的提示訊息
     */
    @Override
    public String checkApplicationStatus(Integer memId) {
        List<SitterApplicationVO> existingApps = repository.findByMemId(memId);
        for (SitterApplicationVO app : existingApps) {
            if (app.getAppStatus() == 0) {
                return "您已有待審核的申請，請耐心等候結果";
            }
            if (app.getAppStatus() == 1) {
                return "您已通過審核成為保姆，無需重複申請";
            }
        }
        return null;
    }

    /**
     * [Refactor] 取得申請頁面所需的初始資料
     */
    @Override
    public Map<String, Object> getApplyFormInitData(Integer memId, String avatarUrl) {
        Map<String, Object> data = new HashMap<>();

        String memName = "會員姓名";
        String memPhone = "未設定";
        SitterMemberVO currentMember = null;

        if (memId != null) {
            Optional<SitterMemberVO> memberOpt = sitterMemberRepository.findById(memId);
            if (memberOpt.isPresent()) {
                currentMember = memberOpt.get();
                memName = currentMember.getMemName();
                memPhone = currentMember.getMemTel();
            }
        }

        data.put("memName", memName != null ? memName : "會員姓名");
        data.put("memPhone", memPhone != null ? memPhone : "未設定");
        data.put("avatarUrl", avatarUrl);
        data.put("currentMember", currentMember); // 新增：傳遞完整會員物件供 sidebar 使用
        data.put("memberRole", "一般會員");
        data.put("defaultCity", "台北市");
        data.put("defaultDistrict", "大安區");

        return data;
    }
}
