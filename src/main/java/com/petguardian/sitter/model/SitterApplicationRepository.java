package com.petguardian.sitter.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 保姆申請 Repository
 * 
 * 提供保姆申請單的資料存取
 * 包含申請狀態查詢與會員申請紀錄查詢
 */
@Repository
public interface SitterApplicationRepository extends JpaRepository<SitterApplicationVO, Integer> {

    /**
     * 依會員編號查詢申請紀錄
     * 
     * @param memId 會員編號
     * @return List<SitterApplicationVO> 該會員的所有申請
     */
    List<SitterApplicationVO> findByMemId(Integer memId);

    /**
     * 依申請狀態查詢
     * 
     * @param appStatus 申請狀態 (0:待審核, 1:通過, 2:不通過, 3:撤回)
     * @return List<SitterApplicationVO> 符合狀態的申請列表
     */
    List<SitterApplicationVO> findByAppStatus(Byte appStatus);

    /**
     * 計算指定申請狀態的申請數量
     * 用於統計待審核的保母申請數量（appStatus = 0 表示待審核）
     */
    long countByAppStatus(Byte appStatus);
}
