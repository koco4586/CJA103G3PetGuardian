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

		HttpSession session = request.getSession(false);

		Integer admId = (session != null) ? (Integer) session.getAttribute("admId") : null;

		if (admId == null) {
//			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//			response.setContentType("application/json;charset=UTF-8");
//			response.getWriter().write("{\"message\":\"請先登入\",\"code\":\"UNAUTHORIZED\"}");
//			
			response.sendRedirect(request.getContextPath() + "/html/backend/admin/admin_login.html");  //request.getContextPath():應用程式部署的路徑前綴,這裡取到的是 "" (空字串)

			
			return false;
		}

		return true;
	}

}