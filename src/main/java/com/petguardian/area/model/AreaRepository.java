package com.petguardian.area.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * 地區 Repository
 * 
 * 提供地區資料的查詢方法
 */
@Repository
public interface AreaRepository extends JpaRepository<AreaVO, Integer> {

    /**
     * 查詢所有縣市 (去重)
     * 
     * @return List<String> 所有縣市列表
     */
    @Query("SELECT DISTINCT a.cityName FROM AreaVO a ORDER BY a.cityName")
    List<String> findAllCities();

    /**
     * 根據縣市查詢所有行政區
     * 
     * @param cityName 縣市名稱
     * @return List<AreaVO> 該縣市的所有行政區
     */
    List<AreaVO> findByCityName(String cityName);

    /**
     * 根據縣市與行政區查詢地區
     * 
     * @param cityName 縣市名稱
     * @param district 行政區名稱
     * @return AreaVO 地區資料,若不存在則返回 null
     */
    AreaVO findByCityNameAndDistrict(String cityName, String district);
}
