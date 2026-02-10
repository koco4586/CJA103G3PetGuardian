package com.petguardian.member.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MemberManagementConfig implements WebMvcConfigurer {

	private static final String REALPATH = "src/main/resources/static/images/member/";

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/member/**") // 只要有 /member/開頭的url自定義路徑GET請求就會被攔截（也就是fetch取得的data會是假路徑，
													// 把假路徑放到img標籤的src會發出httpGET請求）
				.addResourceLocations("file:" + REALPATH); // 並且去照片儲存真實路徑資料夾src/main/resources/static/images/member/資料夾找檔案，並直接回傳
	}
}
