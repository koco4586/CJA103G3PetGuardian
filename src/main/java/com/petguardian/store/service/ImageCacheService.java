package com.petguardian.store.service;

import com.petguardian.seller.model.ProductPic;
import com.petguardian.seller.model.ProductPicRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 商品圖片快取服務
 * 避免每次載入頁面時重複從資料庫讀取並轉換 Base64
 */
@Service
public class ImageCacheService {

    @Autowired
    private ProductPicRepository productPicDAO;

    // 使用 ConcurrentHashMap 作為快取
    private final Map<Integer, String> imageCache = new ConcurrentHashMap<>();

    // 預設佔位圖（1x1 灰色像素）
    private static final String PLACEHOLDER_IMAGE =
        "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8/5+hHgAHggJ/PchI7wAAAABJRU5ErkJggg==";

    /**
     * 取得商品圖片 Base64（優先從快取讀取）
     */
    public String getProductImageBase64(Integer proId) {
        // 先從快取讀取
        String cachedImage = imageCache.get(proId);
        if (cachedImage != null) {
            return cachedImage;
        }

        // 從資料庫讀取
        String imageBase64 = loadImageFromDatabase(proId);

        // 存入快取
        imageCache.put(proId, imageBase64);

        return imageBase64;
    }

    /**
     * 從資料庫載入圖片並轉換為 Base64
     */
    private String loadImageFromDatabase(Integer proId) {
        List<ProductPic> pics = productPicDAO.findByProduct_ProId(proId);

        if (!pics.isEmpty() && pics.get(0).getProPic() != null) {
            byte[] imageBytes = pics.get(0).getProPic();
            return "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(imageBytes);
        }

        return PLACEHOLDER_IMAGE;
    }

    /**
     * 取得原始圖片位元組（用於 API 端點）
     */
    public byte[] getProductImageBytes(Integer proId) {
        List<ProductPic> pics = productPicDAO.findByProduct_ProId(proId);

        if (!pics.isEmpty() && pics.get(0).getProPic() != null) {
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
                String imageBase64 = loadImageFromDatabase(proId);
                imageCache.put(proId, imageBase64);
            }
        }
    }
}
