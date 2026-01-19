package com.petguardian.sitter.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.petguardian.sitter.model.SitterRepository;
import com.petguardian.sitter.model.SitterVO;

@Service
public class SitterServiceImpl implements SitterService {

    @Autowired
    private SitterRepository repository;

    @Override
    @Transactional
    public SitterVO createSitter(Integer memId, String sitterName, String sitterAdd) {
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
        if (optional.isPresent()) {
            SitterVO vo = optional.get();
            vo.setSitterStatus(status);
            return repository.save(vo);
        }
        return null;
    }

    @Override
    @Transactional
    public SitterVO updateSitterInfo(Integer sitterId, String sitterName, String sitterAdd) {
        Optional<SitterVO> optional = repository.findById(sitterId);
        if (optional.isPresent()) {
            SitterVO vo = optional.get();
            vo.setSitterName(sitterName);
            vo.setSitterAdd(sitterAdd);
            return repository.save(vo);
        }
        return null;
    }
}
