package com.petguardian.petsitter.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.petguardian.petsitter.model.PetSitterServiceId;
import com.petguardian.petsitter.model.PetSitterServiceRepository;
import com.petguardian.petsitter.model.PetSitterServiceVO;
import com.petguardian.sitter.model.SitterRepository;
import com.petguardian.sitter.model.SitterVO;

/**
 * 保姆服務資訊業務邏輯實作
 */
@Service
public class PetSitterServiceImpl implements PetSitterService {

    @Autowired
    private PetSitterServiceRepository repository;

    @Autowired
    private SitterRepository sitterRepository;

    @Override
    @Transactional
    public PetSitterServiceVO setServicePrice(Integer sitterId, Integer serviceItemId, Integer price) {
        // 1. 驗證保姆是否存在
        Optional<SitterVO> sitterOpt = sitterRepository.findById(sitterId);
        if (!sitterOpt.isPresent()) {
            throw new IllegalArgumentException("保姆不存在: " + sitterId);
        }

        // 2. 驗證價格範圍 (400-1000)
        if (price == null || price < 400 || price > 1000) {
            throw new IllegalArgumentException("價格必須介於 400 到 1000 之間");
        }

        // 3. 查詢是否已存在
        PetSitterServiceId id = new PetSitterServiceId(serviceItemId, sitterId);
        Optional<PetSitterServiceVO> existingOpt = repository.findById(id);

        PetSitterServiceVO vo;
        if (existingOpt.isPresent()) {
            // 已存在,更新價格
            vo = existingOpt.get();
            vo.setDefaultPrice(price);
        } else {
            // 不存在,新增
            vo = new PetSitterServiceVO();
            vo.setServiceItemId(serviceItemId);
            vo.setSitter(sitterOpt.get());
            vo.setDefaultPrice(price);
        }

        // 4. 儲存並返回
        return repository.save(vo);
    }

    @Override
    public List<PetSitterServiceVO> getServicesBySitter(Integer sitterId) {
        return repository.findBySitter_SitterId(sitterId);
    }

    @Override
    public PetSitterServiceVO getService(Integer sitterId, Integer serviceItemId) {
        PetSitterServiceId id = new PetSitterServiceId(serviceItemId, sitterId);
        return repository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public void deleteService(Integer sitterId, Integer serviceItemId) {
        PetSitterServiceId id = new PetSitterServiceId(serviceItemId, sitterId);
        Optional<PetSitterServiceVO> existingOpt = repository.findById(id);

        if (!existingOpt.isPresent()) {
            throw new IllegalArgumentException("服務不存在");
        }

        repository.deleteById(id);
    }

    @Override
    public List<PetSitterServiceVO> getSittersByService(Integer serviceItemId) {
        return repository.findByServiceItemId(serviceItemId);
    }
}
