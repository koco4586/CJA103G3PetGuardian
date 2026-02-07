package com.petguardian.seller.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 商城商品圖片靜態資源設定
 * 同時從 src 目錄和 target 目錄讀取圖片
 */
@Configuration
public class StoreImageConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 讓 /images/store_image/ 路徑同時從 src 和 target 兩個目錄讀取
        // 上傳圖片存到 src 目錄後，不需要重新編譯就能顯示
        registry.addResourceHandler("/images/store_image/**")
                .addResourceLocations(
                        "file:src/main/resources/static/images/store_image/",
                        "classpath:/static/images/store_image/"
                )
                .setCachePeriod(0);
    }
}