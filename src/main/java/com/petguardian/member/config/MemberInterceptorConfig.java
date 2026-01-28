package com.petguardian.member.config;

import com.petguardian.member.interceptor.MemberInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MemberInterceptorConfig implements WebMvcConfigurer{

    @Autowired
    private MemberInterceptor memberInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(memberInterceptor)
                .addPathPatterns("/front/**")
                .excludePathPatterns(
                        "/front/loginpage",
                        "/front/login",
                        "/html/frontend/member/register/registersuccess.html",
						"/front/registerpage",
                        "/front/register",
						"/css/**",
						"/js/**",
						"/images/**",
						"/sitter/*",
						"/pet/evaluate"
                );
    }
}


