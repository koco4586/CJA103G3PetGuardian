package com.petguardian.petsitter.service;

import java.util.List;

import com.petguardian.petsitter.model.PetSitterServiceVO;

/**
 * 保姆服務資訊業務邏輯介面
 * 
 * 提供保姆設定服務價格、查詢服務項目等功能
 */
public interface PetSitterService {

    /**
     * 保姆設定服務項目價格
     * 
     * @param sitterId      保姆編號
     * @param serviceItemId 服務項目編號 (1:散步, 2:餵食, 3:洗澡)
     * @param price         價格 (400-1000)
     * @return PetSitterServiceVO 設定後的服務資訊
     * @throws IllegalArgumentException 若保姆不存在或價格不符合規範
     */
    PetSitterServiceVO setServicePrice(Integer sitterId, Integer serviceItemId, Integer price);

    /**
     * 查詢保姆的所有服務項目
     * 
     * @param sitterId 保姆編號
     * @return List<PetSitterServiceVO> 該保姆的所有服務項目
     */
    List<PetSitterServiceVO> getServicesBySitter(Integer sitterId);

    /**
     * 查詢單筆服務資訊
     * 
     * @param sitterId      保姆編號
     * @param serviceItemId 服務項目編號
     * @return PetSitterServiceVO 服務資訊,若不存在則返回 null
     */
    PetSitterServiceVO getService(Integer sitterId, Integer serviceItemId);

    /**
     * 刪除保姆的服務項目
     * 
     * @param sitterId      保姆編號
     * @param serviceItemId 服務項目編號
     * @throws IllegalArgumentException 若服務不存在
     */
    void deleteService(Integer sitterId, Integer serviceItemId);

    /**
     * 查詢提供某服務的所有保姆
     * 
     * @param serviceItemId 服務項目編號
     * @return List<PetSitterServiceVO> 提供該服務的所有保姆
     */
    List<PetSitterServiceVO> getSittersByService(Integer serviceItemId);
}
