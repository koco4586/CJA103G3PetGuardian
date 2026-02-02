package com.petguardian.member.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {//開發測試專用,完全關閉Spring Security的保護機制

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
        		.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authorize -> authorize
                		
                		//下面如果有人需要把自己一部分網站封掉(指的是需要登入)的話，把("")裡面的變成自己的登入網址(下面是參考)
//                		.requestMatchers("/html").authenticated()
//                		
//                		//下面是自己網址不需要登入的地方，要用的話，也比照上面說的，複製貼上然後改("")內的網址就好
//                		.requestMatchers("/pet/**",  "/login", "/images/**", "/css/**", "/js/**").permitAll()
                		
                        .anyRequest().permitAll()  // 所有請求都允許
                );//記得把這裡打開
                
//                .formLogin(form -> form
//                        .loginPage("") // 指向你的專案登入頁
//                        .permitAll()
                    
    
        
    

        return http.build();
    }
}