package com.petguardian.admin.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class AdminManagementUpdateInfoConfig implements WebMvcConfigurer {

	@Value("${file.upload.admin.path}")
	private String uploadPath;

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		// 只有在外部目錄時才需要這個設定
		// 如果用 resources/static，Spring Boot 會自動處理
		if (!uploadPath.contains("resources/static")) {
			registry.addResourceHandler("/images/backend/admin/**").addResourceLocations("file:" + uploadPath);
		}
	}
}

//package com.petguardian.admin.config;
//
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
//
//@Configuration
//public class AdminManagementUpdateInfoConfig implements WebMvcConfigurer {
//
//	@Override
//	public void addResourceHandlers(ResourceHandlerRegistry registry) {
//
//		registry.addResourceHandler("/adminupdateinfo/**")
//		.addResourceLocations("file:/Users/CJA103G3_Workspace/CJA103G3PetGuardian/src/main/resources/static/images/backend/admin/adminupdateinfo/");
//	}
//}