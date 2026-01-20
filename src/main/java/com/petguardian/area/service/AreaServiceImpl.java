package com.petguardian.area.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.petguardian.area.model.AreaRepository;
import com.petguardian.area.model.AreaVO;

/**
 * 地區業務邏輯實作
 */
@Service
public class AreaServiceImpl implements AreaService {

    @Autowired
    private AreaRepository repository;

    @Override
    public List<AreaVO> getAllAreas() {
        return repository.findAll();
    }

    @Override
    public List<String> getAllCities() {
        return repository.findAllCities();
    }

    @Override
    public List<AreaVO> getDistrictsByCity(String cityName) {
        return repository.findByCityName(cityName);
    }

    @Override
    public AreaVO getAreaById(Integer areaId) {
        return repository.findById(areaId).orElse(null);
    }

    @Override
    public AreaVO getAreaByCityAndDistrict(String cityName, String district) {
        return repository.findByCityNameAndDistrict(cityName, district);
    }
}
