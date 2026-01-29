package com.petguardian.sitter.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
     * 計算申請狀態不是指定值的申請數量
     * 用於統計待審保母數量（排除已通過的申請）
     */
    long countByAppStatusNot(Byte appStatus);
}
