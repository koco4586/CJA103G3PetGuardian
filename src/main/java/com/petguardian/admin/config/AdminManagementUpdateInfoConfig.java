package com.petguardian.admin.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class AdminManagementUpdateInfoConfig implements WebMvcConfigurer {

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {

		registry.addResourceHandler("/adminupdateinfo/**")
		.addResourceLocations("file:/Users/CJA103G3_Workspace/CJA103G3PetGuardian/src/main/resources/static/images/backend/admin/adminupdateinfo/");
	}
}