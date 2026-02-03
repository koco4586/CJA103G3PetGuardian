package com.petguardian.petsitter.model;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import org.springframework.data.repository.query.Param;

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

    // 1. 修復 findAllById 的問題：改用自定義的 In 查詢
    @Query("SELECT s FROM PetSitterServiceVO s JOIN FETCH s.serviceItem WHERE s.serviceItemId IN :ids")
    List<PetSitterServiceVO> findByServiceItemIdIn(@Param("ids") java.util.Collection<Integer> ids);

    // 2. 修復 findBySitterIdAndServiceItemId
    // 注意：因為 sitter 是一個物件，所以路徑要寫成 sitter.sitterId
    @Query("SELECT s FROM PetSitterServiceVO s WHERE s.sitter.sitterId = :sitterId AND s.serviceItemId = :serviceItemId")
    Optional<PetSitterServiceVO> findBySitterIdAndServiceItemId(
            @Param("sitterId") Integer sitterId,
            @Param("serviceItemId") Integer serviceItemId);
}
