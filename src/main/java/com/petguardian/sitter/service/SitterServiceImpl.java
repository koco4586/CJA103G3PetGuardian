package com.petguardian.sitter.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.petguardian.member.repository.register.MemberRegisterRepository;
import com.petguardian.sitter.model.SitterRepository;
import com.petguardian.sitter.model.SitterVO;

@Service
public class SitterServiceImpl implements SitterService {

    @Autowired
    private SitterRepository repository;

    @Autowired
    private MemberRegisterRepository memberRepository;

    @Override
    @Transactional
    public SitterVO createSitter(Integer memId, String sitterName, String sitterAdd) {
        // 1. 驗證會員是否存在
        if (!memberRepository.existsById(memId)) {
            throw new IllegalArgumentException("會員不存在: " + memId);
        }

        // 2. 檢查是否已是保姆
        SitterVO existing = repository.findByMemId(memId);
        if (existing != null) {
            throw new IllegalStateException("該會員已是保姆,無法重複建立");
        }

        // 3. 建立保姆資料
        SitterVO vo = new SitterVO();
        vo.setMemId(memId);
        vo.setSitterName(sitterName);
        vo.setSitterAdd(sitterAdd);
        // sitterCreatedAt, sitterStatus, serviceTime, sitterRatingCount,
        // sitterStarCount 由資料庫預設值處理
        return repository.save(vo);
    }

    @Override
    public SitterVO getSitterByMemId(Integer memId) {
        return repository.findByMemId(memId);
    }

    @Override
    public SitterVO getSitterById(Integer sitterId) {
        return repository.findById(sitterId).orElse(null);
    }

    @Override
    public List<SitterVO> getAllSitters() {
        return repository.findAll();
    }

    @Override
    public List<SitterVO> getSittersByStatus(Byte status) {
        return repository.findBySitterStatus(status);
    }

    @Override
    @Transactional
    public SitterVO updateSitterStatus(Integer sitterId, Byte status) {
        Optional<SitterVO> optional = repository.findById(sitterId);
        if (!optional.isPresent()) {
            throw new IllegalArgumentException("保姆不存在: " + sitterId);
        }
        SitterVO vo = optional.get();
        vo.setSitterStatus(status);
        return repository.save(vo);
    }

    @Override
    @Transactional
    public SitterVO updateSitterInfo(Integer sitterId, String sitterName, String sitterAdd) {
        Optional<SitterVO> optional = repository.findById(sitterId);
        if (!optional.isPresent()) {
            throw new IllegalArgumentException("保姆不存在: " + sitterId);
        }
        SitterVO vo = optional.get();
        vo.setSitterName(sitterName);
        vo.setSitterAdd(sitterAdd);
        return repository.save(vo);
    }

    @Override
    @Transactional
    public SitterVO updateServiceTime(Integer sitterId, String serviceTime) {
        System.out.println("=== Service 層 Debug ===");
        System.out.println("收到 sitterId: " + sitterId);
        System.out.println("收到 serviceTime: " + serviceTime);

        Optional<SitterVO> optional = repository.findById(sitterId);
        if (!optional.isPresent()) {
            throw new IllegalArgumentException("保姆不存在: " + sitterId);
        }
        SitterVO vo = optional.get();
        System.out.println("更新前 serviceTime: " + vo.getServiceTime());

        vo.setServiceTime(serviceTime);
        SitterVO saved = repository.save(vo);

        System.out.println("更新後 serviceTime: " + saved.getServiceTime());
        System.out.println("====================");

        return saved;
    }
}
