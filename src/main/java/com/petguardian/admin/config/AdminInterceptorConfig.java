package com.petguardian.admin.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.petguardian.admin.interceptor.AdminInterceptor;

@Configuration
public class AdminInterceptorConfig implements WebMvcConfigurer {

    @Autowired
    private AdminInterceptor adminInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(adminInterceptor)
                .addPathPatterns("/admin/**")
                .excludePathPatterns(
                        "/admin/adminloginpage",
                        "/admin/adminlogin",
                        "/admin/admininsertpage",
                        "/admin/admininsert",
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/admin/sitter/*",
                        "/admin/**" // [Temporarily Bypass] Allow all admin pages
                );
    }
}
