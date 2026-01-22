package com.petguardian.common.service;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private static final String SESSION_KEY = "memId";
    private static final Integer TEST_MEM_ID = 1003; // 統一測試 ID

    /**
     * 核心方法：獲取當前會員 ID
     * 整合時只需修改此處的邏輯（例如改用 Spring Security）
     */
    public Integer getCurrentMemId(HttpSession session) {
        Integer memId = (Integer) session.getAttribute(SESSION_KEY);

        // 模擬登入邏輯
        if (memId == null) {
            memId = TEST_MEM_ID;
            session.setAttribute(SESSION_KEY, memId);
        }
        return memId;
    }

    /**
     * 檢查是否已登入（不會自動設定測試 ID）
     * @return 若已登入返回會員 ID，否則返回 null
     */
    public Integer getLoggedInMemId(HttpSession session) {
        return (Integer) session.getAttribute(SESSION_KEY);
    }

    /**
     * 檢查是否已登入
     * @return true 表示已登入，false 表示未登入
     */
    public boolean isLoggedIn(HttpSession session) {
        return session.getAttribute(SESSION_KEY) != null;
    }
}