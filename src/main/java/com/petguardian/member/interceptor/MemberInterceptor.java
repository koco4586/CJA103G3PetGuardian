package com.petguardian.member.interceptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.petguardian.member.model.Member;
import com.petguardian.member.repository.login.MemberLoginRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class MemberInterceptor implements HandlerInterceptor {

	@Autowired
	private MemberLoginRepository memberLoginRepository; // 需要注入

	@Override
	public boolean preHandle(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler)
			throws Exception {

		HttpSession session = request.getSession(false);

		Integer memId = (session != null) ? (Integer) session.getAttribute("memId") : null;

		if (memId == null) {

//			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);// 401

//			response.setContentType("application/json;charset=UTF-8");

//			response.getWriter().write("{\"message\":\"請先登入\",\"code\":\"UNAUTHORIZED\"}");

			response.sendRedirect(request.getContextPath() + "/html/frontend/member/login/login.html");  //request.getContextPath():應用程式部署的路徑前綴,這裡取到的是 "" (空字串)
			
			return false;

		}

		return true;

	}

}