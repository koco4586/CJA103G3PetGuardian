// package com.petguardian.common.service;

// import org.springframework.context.annotation.Primary;
// import org.springframework.stereotype.Service;

// import com.petguardian.member.model.Member;
// import com.petguardian.member.repository.login.MemberLoginRepository;

// import jakarta.servlet.http.HttpServletRequest;
// import jakarta.servlet.http.HttpSession;

// /**
// * Mock authentication strategy implementation for MVP development.
// * Reads 'sessionId' (preferred, via POST) or 'userId' (legacy) from request
// * parameters.
// *
// * Usage:
// * - POST /chat (body: sessionId=1001)
// * - GET /chat?userId=1001
// *
// * IMPROVEMENT:
// * Now writes to HttpSession if a valid ID is provided via params,
// * effectively "logging in" the user for other modules.
// */
// @Service
// public class MockAuthStrategyServiceImpl implements AuthStrategyService {

// private static final String SESSION_MEMBER_ID = "memId";

// private final MemberLoginRepository memberRepository;

// public MockAuthStrategyServiceImpl(MemberLoginRepository memberRepository) {
// this.memberRepository = memberRepository;
// }

// @Override
// public Integer getCurrentUserId(HttpServletRequest request) {
// // 1. Try URL/Body parameter (for MVP testing)
// // Support "sessionId" (POST flow) or "userId" (Legacy/GET flow)
// Integer foundId = null;

// String sessionIdParam = request.getParameter("sessionId");
// if (sessionIdParam != null && !sessionIdParam.isEmpty()) {
// try {
// foundId = Integer.parseInt(sessionIdParam);
// } catch (NumberFormatException e) {
// // Invalid format
// }
// }

// if (foundId == null) {
// String userIdParam = request.getParameter("userId");
// if (userIdParam != null && !userIdParam.isEmpty()) {
// try {
// foundId = Integer.parseInt(userIdParam);
// } catch (NumberFormatException e) {
// // Fall through
// }
// }
// }

// // 2. If ID found in params, WRITE TO SESSION (Mock Login)
// if (foundId != null) {
// HttpSession session = request.getSession(true); // Create session if needed
// session.setAttribute(SESSION_MEMBER_ID, foundId);
// return foundId;
// }

// // 3. Fall back to session (for normal login flow or previously mocked
// session)
// HttpSession session = request.getSession(false);
// if (session != null) {
// Object memId = session.getAttribute(SESSION_MEMBER_ID);
// if (memId instanceof Integer) {
// return (Integer) memId;
// }
// if (memId instanceof String) {
// try {
// return Integer.parseInt((String) memId);
// } catch (NumberFormatException e) {
// // Ignore
// }
// }
// }

// return null;
// }

// @Override
// public String getCurrentUserName(HttpServletRequest request) {
// Integer userId = getCurrentUserId(request);
// if (userId == null) {
// return null;
// }
// Member member = memberRepository.findById(userId).orElse(null);
// return member != null ? member.getMemName() : "User " + userId;
// }
// }
