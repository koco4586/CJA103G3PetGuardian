package com.petguardian.sitter.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
}
