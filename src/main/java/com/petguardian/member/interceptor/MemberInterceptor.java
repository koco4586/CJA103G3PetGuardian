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

			//有登入畫面後，上面三行改成下面這行,這行只是在 response 物件中「設置」了重定向的標頭,不會直接飛走
			response.sendRedirect(request.getContextPath() + "/html/frontend/member/login/login.html");  //request.getContextPath():應用程式部署的路徑前綴,這裡取到的是 "" (空字串)
			
			return false;	//這行執行時拒絕http request請求,整個請求處理流程結束後,response才被發送給瀏覽器,瀏覽器收到重定向的標頭才"真正跳轉"

		}

		return true;

	}

}