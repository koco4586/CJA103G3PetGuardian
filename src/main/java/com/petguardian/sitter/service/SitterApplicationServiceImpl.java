package com.petguardian.sitter.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.petguardian.sitter.model.SitterApplicationRepository;
import com.petguardian.sitter.model.SitterApplicationVO;
import com.petguardian.sitter.model.SitterMemberRepository;
import com.petguardian.sitter.model.SitterMemberVO;
import com.petguardian.sitter.model.SitterVO;

@Service
public class SitterApplicationServiceImpl implements SitterApplicationService {

    @Autowired
    private SitterApplicationRepository repository;

    @Autowired
    private SitterMemberRepository sitterMemberRepository;

    @Autowired
    private SitterService sitterService;

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
                sitterService.createSitter(memId, sitterName, sitterAdd);
            }

            // TODO: 會員功能完成後,取消以下註解以啟用會員保姆狀態更新
            // 3.4 更新會員的保姆狀態為啟用
            // Optional<Member> memberOpt = memberRepository.findById(memId);
            // if (memberOpt.isPresent()) {
            // Member member = memberOpt.get();
            // member.setMemSitterStatus(1); // 啟用保姆狀態
            // memberRepository.save(member);
            // }
        }

        return repository.save(vo);
    }

    @Override
    public List<SitterApplicationVO> getApplicationsByMember(Integer memId) {
        return repository.findByMemId(memId);
    }

    @Override
    public List<SitterApplicationVO> getApplicationsByStatus(Byte status) {
        return repository.findByAppStatus(status);
    }

    @Override
    public SitterApplicationVO getApplicationById(Integer appId) {
        return repository.findById(appId).orElse(null);
    }

    @Override
    public List<SitterApplicationVO> getAllApplications() {
        return repository.findAll();
    }
}
