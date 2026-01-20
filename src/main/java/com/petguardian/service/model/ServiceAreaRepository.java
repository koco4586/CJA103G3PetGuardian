package com.petguardian.service.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 保姆服務地區 Repository
 * 
 * 提供保姆服務地區的資料存取
 * 複合主鍵: (sitter_id, area_id)
 */
@Repository
public interface ServiceAreaRepository extends JpaRepository<ServiceAreaVO, ServiceAreaId> {

    /**
     * 查詢某保姆的所有服務地區
     * 
     * @param sitterId 保姆編號
     * @return List<ServiceAreaVO> 該保姆的所有服務地區
     */
    List<ServiceAreaVO> findBySitter_SitterId(Integer sitterId);

    /**
     * 查詢某地區的所有保姆
     * 
     * @param areaId 地區編號
     * @return List<ServiceAreaVO> 該地區的所有保姆
     */
    List<ServiceAreaVO> findByArea_AreaId(Integer areaId);

    /**
     * 檢查某保姆是否服務某地區
     * 
     * @param sitterId 保姆編號
     * @param areaId   地區編號
     * @return boolean true:服務, false:不服務
     */
    boolean existsBySitter_SitterIdAndArea_AreaId(Integer sitterId, Integer areaId);
}
