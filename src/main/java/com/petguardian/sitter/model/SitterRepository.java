package com.petguardian.sitter.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * 保姆 Repository
 * 
 * 提供保姆基本資料的存取與查詢
 * 包含依會員查詢、狀態篩選、服務地區篩選等功能
 */
@Repository
public interface SitterRepository extends JpaRepository<SitterVO, Integer> {

        /**
         * 依會員編號查詢保姆
         * 
         * @param memId 會員編號
         * @return SitterVO 該會員的保姆資料 (一個會員只能有一個保姆身分)
         */
        SitterVO findByMemId(Integer memId);

        /**
         * 依保姆狀態查詢
         * 
         * @param sitterStatus 保姆狀態 (0:啟用, 1:停用)
         * @return List<SitterVO> 符合狀態的保姆列表
         */
        List<SitterVO> findBySitterStatus(Byte sitterStatus);

        @org.springframework.data.jpa.repository.Modifying
        @org.springframework.data.jpa.repository.Query("UPDATE SitterVO s SET s.serviceTime = :serviceTime WHERE s.sitterId = :sitterId")
        void updateServiceTime(Integer sitterId, String serviceTime);

        // ========== 以下為會員搜尋保姆功能新增的方法 ==========

        /**
         * 查詢所有啟用中的保姆，並按評分數量降序排列
         * 
         * @param sitterStatus 保姆狀態 (0:啟用)
         * @return List<SitterVO> 所有啟用中的保姆
         */
        List<SitterVO> findBySitterStatusOrderBySitterRatingCountDesc(Byte sitterStatus);

        /**
         * 根據服務地區查詢保姆
         * 
         * @param areaId       地區 ID
         * @param sitterStatus 保姆狀態
         * @return List<SitterVO> 符合條件的保姆列表
         */
        @Query("SELECT DISTINCT s FROM SitterVO s " +
                        "JOIN s.serviceAreas sa " +
                        "WHERE sa.area.areaId = :areaId " +
                        "AND s.sitterStatus = :sitterStatus")
        List<SitterVO> findByServiceArea(@Param("areaId") Integer areaId,
                        @Param("sitterStatus") Byte sitterStatus);

        /**
         * 根據多個服務地區查詢保姆
         * 
         * @param areaIds      地區 ID 列表
         * @param sitterStatus 保姆狀態
         * @return List<SitterVO> 符合條件的保姆列表
         */
        @Query("SELECT DISTINCT s FROM SitterVO s " +
                        "JOIN s.serviceAreas sa " +
                        "WHERE sa.area.areaId IN :areaIds " +
                        "AND s.sitterStatus = :sitterStatus")
        List<SitterVO> findByServiceAreas(@Param("areaIds") List<Integer> areaIds,
                        @Param("sitterStatus") Byte sitterStatus);
}
