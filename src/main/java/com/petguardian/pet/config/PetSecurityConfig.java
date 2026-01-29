package com.petguardian.pet.config;

import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

//@Configuration
//@EnableWebSecurity
@Order(1) 
public class PetSecurityConfig {

	@Bean
    public SecurityFilterChain petFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/pet/**", "/html/pet/**") // 🔴 同時管轄「程式路徑」與「靜態檔案路徑」
            .csrf(csrf -> csrf.disable()) 
            .authorizeHttpRequests(auth -> auth
                // 只有登入者可以「新增/修改」
                .requestMatchers("/pet/update", "/pet/insertBase64", "/html/pet/petupdate.html").authenticated()
                // 其他如查看列表或圖片，全部放行
                .anyRequest().permitAll() 
            )
            .formLogin(form -> form
                .loginPage("/member/login") // 跳轉到會員的登入頁
                .permitAll()
            );
        return http.build();
    }
}