package com.petguardian.petsitter.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.petguardian.petsitter.model.PetSitterServicePetTypeRepository;
import com.petguardian.petsitter.model.PetSitterServicePetTypeVO;
import com.petguardian.sitter.model.SitterRepository;

import com.petguardian.petsitter.model.PetType;
import com.petguardian.petsitter.model.PetSize;

/**
 * 保姆服務寵物對象業務邏輯實作
 * 
 * 提供保姆設定服務對象(寵物種類+體型)、查詢服務配置等功能的實作
 */
@Service("petSitterServicePetTypeService")
public class PetSitterServicePetTypeServiceImpl implements PetSitterServicePetTypeService {

    @Autowired
    private PetSitterServicePetTypeRepository repository;

    @Autowired
    private SitterRepository sitterRepository; // Added SitterRepository

    /**
     * 新增保姆服務對象(寵物種類+體型)
     * 
     * @param sitterId      保姆編號
     * @param serviceItemId 服務項目編號
     * @param typeId        寵物種類編號
     * @param sizeId        寵物體型編號
     * @return PetSitterServicePetTypeVO 新增的服務配置
     * @throws IllegalArgumentException 若保姆不存在
     * @throws IllegalStateException    若服務配置已存在
     */
    @Override
    @Transactional
    public PetSitterServicePetTypeVO addServicePetType(Integer sitterId, Integer serviceItemId, Integer typeId,
            Integer sizeId) {
        // 1. 驗證保姆是否存在
        if (sitterId == null || !sitterRepository.existsById(sitterId)) {
            throw new IllegalArgumentException("保姆不存在: " + sitterId);
        }

        // 1.5 驗證寵物種類與體型是否有效
        if (PetType.fromId(typeId) == null) {
            throw new IllegalArgumentException("無效的寵物種類 : " + typeId);
        }
        if (PetSize.fromId(sizeId) == null) {
            throw new IllegalArgumentException("無效的寵物體型 : " + sizeId);
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
    @Transactional
    public PetSitterServicePetTypeVO addServicePetTypeForMember(Integer memId, Integer serviceItemId, Integer typeId,
            Integer sizeId) {
        com.petguardian.sitter.model.SitterVO sitter = sitterRepository.findByMemId(memId);
        if (sitter == null) {
            throw new IllegalArgumentException("會員尚未成為保姆");
        }
        return addServicePetType(sitter.getSitterId(), serviceItemId, typeId, sizeId);
    }

    /**
     * 查詢保姆的所有服務對象配置
     * 
     * @param sitterId 保姆編號
     * @return List<PetSitterServicePetTypeVO> 該保姆的所有服務對象配置
     */
    @Override
    public List<PetSitterServicePetTypeVO> getServicePetTypesBySitter(Integer sitterId) {
        return repository.findBySitterId(sitterId);
    }

    @Override
    public List<PetSitterServicePetTypeVO> getServicePetTypesByMember(Integer memId) {
        com.petguardian.sitter.model.SitterVO sitter = sitterRepository.findByMemId(memId);
        if (sitter == null) {
            throw new IllegalArgumentException("會員尚未成為保姆");
        }
        return getServicePetTypesBySitter(sitter.getSitterId());
    }

    /**
     * 查詢保姆在特定服務項目的所有服務對象配置
     * 
     * @param sitterId      保姆編號
     * @param serviceItemId 服務項目編號
     * @return List<PetSitterServicePetTypeVO> 該保姆在該服務項目的所有配置
     */
    @Override
    public List<PetSitterServicePetTypeVO> getServicePetTypesBySitterAndService(Integer sitterId,
            Integer serviceItemId) {
        return repository.findBySitterIdAndServiceItemId(sitterId, serviceItemId);
    }

    /**
     * 查詢提供特定服務項目的所有配置
     * 
     * @param serviceItemId 服務項目編號
     * @return List<PetSitterServicePetTypeVO> 提供該服務的所有配置
     */
    @Override
    public List<PetSitterServicePetTypeVO> getServicePetTypesByService(Integer serviceItemId) {
        return repository.findByServiceItemId(serviceItemId);
    }

    /**
     * 查詢符合寵物種類與體型的所有服務配置
     * 
     * @param typeId 寵物種類編號
     * @param sizeId 寵物體型編號
     * @return List<PetSitterServicePetTypeVO> 符合條件的所有配置
     */
    @Override
    public List<PetSitterServicePetTypeVO> getServicesByPetTypeAndSize(Integer typeId, Integer sizeId) {
        // 查詢符合寵物種類與體型的所有服務配置
        List<PetSitterServicePetTypeVO> byType = repository.findByTypeId(typeId);
        List<PetSitterServicePetTypeVO> bySize = repository.findBySizeId(sizeId);

        // 取交集:同時符合種類與體型的配置
        byType.retainAll(bySize);
        return byType;
    }

    /**
     * 刪除服務對象配置
     * 
     * @param servicePetId 服務對象配置編號
     * @throws IllegalArgumentException 若配置不存在
     */
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
    @Transactional
    public void deleteServicePetTypeForMember(Integer memId, Integer servicePetId) {
        // 1. 檢查會員是否為保姆
        com.petguardian.sitter.model.SitterVO sitter = sitterRepository.findByMemId(memId);
        if (sitter == null) {
            throw new IllegalArgumentException("會員尚未成為保姆");
        }

        // 2. 檢查該服務配置是否屬於該保姆
        Optional<PetSitterServicePetTypeVO> existingOpt = repository.findById(servicePetId);
        if (!existingOpt.isPresent()) {
            throw new IllegalArgumentException("服務配置不存在: " + servicePetId);
        }
        if (!existingOpt.get().getSitterId().equals(sitter.getSitterId())) {
            throw new IllegalArgumentException("無權刪除此配置");
        }

        deleteServicePetType(servicePetId);
    }

    /**
     * 檢查保姆是否支援特定服務配置
     * 
     * @param sitterId      保姆編號
     * @param serviceItemId 服務項目編號
     * @param typeId        寵物種類編號
     * @param sizeId        寵物體型編號
     * @return boolean true:支援, false:不支援
     */
    @Override
    public boolean isServiceSupported(Integer sitterId, Integer serviceItemId, Integer typeId, Integer sizeId) {
        List<PetSitterServicePetTypeVO> result = repository.findBySitterIdAndServiceItemIdAndTypeIdAndSizeId(sitterId,
                serviceItemId, typeId, sizeId);
        return !result.isEmpty();
    }

    /**
     * 查詢所有服務對象配置
     * 
     * @return List<PetSitterServicePetTypeVO> 所有配置
     */
    @Override
    public List<PetSitterServicePetTypeVO> getAllServicePetTypes() {
        return repository.findAll();
    }
}
