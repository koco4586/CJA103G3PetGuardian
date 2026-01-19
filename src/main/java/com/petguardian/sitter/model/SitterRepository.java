package com.petguardian.sitter.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
}
