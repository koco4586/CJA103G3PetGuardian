package com.petguardian.store.controller;

import com.petguardian.common.service.AuthStrategyService;
import com.petguardian.orders.model.StoreMemberRepository;
import com.petguardian.orders.model.StoreMemberVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * 全域 Model 屬性注入
 * 為所有 Controller 自動注入常用資料（如登入會員資訊）
 */
@ControllerAdvice
public class GlobalModelAdvice {

    @Autowired
    private AuthStrategyService authService;

    @Autowired
    private StoreMemberRepository memberRepository;

    /**
     * 自動注入當前登入會員資訊
     * 在所有 Thymeleaf 模板中可使用 ${currentMember} 存取
     */
    @ModelAttribute("currentMember")
    public StoreMemberVO getCurrentMember(HttpServletRequest request) {
        Integer memId = authService.getCurrentUserId(request);
        if (memId == null) {
            return null;
        }
        return memberRepository.findById(memId).orElse(null);
    }
}
