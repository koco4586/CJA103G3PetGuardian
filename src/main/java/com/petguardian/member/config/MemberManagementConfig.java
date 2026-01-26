package com.petguardian.member.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MemberManagementConfig implements WebMvcConfigurer{

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/member/**")                                                             //只要有 /img/ 開頭的url自定義路徑GET請求就會被攔截（也就是fetch取得的data會是假路徑，把假路徑放到img標籤的src會發出http GET請求）
                .addResourceLocations("file:/Users/CJA103G3_Workspace/CJA103G3PetGuardian/src/main/resources/static/images/member/");     //並且去照片儲存真實路徑資料夾/Users/Jialu_WorkSpace/FileImage/src/main/resources/static/img/夾找檔案，並直接回傳
    }
}
