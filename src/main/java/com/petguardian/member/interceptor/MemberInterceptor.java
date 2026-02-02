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

		// if(memId != null){

		if (memId == null) {

			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);// 401

			response.setContentType("application/json;charset=UTF-8");

			response.getWriter().write("{\"message\":\"請先登入\",\"code\":\"UNAUTHORIZED\"}");

			return false;

		}

		// 檢查會員當前狀態
		Member member = memberLoginRepository.findById(memId).orElse(null);
		if (member == null || member.getMemStatus() == 0) {
			session.invalidate(); // 清除 session
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			response.setContentType("application/json;charset=UTF-8");
			response.getWriter().write("{\"message\":\"帳號已被停權\",\"code\":\"ACCOUNT_SUSPENDED\"}");
			return false;
		}

		return true;

//            return true;
//        }

		// response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);//401

		// response.setContentType("application/json;charset=UTF-8");

		// response.getWriter().write("{\"message\":\"請先登入\",\"code\":\"UNAUTHORIZED\"}");

		// return false;
	}

}