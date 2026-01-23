package com.petguardian.member.interceptor;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class MemberInterceptor implements HandlerInterceptor{

    @Override
    public boolean preHandle(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler)
            throws Exception {

        HttpSession session = request.getSession(false);

        Integer memId = (session != null) ? (Integer)session.getAttribute("memId") : null;

        if(memId != null){
            return true;
        }

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);//401

        response.setContentType("application/json;charset=UTF-8");

        response.getWriter().write("{\"message\":\"請先登入\",\"code\":\"UNAUTHORIZED\"}");

        return false;
    }

}