package com.petguardian.store.controller;

import com.petguardian.store.service.ImageCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

/**
 * 商品圖片 API 控制器
 * 提供圖片 URL 重導向端點
 */
@RestController
@RequestMapping("/api/product")
public class ProductImageController {

    @Autowired
    private ImageCacheService imageCacheService;

    /**
     * 取得商品圖片 URL
     * GET /api/product/{proId}/image
     *
     * 重導向到實際圖片路徑
     */
    @GetMapping("/{proId}/image")
    public ResponseEntity<Void> getProductImage(@PathVariable Integer proId) {
        String imageUrl = imageCacheService.getProductImage(proId);

        if (imageUrl == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(imageUrl))
                .build();
    }
}
