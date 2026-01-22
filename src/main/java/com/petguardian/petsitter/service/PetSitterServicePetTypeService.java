package com.petguardian.petsitter.service;

import java.util.List;

import com.petguardian.petsitter.model.PetSitterServicePetTypeVO;

/**
 * 保姆服務寵物對象業務邏輯介面
 * 
 * 提供服務項目與寵物種類/體型配置的管理功能
 */
public interface PetSitterServicePetTypeService {

    /**
     * 新增服務寵物對象配置
     * 
     * @param sitterId      保姆編號
     * @param serviceItemId 服務項目編號 (1:散步, 2:餵食, 3:洗澡)
     * @param typeId        寵物種類編號 (1:貓, 2:狗)
     * @param sizeId        寵物體型編號 (1:小型, 2:中型, 3:大型)
     * @return PetSitterServicePetTypeVO 新增的配置
     * @throws IllegalArgumentException 若保姆不存在
     * @throws IllegalStateException    若配置已存在
     */
    PetSitterServicePetTypeVO addServicePetType(Integer sitterId, Integer serviceItemId, Integer typeId,
            Integer sizeId);

    /**
     * 新增保姆服務寵物對象配置 (透過會員 ID)
     * 
     * @param memId         會員編號
     * @param serviceItemId 服務項目編號 (1:散步, 2:餵食, 3:洗澡)
     * @param typeId        寵物種類編號 (1:貓, 2:狗)
     * @param sizeId        寵物體型編號 (1:小型, 2:中型, 3:大型)
     * @return PetSitterServicePetTypeVO 新增的配置
     */
    PetSitterServicePetTypeVO addServicePetTypeForMember(Integer memId, Integer serviceItemId, Integer typeId,
            Integer sizeId);

    /**
     * 查詢某保姆的所有服務寵物對象配置
     * 
     * @param sitterId 保姆編號
     * @return List<PetSitterServicePetTypeVO> 該保姆的所有配置
     */
    List<PetSitterServicePetTypeVO> getServicePetTypesBySitter(Integer sitterId);

    /**
     * 查詢某保姆的所有服務寵物對象配置 (透過會員 ID)
     * 
     * @param memId 會員編號
     * @return List<PetSitterServicePetTypeVO> 該保姆的所有配置
     */
    List<PetSitterServicePetTypeVO> getServicePetTypesByMember(Integer memId);

    /**
     * 查詢某保姆某服務項目的所有寵物對象配置
     * 
     * @param sitterId      保姆編號
     * @param serviceItemId 服務項目編號
     * @return List<PetSitterServicePetTypeVO> 該保姆該服務的所有配置
     */
    List<PetSitterServicePetTypeVO> getServicePetTypesBySitterAndService(Integer sitterId, Integer serviceItemId);

    /**
     * 查詢某服務項目可服務的所有寵物對象 (跨所有保姆)
     * 
     * @param serviceItemId 服務項目編號
     * @return List<PetSitterServicePetTypeVO> 該服務可服務的寵物對象列表
     */
    List<PetSitterServicePetTypeVO> getServicePetTypesByService(Integer serviceItemId);

    /**
     * 查詢某寵物種類與體型可使用的服務 (跨所有保姆)
     * 
     * @param typeId 寵物種類編號
     * @param sizeId 寵物體型編號
     * @return List<PetSitterServicePetTypeVO> 可使用的服務列表
     */
    List<PetSitterServicePetTypeVO> getServicesByPetTypeAndSize(Integer typeId, Integer sizeId);

    /**
     * 刪除服務寵物對象配置
     * 
     * @param servicePetId 服務寵物對象編號
     * @throws IllegalArgumentException 若配置不存在
     */
    void deleteServicePetType(Integer servicePetId);

    /**
     * 刪除服務寵物對象配置 (透過會員 ID 進行權限檢查)
     * 
     * @param memId        會員編號
     * @param servicePetId 服務寵物對象編號
     */
    void deleteServicePetTypeForMember(Integer memId, Integer servicePetId);

    /**
     * 檢查某保姆的某服務是否支援特定寵物種類與體型
     * 
     * @param sitterId      保姆編號
     * @param serviceItemId 服務項目編號
     * @param typeId        寵物種類編號
     * @param sizeId        寵物體型編號
     * @return boolean true:支援, false:不支援
     */
    boolean isServiceSupported(Integer sitterId, Integer serviceItemId, Integer typeId, Integer sizeId);

    /**
     * 查詢所有服務寵物對象配置
     * 
     * @return List<PetSitterServicePetTypeVO> 所有配置列表
     */
    List<PetSitterServicePetTypeVO> getAllServicePetTypes();
}
