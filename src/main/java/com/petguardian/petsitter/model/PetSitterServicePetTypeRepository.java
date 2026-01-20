package com.petguardian.petsitter.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 保姆服務寵物對象 Repository
 * 
 * 提供服務項目與寵物種類/體型配置的資料存取
 * 用於定義每個服務項目可以服務的寵物對象
 */
@Repository
public interface PetSitterServicePetTypeRepository extends JpaRepository<PetSitterServicePetTypeVO, Integer> {

    /**
     * 查詢某保姆的所有服務寵物對象配置
     * 
     * @param sitterId 保姆編號
     * @return List<PetSitterServicePetTypeVO> 該保姆的所有配置
     */
    List<PetSitterServicePetTypeVO> findBySitterId(Integer sitterId);

    /**
     * 查詢某保姆某服務項目的所有寵物對象配置
     * 
     * @param sitterId      保姆編號
     * @param serviceItemId 服務項目編號
     * @return List<PetSitterServicePetTypeVO> 該保姆該服務的所有配置
     */
    List<PetSitterServicePetTypeVO> findBySitterIdAndServiceItemId(Integer sitterId, Integer serviceItemId);

    /**
     * 查詢某服務項目可服務的所有寵物對象 (跨所有保姆)
     * 
     * @param serviceItemId 服務項目編號
     * @return List<PetSitterServicePetTypeVO> 該服務可服務的寵物對象列表
     */
    List<PetSitterServicePetTypeVO> findByServiceItemId(Integer serviceItemId);

    /**
     * 查詢某寵物種類可使用的所有服務 (跨所有保姆)
     * 
     * @param typeId 寵物種類編號 (1:貓, 2:狗)
     * @return List<PetSitterServicePetTypeVO> 該寵物種類可使用的服務列表
     */
    List<PetSitterServicePetTypeVO> findByTypeId(Integer typeId);

    /**
     * 查詢某寵物體型可使用的所有服務 (跨所有保姆)
     * 
     * @param sizeId 寵物體型編號 (1:小型, 2:中型, 3:大型)
     * @return List<PetSitterServicePetTypeVO> 該寵物體型可使用的服務列表
     */
    List<PetSitterServicePetTypeVO> findBySizeId(Integer sizeId);

    /**
     * 查詢特定組合的服務配置
     * 用於檢查某保姆的某服務是否支援特定寵物種類與體型
     * 
     * @param sitterId      保姆編號
     * @param serviceItemId 服務項目編號
     * @param typeId        寵物種類編號
     * @param sizeId        寵物體型編號
     * @return List<PetSitterServicePetTypeVO> 符合條件的配置列表
     */
    List<PetSitterServicePetTypeVO> findBySitterIdAndServiceItemIdAndTypeIdAndSizeId(
            Integer sitterId, Integer serviceItemId, Integer typeId, Integer sizeId);
}
