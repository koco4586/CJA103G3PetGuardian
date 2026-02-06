package com.petguardian.store.service;

import com.petguardian.seller.model.ProductPic;
import com.petguardian.seller.model.ProductPicRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 商品圖片快取服務
 * 避免每次載入頁面時重複從資料庫讀取圖片 URL
 */
@Service
public class ImageCacheService {

    @Autowired
    private ProductPicRepository productPicDAO;

    // 使用 ConcurrentHashMap 作為快取
    private final Map<Integer, String> imageCache = new ConcurrentHashMap<>();

    // 預設佔位圖
    private static final String PLACEHOLDER_IMAGE = "/images/default-product.png";

    /**
     * 取得商品圖片 URL（優先從快取讀取）
     */
    public String getProductImageUrl(Integer proId) {
        // 先從快取讀取
        String cachedImage = imageCache.get(proId);
        if (cachedImage != null) {
            return cachedImage;
        }

        // 從資料庫讀取
        String imageUrl = loadImageFromDatabase(proId);

        // 存入快取
        imageCache.put(proId, imageUrl);

        return imageUrl;
    }

    /**
     * 從資料庫載入圖片 URL
     */
    private String loadImageFromDatabase(Integer proId) {
        List<ProductPic> pics = productPicDAO.findByProduct_ProId(proId);

        if (!pics.isEmpty() && pics.get(0).getProPic() != null && !pics.get(0).getProPic().isEmpty()) {
            return pics.get(0).getProPic();
        }

        return PLACEHOLDER_IMAGE;
    }

    /**
     * 取得商品圖片 URL（用於 API 端點）
     */
    public String getProductImage(Integer proId) {
        List<ProductPic> pics = productPicDAO.findByProduct_ProId(proId);

        if (!pics.isEmpty() && pics.get(0).getProPic() != null && !pics.get(0).getProPic().isEmpty()) {
            return pics.get(0).getProPic();
        }

        return null;
    }

    /**
     * 清除特定商品的快取（當商品圖片更新時呼叫）
     */
    public void evictCache(Integer proId) {
        imageCache.remove(proId);
    }

    /**
     * 清除所有快取
     */
    public void clearCache() {
        imageCache.clear();
    }

    /**
     * 預載多個商品圖片到快取
     */
    public void preloadImages(List<Integer> proIds) {
        for (Integer proId : proIds) {
            if (!imageCache.containsKey(proId)) {
                String imageUrl = loadImageFromDatabase(proId);
                imageCache.put(proId, imageUrl);
            }
        }
    }
}
