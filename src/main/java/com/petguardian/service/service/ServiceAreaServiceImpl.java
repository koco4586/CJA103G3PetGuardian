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
 */
@Service
public class ServiceAreaServiceImpl implements ServiceAreaService {

    @Autowired
    private ServiceAreaRepository repository;

    @Autowired
    private SitterRepository sitterRepository;

    @Autowired
    private AreaRepository areaRepository;

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
    public void deleteServiceArea(Integer sitterId, Integer areaId) {
        // 檢查是否存在
        if (!repository.existsBySitter_SitterIdAndArea_AreaId(sitterId, areaId)) {
            throw new IllegalArgumentException("服務地區不存在");
        }

        ServiceAreaId id = new ServiceAreaId(sitterId, areaId);
        repository.deleteById(id);
    }

    @Override
    public List<ServiceAreaVO> getServiceAreasBySitter(Integer sitterId) {
        return repository.findBySitter_SitterId(sitterId);
    }

    @Override
    public List<SitterVO> getSittersByArea(Integer areaId) {
        List<ServiceAreaVO> serviceAreas = repository.findByArea_AreaId(areaId);
        return serviceAreas.stream()
                .map(ServiceAreaVO::getSitter)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isAreaServed(Integer sitterId, Integer areaId) {
        return repository.existsBySitter_SitterIdAndArea_AreaId(sitterId, areaId);
    }
}
