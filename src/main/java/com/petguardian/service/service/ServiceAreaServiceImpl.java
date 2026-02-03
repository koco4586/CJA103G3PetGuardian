package com.petguardian.service.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.petguardian.area.model.AreaRepository;
import com.petguardian.area.model.AreaVO;
import com.petguardian.service.model.ServiceAreaId;
import com.petguardian.service.model.ServiceAreaRepository;
import com.petguardian.service.model.ServiceAreaVO;
import com.petguardian.sitter.model.SitterRepository;
import com.petguardian.sitter.model.SitterVO;

/**
 * 保姆服務地區業務邏輯實作
 * 
 * 提供保姆設定服務地區、查詢服務地區等功能的實作
 */
@Service("serviceAreaService")
public class ServiceAreaServiceImpl implements ServiceAreaService {

    @Autowired
    private ServiceAreaRepository repository;

    @Autowired
    private SitterRepository sitterRepository;

    @Autowired
    private AreaRepository areaRepository;

    /**
     * 保姆新增服務地區
     * 
     * @param sitterId 保姆編號
     * @param areaId   地區編號
     * @return ServiceAreaVO 新增的服務地區
     * @throws IllegalArgumentException 若保姆或地區不存在
     * @throws IllegalStateException    若服務地區已存在
     */
    @Override
    @Transactional
    public ServiceAreaVO addServiceArea(Integer sitterId, Integer areaId) {
        // 1. 驗證保姆是否存在
        SitterVO sitter = sitterRepository.findById(sitterId)
                .orElseThrow(() -> new IllegalArgumentException("保姆不存在: " + sitterId));

        // 2. 驗證地區是否存在
        AreaVO area = areaRepository.findById(areaId)
                .orElseThrow(() -> new IllegalArgumentException("地區不存在: " + areaId));

        // 3. 檢查是否已存在
        if (repository.existsBySitter_SitterIdAndArea_AreaId(sitterId, areaId)) {
            throw new IllegalStateException("此服務地區已存在");
        }

        // 4. 建立新服務地區
        ServiceAreaVO vo = new ServiceAreaVO();
        vo.setSitter(sitter);
        vo.setArea(area);

        return repository.save(vo);
    }

    @Override
    @Transactional
    public ServiceAreaVO addServiceAreaForMember(Integer memId, Integer areaId) {
        SitterVO sitter = sitterRepository.findByMemId(memId);
        if (sitter == null) {
            throw new IllegalArgumentException("會員尚未成為保姆");
        }
        return addServiceArea(sitter.getSitterId(), areaId);
    }

    /**
     * 保姆刪除服務地區
     * 
     * @param sitterId 保姆編號
     * @param areaId   地區編號
     * @throws IllegalArgumentException 若服務地區不存在
     */
    @Override
    @Transactional
    public void deleteServiceArea(Integer sitterId, Integer areaId) {
        // 檢查是否存在
        if (!repository.existsBySitter_SitterIdAndArea_AreaId(sitterId, areaId)) {
            throw new IllegalArgumentException("服務地區不存在");
        }

        ServiceAreaId id = new ServiceAreaId(sitterId, areaId);
        repository.deleteById(id);
    }

    @Override
    @Transactional
    public void deleteServiceAreaForMember(Integer memId, Integer areaId) {
        SitterVO sitter = sitterRepository.findByMemId(memId);
        if (sitter == null) {
            throw new IllegalArgumentException("會員尚未成為保姆");
        }
        deleteServiceArea(sitter.getSitterId(), areaId);
    }

    /**
     * 查詢保姆的所有服務地區
     * 
     * @param sitterId 保姆編號
     * @return List<ServiceAreaVO> 該保姆的所有服務地區
     */
    @Override
    @Transactional(readOnly = true)
    public List<ServiceAreaVO> getServiceAreasBySitter(Integer sitterId) {
        // 使用優化版查詢，一次載入 ServiceArea 和 Area，避免 N+1 問題
        return repository.findBySitterIdWithArea(sitterId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceAreaVO> getServiceAreasByMember(Integer memId) {
        SitterVO sitter = sitterRepository.findByMemId(memId);
        if (sitter == null) {
            throw new IllegalArgumentException("會員尚未成為保姆");
        }
        return getServiceAreasBySitter(sitter.getSitterId());
    }

    /**
     * 查詢某地區的所有保姆
     * 
     * @param areaId 地區編號
     * @return List<SitterVO> 該地區的所有保姆
     */
    @Override
    public List<SitterVO> getSittersByArea(Integer areaId) {
        List<ServiceAreaVO> serviceAreas = repository.findByArea_AreaId(areaId);
        return serviceAreas.stream()
                .map(ServiceAreaVO::getSitter)
                .collect(Collectors.toList());
    }

    /**
     * 檢查保姆是否服務某地區
     * 
     * @param sitterId 保姆編號
     * @param areaId   地區編號
     * @return boolean true:服務, false:不服務
     */
    @Override
    public boolean isAreaServed(Integer sitterId, Integer areaId) {
        return repository.existsBySitter_SitterIdAndArea_AreaId(sitterId, areaId);
    }
}
