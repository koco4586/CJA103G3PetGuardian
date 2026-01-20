package com.petguardian.chat.service;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.petguardian.chat.model.ChatMemberRepository;
import com.petguardian.chat.model.ChatMemberVO;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Mock authentication strategy implementation for MVP development.
 * Reads 'sessionId' (preferred, via POST) or 'userId' (legacy) from request
 * parameters.
 * 
 * Usage:
 * - POST /chat (body: sessionId=1001)
 * - GET /chat?userId=1001
 */
@Service
@Primary
public class MockAuthStrategyServiceImpl implements AuthStrategyService {

    private static final String SESSION_MEMBER_ID = "memId";

    private final ChatMemberRepository memberRepository;

    public MockAuthStrategyServiceImpl(ChatMemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public Integer getCurrentUserId(HttpServletRequest request) {
        // 1. Try URL/Body parameter (for MVP testing)
        // Support "sessionId" (POST flow) or "userId" (Legacy/GET flow)
        String sessionIdParam = request.getParameter("sessionId");
        if (sessionIdParam != null && !sessionIdParam.isEmpty()) {
            try {
                return Integer.parseInt(sessionIdParam);
            } catch (NumberFormatException e) {
                // Invalid format
            }
        }

        String userIdParam = request.getParameter("userId");
        if (userIdParam != null && !userIdParam.isEmpty()) {
            try {
                return Integer.parseInt(userIdParam);
            } catch (NumberFormatException e) {
                // Fall through to session check
            }
        }

        // 2. Fall back to session (for normal login flow)
        jakarta.servlet.http.HttpSession session = request.getSession(false);
        if (session != null) {
            Object memId = session.getAttribute(SESSION_MEMBER_ID);
            if (memId instanceof Integer) {
                return (Integer) memId;
            }
            if (memId instanceof String) {
                try {
                    return Integer.parseInt((String) memId);
                } catch (NumberFormatException e) {
                    // Ignore
                }
            }
        }

        return null;
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
