package com.petguardian.sitter.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.petguardian.sitter.model.SitterApplicationRepository;
import com.petguardian.sitter.model.SitterApplicationVO;

@Service
public class SitterApplicationServiceImpl implements SitterApplicationService {

    @Autowired
    private SitterApplicationRepository repository;

    @Override
    @Transactional
    public SitterApplicationVO createApplication(Integer memId, String intro, String experience) {
        SitterApplicationVO vo = new SitterApplicationVO();
        vo.setMemId(memId);
        vo.setAppIntro(intro);
        vo.setAppExperience(experience);
        // appStatus, appCreatedAt 由資料庫預設值處理, 但若 JPA save 後回傳物件需有值，可在此先設或依賴 DB refresh
        // 此處依賴 DB default，save 後回傳物件會包含 ID，但時間可能需 refresh，簡單起見先依賴 JPA 回傳
        return repository.save(vo);
    }

    @Override
    @Transactional
    public SitterApplicationVO reviewApplication(Integer appId, Byte status, String reviewNote) {
        Optional<SitterApplicationVO> optional = repository.findById(appId);
        if (optional.isPresent()) {
            SitterApplicationVO vo = optional.get();
            vo.setAppStatus(status);
            vo.setAppReviewNote(reviewNote);
            vo.setAppReviewAt(LocalDateTime.now());
            return repository.save(vo);
        }
        return null; // 或拋出 Exceptions
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
