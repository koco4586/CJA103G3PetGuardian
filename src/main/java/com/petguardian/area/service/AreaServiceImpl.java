package com.petguardian.area.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.petguardian.area.model.AreaRepository;
import com.petguardian.area.model.AreaVO;

/**
 * 地區業務邏輯 Service 實作
 * 
 */
@Service("areaService")
public class AreaServiceImpl implements AreaService {

    @Autowired
    AreaRepository repository;

    @Override
    public List<AreaVO> getAll() {
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
    public AreaVO getOneArea(Integer areaId) {
        Optional<AreaVO> optional = repository.findById(areaId);
        return optional.orElse(null); // public T orElse(T other) : 如果值存在就回傳其值，否則回傳null
    }

    @Override
    public AreaVO getAreaByCityAndDistrict(String cityName, String district) {
        return repository.findByCityNameAndDistrict(cityName, district);
    }

}
