package com.petguardian.sitter.service;

import java.util.List;
import com.petguardian.sitter.model.SitterVO;

public interface SitterService {

    /**
     * 新增保姆
     * 
     * @param memId      會員編號
     * @param sitterName 保姆姓名
     * @param sitterAdd  服務地址
     * @return SitterVO 新增的保姆物件
     */
    SitterVO createSitter(Integer memId, String sitterName, String sitterAdd);

    /**
     * 依會員編號查詢保姆 (會員查詢自己是否為保姆)
     * 
     * @param memId 會員編號
     * @return SitterVO 該會員的保姆資料
     */
    SitterVO getSitterByMemId(Integer memId);

    /**
     * 依保姆編號查詢 (會員查看特定保姆詳情)
     * 
     * @param sitterId 保姆編號
     * @return SitterVO
     */
    SitterVO getSitterById(Integer sitterId);

    /**
     * 查詢所有保姆 (會員瀏覽保姆列表)
     * 
     * @return List<SitterVO>
     */
    List<SitterVO> getAllSitters();

    /**
     * 依狀態查詢保姆 (會員查詢啟用中的保姆)
     * 
     * @param status 保姆狀態 (0:啟用, 1:停用)
     * @return List<SitterVO>
     */
    List<SitterVO> getSittersByStatus(Byte status);

    /**
     * 更新保姆狀態 (管理員或保姆自己)
     * 
     * @param sitterId 保姆編號
     * @param status   新狀態
     * @return SitterVO 更新後的保姆物件
     */
    SitterVO updateSitterStatus(Integer sitterId, Byte status);

    /**
     * 更新保姆資訊 (保姆自己)
     * 
     * @param sitterId   保姆編號
     * @param sitterName 保姆姓名
     * @param sitterAdd  服務地址
     * @return SitterVO 更新後的保姆物件
     */
    SitterVO updateSitterInfo(Integer sitterId, String sitterName, String sitterAdd);
}
