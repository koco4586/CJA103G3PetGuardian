package com.petguardian.sitter.service;

import java.util.List;
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
}
