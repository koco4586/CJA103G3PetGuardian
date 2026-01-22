package com.petguardian.area.service;

import java.util.List;

import com.petguardian.area.model.AreaVO;

/**
 * 地區業務邏輯介面
 * 
 * 提供地區查詢功能
 */
public interface AreaService {

    /**
     * 查詢所有地區
     * 
     * @return List<AreaVO> 所有地區列表
     */
    List<AreaVO> getAll();

    /**
     * 查詢所有縣市
     * 
     * @return List<String> 所有縣市列表 (去重)
     */
    List<String> getAllCities();

    /**
     * 根據縣市查詢行政區
     * 
     * @param cityName 縣市名稱
     * @return List<AreaVO> 該縣市的所有行政區
     */
    List<AreaVO> getDistrictsByCity(String cityName);

    /**
     * 根據 ID 查詢地區
     * 
     * @param areaId 地區編號
     * @return AreaVO 地區資料,若不存在則返回 null
     */
    AreaVO getOneArea(Integer areaId);

    /**
     * 根據縣市與行政區查詢地區
     * 
     * @param cityName 縣市名稱
     * @param district 行政區名稱
     * @return AreaVO 地區資料,若不存在則返回 null
     */
    AreaVO getAreaByCityAndDistrict(String cityName, String district);
}
