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
            
			//有登入畫面後，上面三行改成下面這行,這行只是在 response 物件中「設置」了重定向的標頭,不會直接飛走
			response.sendRedirect(request.getContextPath() + "/html/backend/admin/admin_login.html");  //request.getContextPath():應用程式部署的路徑前綴,這裡取到的是 "" (空字串)

			
			return false;   //這行執行時拒絕http request請求,整個請求處理流程結束後,response才被發送給瀏覽器,瀏覽器收到重定向的標頭才"真正跳轉"
		}

		return true;
	}

}