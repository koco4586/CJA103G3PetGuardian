package com.petguardian.sitter.service;

import java.util.List;
import java.util.Map;
import com.petguardian.sitter.model.SitterApplicationVO;

public interface SitterApplicationService {

    /**
     * 新增保姆申請
     * 
     * @param memId      會員編號
     * @param intro      個人簡介
     * @param experience 相關經驗
     * @return SitterApplicationVO 新增的申請物件
     */
    SitterApplicationVO createApplication(Integer memId, String intro, String experience);

    /**
     * 審核申請 (管理員功能)
     * 
     * @param appId      申請編號
     * @param status     新狀態 (1:通過, 2:不通過)
     * @param reviewNote 審核意見
     * @return SitterApplicationVO 更新後的申請物件
     */
    SitterApplicationVO reviewApplication(Integer appId, Byte status, String reviewNote);

    /**
     * 查詢會員的所有申請
     * 
     * @param memId 會員編號
     * @return List<SitterApplicationVO>
     */
    List<SitterApplicationVO> getApplicationsByMember(Integer memId);

    /**
     * 查詢特定狀態的申請 (例如待審核)
     * 
     * @param status 狀態碼 (0:待審核)
     * @return List<SitterApplicationVO>
     */
    List<SitterApplicationVO> getApplicationsByStatus(Byte status);

    /**
     * 取得單筆申請詳情
     * 
     * @param appId 申請編號
     * @return SitterApplicationVO
     */
    SitterApplicationVO getApplicationById(Integer appId);

    /**
     * 取得所有申請紀錄
     * 
     * @return List<SitterApplicationVO>
     */
    List<SitterApplicationVO> getAllApplications();

    /**
     * 檢查會員是否已經擁有有效的保姆資格 (即通過審核)
     * 
     * @param memId 會員編號
     * @return true 若已是保姆
     */
    boolean isSitter(Integer memId);

    /**
     * [Refactor] 檢查會員申請狀態，回傳對應的提示訊息
     * 
     * @param memId 會員編號
     * @return 提示訊息 (若無問題則回傳 null)
     */
    String checkApplicationStatus(Integer memId);

    /**
     * [Refactor] 取得申請頁面所需的初始資料 (會員名稱、電話、頭像等)
     * 
     * @param memId     會員編號
     * @param avatarUrl Session 中的頭像 URL
     * @return 包含資料的 Map
     */
    Map<String, Object> getApplyFormInitData(Integer memId, String avatarUrl);
}
