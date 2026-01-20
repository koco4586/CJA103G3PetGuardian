package com.petguardian.service.service;

import java.util.List;

import com.petguardian.service.model.ServiceAreaVO;
import com.petguardian.sitter.model.SitterVO;

/**
 * 保姆服務地區業務邏輯介面
 * 
 * 提供保姆設定服務地區、查詢服務地區等功能
 */
public interface ServiceAreaService {

    /**
     * 保姆新增服務地區
     * 
     * @param sitterId 保姆編號
     * @param areaId   地區編號
     * @return ServiceAreaVO 新增的服務地區
     * @throws IllegalArgumentException 若保姆或地區不存在
     * @throws IllegalStateException    若服務地區已存在
     */
    ServiceAreaVO addServiceArea(Integer sitterId, Integer areaId);

    /**
     * 保姆刪除服務地區
     * 
     * @param sitterId 保姆編號
     * @param areaId   地區編號
     * @throws IllegalArgumentException 若服務地區不存在
     */
    void deleteServiceArea(Integer sitterId, Integer areaId);

    /**
     * 查詢保姆的所有服務地區
     * 
     * @param sitterId 保姆編號
     * @return List<ServiceAreaVO> 該保姆的所有服務地區
     */
    List<ServiceAreaVO> getServiceAreasBySitter(Integer sitterId);

    /**
     * 查詢某地區的所有保姆
     * 
     * @param areaId 地區編號
     * @return List<SitterVO> 該地區的所有保姆
     */
    List<SitterVO> getSittersByArea(Integer areaId);

    /**
     * 檢查保姆是否服務某地區
     * 
     * @param sitterId 保姆編號
     * @param areaId   地區編號
     * @return boolean true:服務, false:不服務
     */
    boolean isAreaServed(Integer sitterId, Integer areaId);
}
