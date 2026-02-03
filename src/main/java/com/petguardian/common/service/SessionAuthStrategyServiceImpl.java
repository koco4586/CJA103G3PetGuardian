package com.petguardian.common.service;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.petguardian.member.model.Member;
import com.petguardian.member.repository.login.MemberLoginRepository;

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
@Primary
public class SessionAuthStrategyServiceImpl implements AuthStrategyService {

    private static final String SESSION_MEMBER_ID = "memId";
    private static final String SESSION_MEMBER_NAME = "memName";

    private final MemberLoginRepository memberRepository;

    public SessionAuthStrategyServiceImpl(MemberLoginRepository memberRepository) {
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
        Member member = memberRepository.findById(userId).orElse(null);
        return member != null ? member.getMemName() : null;
    }
}
