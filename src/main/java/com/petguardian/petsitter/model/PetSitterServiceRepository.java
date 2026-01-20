package com.petguardian.petsitter.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 保姆服務資訊 Repository
 * 
 * 提供保姆服務項目與價格的資料存取
 * 複合主鍵: (service_item_id, sitter_id)
 */
@Repository
public interface PetSitterServiceRepository extends JpaRepository<PetSitterServiceVO, PetSitterServiceId> {

    /**
     * 查詢某保姆的所有服務項目
     * 
     * @param sitterId 保姆編號
     * @return List<PetSitterServiceVO> 該保姆的所有服務項目
     */
    List<PetSitterServiceVO> findBySitter_SitterId(Integer sitterId);

    /**
     * 查詢提供某服務的所有保姆
     * 
     * @param serviceItemId 服務項目編號
     * @return List<PetSitterServiceVO> 提供該服務的所有保姆
     */
    List<PetSitterServiceVO> findByServiceItemId(Integer serviceItemId);
}
