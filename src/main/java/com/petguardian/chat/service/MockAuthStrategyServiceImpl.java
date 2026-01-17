package com.petguardian.chat.service;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.petguardian.chat.model.ChatMemberRepository;
import com.petguardian.chat.model.ChatMemberVO;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Mock authentication strategy implementation for MVP development.
 * Reads userId from URL parameter.
 * 
 * Usage: /chat/mvp?userId=1001
 */
@Service
@Primary
public class MockAuthStrategyServiceImpl implements AuthStrategyService {

    private final ChatMemberRepository memberRepository;

    public MockAuthStrategyServiceImpl(ChatMemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public Integer getCurrentUserId(HttpServletRequest request) {
        String userIdParam = request.getParameter("userId");
        if (userIdParam == null || userIdParam.isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(userIdParam);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public String getCurrentUserName(HttpServletRequest request) {
        Integer userId = getCurrentUserId(request);
        if (userId == null) {
            return null;
        }
        ChatMemberVO member = memberRepository.findById(userId).orElse(null);
        return member != null ? member.getMemName() : "User " + userId;
    }
}
