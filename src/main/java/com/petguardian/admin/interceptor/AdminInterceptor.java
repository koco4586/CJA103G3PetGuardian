package com.petguardian.admin.interceptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.petguardian.admin.model.Admin;
import com.petguardian.admin.repository.login.AdminLoginRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class AdminInterceptor implements HandlerInterceptor {

	@Autowired
	private AdminLoginRepository adminLoginRepository;

	@Override
	public boolean preHandle(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler)
			throws Exception {

		//

		// 登出請求直接放行
		String requestURI = request.getRequestURI();
		if (requestURI.contains("/adminlogout")) {
			return true;
		}

		//

		HttpSession session = request.getSession(false);

		Integer admId = (session != null) ? (Integer) session.getAttribute("admId") : null;

		if (admId == null) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.setContentType("application/json;charset=UTF-8");
			response.getWriter().write("{\"message\":\"請先登入\",\"code\":\"UNAUTHORIZED\"}");
			return false;
		}

		// 檢查管理員當前狀態
		Admin admin = adminLoginRepository.findById(admId).orElse(null);
		if (admin == null || admin.getAdmStatus() == 0) {
			session.invalidate(); // 清除 session
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			response.setContentType("application/json;charset=UTF-8");
			response.getWriter().write("{\"message\":\"帳號已被停權\",\"code\":\"ACCOUNT_SUSPENDED\"}");
			return false;
		}

//        if (admId != null) {
//            return true;
//        }

//        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);// 401
//
//        response.setContentType("application/json;charset=UTF-8");
//
//        response.getWriter().write("{\"message\":\"請先登入\",\"code\":\"UNAUTHORIZED\"}");

		return true;// 讓正常的管理員通過
	}

}