package com.petguardian.chat.service;

import org.springframework.stereotype.Service;

import com.petguardian.chat.model.ChatMemberRepository;
import com.petguardian.chat.model.ChatMemberEntity;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * Reads member ID from HttpSession (set by teammate).
 * 
 * Expected session attribute: "memId" (Integer)
 * 
 * To switch to this implementation:
 * 1. Remove @Primary from MockAuthStrategyServiceImpl
 * 2. Add @Primary to this class
 */
@Service
public class SessionAuthStrategyServiceImpl implements AuthStrategyService {

    private static final String SESSION_MEMBER_ID = "memId";
    private static final String SESSION_MEMBER_NAME = "memName";

    private final ChatMemberRepository memberRepository;

    public SessionAuthStrategyServiceImpl(ChatMemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public Integer getCurrentUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        Object memId = session.getAttribute(SESSION_MEMBER_ID);
        if (memId instanceof Integer) {
            return (Integer) memId;
        }
        if (memId instanceof String) {
            try {
                return Integer.parseInt((String) memId);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    @Override
    public String getCurrentUserName(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }

        // Try to get name from session first
        Object memName = session.getAttribute(SESSION_MEMBER_NAME);
        if (memName instanceof String) {
            return (String) memName;
        }

        // Fall back to database lookup
        Integer userId = getCurrentUserId(request);
        if (userId == null) {
            return null;
        }
        ChatMemberEntity member = memberRepository.findById(userId).orElse(null);
        return member != null ? member.getMemName() : null;
    }
}
