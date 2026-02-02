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
                .addPathPatterns("/admin/**")//"/admin/**"
                .excludePathPatterns(
                        "/admin/adminloginpage",
                        "/admin/adminlogin",
//                        "/html/backend/admin/admin_login_success.html",
                        "/css/**",
                        "/js/**",
                        "/images/**",
//                        "/admin/index",
//                        "/admin-member-managementpage",
//                        "/admin/admin-admin-managementpage",
//                        "/admin/adminlogoutpage",
//                        "/admin/adminresetpwpage",
//                        "/adminupdateinfopage",
//                        "/admin/admin-member-management",
//                        "/admin/admin-member-management-searchmember",
//                        "/admin/admin-member-management-updatestatus",
//                        "/admin/admin-admin-management",
//                        "/admin/admin-admin-management-updatestatus",
                        "/admin/adminlogout",
                        "/html/backend/admin/admin_logout.html",
                        "/html/**"
                );
    }
}

