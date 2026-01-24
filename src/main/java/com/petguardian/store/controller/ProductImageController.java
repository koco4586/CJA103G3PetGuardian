package com.petguardian.store.controller;

import com.petguardian.store.service.ImageCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

/**
 * 商品圖片 API 控制器
 * 提供獨立的圖片端點，讓瀏覽器可以快取圖片
 */
@RestController
@RequestMapping("/api/product")
public class ProductImageController {

    @Autowired
    private ImageCacheService imageCacheService;

    /**
     * 取得商品圖片
     * GET /api/product/{proId}/image
     *
     * 設定瀏覽器快取 7 天，大幅減少重複請求
     */
    @GetMapping("/{proId}/image")
    public ResponseEntity<byte[]> getProductImage(@PathVariable Integer proId) {
        byte[] imageBytes = imageCacheService.getProductImageBytes(proId);

        if (imageBytes == null) {
            return ResponseEntity.notFound().build();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG);
        headers.setCacheControl(CacheControl.maxAge(7, TimeUnit.DAYS).cachePublic());

        return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
    }
}
