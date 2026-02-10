package com.petguardian.orders.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
import java.nio.file.Paths;

/**
 * 退貨訂單圖片靜態資源設定
 * 專屬於訂單模組，確保雲端環境能正確讀取上傳的退貨圖片
 */
@Configuration
public class ReturnOrderImageConfig implements WebMvcConfigurer {

    @Value("${file.upload.return.path:src/main/resources/static/images/return/}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 將 /images/return/** 映射到實體上傳路徑與類別路徑
        // 這樣瀏覽器請求 /images/return/abc.jpg 時，Spring 會去指定的實體路徑尋找

        String absolutePath = Paths.get(uploadPath).toAbsolutePath().normalize().toUri().toString();

        registry.addResourceHandler("/images/return/**")
                .addResourceLocations(
                        absolutePath,
                        "classpath:/static/images/return/")
                .setCachePeriod(0); // 開發/測試環境不快取，確保即時看到結果
    }
}
