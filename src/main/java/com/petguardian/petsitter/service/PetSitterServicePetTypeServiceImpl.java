package com.petguardian.petsitter.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.petguardian.petsitter.model.PetSitterServicePetTypeRepository;
import com.petguardian.petsitter.model.PetSitterServicePetTypeVO;
import com.petguardian.sitter.model.SitterRepository;

/**
 * 保姆服務寵物對象業務邏輯實作
 */
@Service
public class PetSitterServicePetTypeServiceImpl implements PetSitterServicePetTypeService {

    @Autowired
    private PetSitterServicePetTypeRepository repository;

    @Autowired
    private SitterRepository sitterRepository; // Added SitterRepository

    @Override
    @Transactional
    public PetSitterServicePetTypeVO addServicePetType(Integer sitterId, Integer serviceItemId, Integer typeId,
            Integer sizeId) {
        // 1. 驗證保姆是否存在
        if (!sitterRepository.existsById(sitterId)) {
            throw new IllegalArgumentException("保姆不存在: " + sitterId);
        }

        // 2. 檢查是否已存在相同配置
        List<PetSitterServicePetTypeVO> existing = repository.findBySitterIdAndServiceItemIdAndTypeIdAndSizeId(sitterId,
                serviceItemId, typeId, sizeId);

        if (!existing.isEmpty()) {
            throw new IllegalStateException("此服務配置已存在");
        }

        // 3. 建立新配置
        PetSitterServicePetTypeVO vo = new PetSitterServicePetTypeVO();
        vo.setSitterId(sitterId); // Set sitterId
        vo.setServiceItemId(serviceItemId);
        vo.setTypeId(typeId);
        vo.setSizeId(sizeId);

        // 4. 儲存並返回
        return repository.save(vo);
    }

    @Override
    public List<PetSitterServicePetTypeVO> getServicePetTypesBySitter(Integer sitterId) {
        return repository.findBySitterId(sitterId);
    }

    @Override
    public List<PetSitterServicePetTypeVO> getServicePetTypesBySitterAndService(Integer sitterId,
            Integer serviceItemId) {
        return repository.findBySitterIdAndServiceItemId(sitterId, serviceItemId);
    }

    @Override
    public List<PetSitterServicePetTypeVO> getServicePetTypesByService(Integer serviceItemId) {
        return repository.findByServiceItemId(serviceItemId);
    }

    @Override
    public List<PetSitterServicePetTypeVO> getServicesByPetTypeAndSize(Integer typeId, Integer sizeId) {
        // 查詢符合寵物種類與體型的所有服務配置
        List<PetSitterServicePetTypeVO> byType = repository.findByTypeId(typeId);
        List<PetSitterServicePetTypeVO> bySize = repository.findBySizeId(sizeId);

        // 取交集:同時符合種類與體型的配置
        byType.retainAll(bySize);
        return byType;
    }

    @Override
    @Transactional
    public void deleteServicePetType(Integer servicePetId) {
        Optional<PetSitterServicePetTypeVO> existingOpt = repository.findById(servicePetId);

        if (!existingOpt.isPresent()) {
            throw new IllegalArgumentException("服務配置不存在: " + servicePetId);
        }

        repository.deleteById(servicePetId);
    }

    @Override
    public boolean isServiceSupported(Integer sitterId, Integer serviceItemId, Integer typeId, Integer sizeId) {
        List<PetSitterServicePetTypeVO> result = repository.findBySitterIdAndServiceItemIdAndTypeIdAndSizeId(sitterId,
                serviceItemId, typeId, sizeId);
        return !result.isEmpty();
    }

    @Override
    public List<PetSitterServicePetTypeVO> getAllServicePetTypes() {
        return repository.findAll();
    }
}
