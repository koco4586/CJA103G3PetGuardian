package com.petguardian.seller.service;

import com.petguardian.seller.model.*;
import com.petguardian.store.service.ImageCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProTypeRepository proTypeRepository;

    @Autowired
    private ProductPicRepository productPicRepository;

    // 注入圖片快取服務，用於在圖片更新後清除快取
    @Autowired
    private ImageCacheService imageCacheService;

    // 預設圖片（1x1透明PNG）
    private static final String DEFAULT_IMAGE = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8/5+hHgAHggJ/PchI7wAAAABJRU5ErkJggg==";

    // 最大圖片數量限制
    private static final int MAX_IMAGE_COUNT = 1;

    // ==================== 基本查詢 ====================

    @Override
    @Transactional(readOnly = true)
    public List<Product> getSellerProducts(Integer memId) {
        if (memId == null) {
            return new ArrayList<>();
        }
        return productRepository.findByMemIdOrderByLaunchedTimeDesc(memId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Product> getProductById(Integer proId) {
        if (proId == null) {
            return Optional.empty();
        }
        return productRepository.findById(proId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProType> getAllProTypes() {
        return proTypeRepository.findAll();
    }

    // ==================== 商品 CRUD ====================

    @Override
    @Transactional
    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    @Override
    @Transactional
    public void deleteProduct(Integer proId) {
        if (proId != null) {
            productRepository.deleteById(proId);
            // 刪除商品時清除快取
            imageCacheService.evictCache(proId);
        }
    }

    @Override
    @Transactional
    public boolean deleteProductBySeller(Integer sellerId, Integer proId) {
        Optional<Product> productOpt = productRepository.findById(proId);
        if (!productOpt.isPresent()) {
            return false;
        }

        Product product = productOpt.get();
        if (!product.getMemId().equals(sellerId)) {
            return false;
        }

        productRepository.deleteById(proId);
        // 刪除商品時清除快取
        imageCacheService.evictCache(proId);
        return true;
    }

    // ==================== 圖片管理 ====================

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getProductImages(Integer proId) {
        List<Map<String, Object>> result = new ArrayList<>();

        if (proId == null) {
            return result;
        }

        List<ProductPic> pics = productPicRepository.findByProduct_ProId(proId);
        for (ProductPic pic : pics) {
            if (pic.getProPic() != null && pic.getProPic().length > 0) {
                Map<String, Object> picData = new HashMap<>();
                picData.put("productPicId", pic.getProductPicId());
                picData.put("imageBase64", "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(pic.getProPic()));
                result.add(picData);
            }
        }

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public String getProductMainImage(Integer proId) {
        if (proId == null) {
            return DEFAULT_IMAGE;
        }

        List<ProductPic> pics = productPicRepository.findByProduct_ProId(proId);

        if (pics != null && !pics.isEmpty()) {
            // 總是回傳最新的一張圖片作為主圖
            ProductPic firstPic = pics.get(0);
            if (firstPic.getProPic() != null && firstPic.getProPic().length > 0) {
                return "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(firstPic.getProPic());
            }
        }

        return DEFAULT_IMAGE;
    }

    @Override
    @Transactional
    public void saveProductImages(Integer proId, List<MultipartFile> images) {
        // 此方法保留給單純上傳圖片的 API 使用，但在 saveProductWithImages 已有完整實作
        if (images == null || images.isEmpty() || proId == null) {
            return;
        }
    }

    @Override
    @Transactional
    public void deleteProductImage(Integer productPicId) {
        if (productPicId != null) {
            try {
                Optional<ProductPic> picOpt = productPicRepository.findById(productPicId);
                Integer proId = null;
                if (picOpt.isPresent()) {
                    proId = picOpt.get().getProduct().getProId();
                }
                productPicRepository.deleteById(productPicId);
                if (proId != null) {
                    imageCacheService.evictCache(proId);
                }
            } catch (Exception e) {
                System.err.println("刪除圖片失敗 ID: " + productPicId + ", 錯誤: " + e.getMessage());
            }
        }
    }

    // ==================== 整合查詢 ====================

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getSellerProductsWithImages(Integer sellerId) {
        List<Map<String, Object>> result = new ArrayList<>();
        List<Product> products = getSellerProducts(sellerId);

        for (Product product : products) {
            Map<String, Object> productData = new HashMap<>();
            productData.put("product", product);
            productData.put("mainImage", getProductMainImage(product.getProId()));
            result.add(productData);
        }

        return result;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Product saveProductWithImages(Integer sellerId, Integer proId, String proName,
                                         Integer proTypeId, Integer proPrice, String proDescription,
                                         Integer stockQuantity, Integer proState,
                                         List<MultipartFile> newImages, List<Integer> deleteImageIds) {
        System.out.println("=== ProductServiceImpl.saveProductWithImages 開始 ===");

        try {
            Product product = null;

            // 1. 判斷新增/編輯
            if (proId != null && proId > 0) {
                System.out.println("模式: 編輯現有商品 ID: " + proId);
                product = productRepository.findById(proId)
                        .orElseThrow(() -> new RuntimeException("商品不存在"));

                if (!product.getMemId().equals(sellerId)) {
                    throw new RuntimeException("無權限編輯此商品");
                }
            } else {
                System.out.println("模式: 新增商品");
                product = new Product();
                product.setMemId(sellerId);
            }

            // 2. 設定商品基本資料
            ProType proType = proTypeRepository.findById(proTypeId)
                    .orElseThrow(() -> new RuntimeException("商品類別不存在"));

            product.setProType(proType);
            product.setProName(proName);
            product.setProPrice(proPrice);
            product.setProDescription(proDescription);
            product.setStockQuantity(stockQuantity);
            product.setProState(proState);

            Product savedProduct = productRepository.saveAndFlush(product);
            Integer savedProId = savedProduct.getProId();

            // =================================================================
            // 3. 圖片處理
            // =================================================================

            // 檢查是否有上傳有效的新圖片
            boolean hasNewUpload = false;
            MultipartFile fileToUpload = null;

            if (newImages != null && !newImages.isEmpty()) {
                for (MultipartFile img : newImages) {
                    if (img != null && !img.isEmpty() && img.getSize() > 0) {
                        hasNewUpload = true;
                        fileToUpload = img;
                        break;
                    }
                }
            }

            if (hasNewUpload) {
                // 【使用者上傳了新圖】
                // 不論前端有沒有傳 deleteImageIds，直接清空該商品所有舊圖，並存入新圖。

                System.out.println("偵測到新圖片上傳，執行舊圖全數清除並寫入新圖");

                // 找出該商品目前所有圖片
                List<ProductPic> oldPics = productPicRepository.findByProduct_ProId(savedProId);
                if (oldPics != null && !oldPics.isEmpty()) {
                    productPicRepository.deleteAll(oldPics);
                    productPicRepository.flush(); // 強制執行刪除
                    System.out.println("已移除 " + oldPics.size() + " 張舊圖片");
                }

                // 儲存新圖片
                try {
                    ProductPic pic = new ProductPic();
                    pic.setProduct(savedProduct);
                    pic.setProPic(fileToUpload.getBytes());
                    productPicRepository.save(pic);
                    System.out.println("新圖片已儲存");
                } catch (Exception e) {
                    throw new RuntimeException("圖片 bytes 讀取失敗", e);
                }

            } else {
                // 【使用者沒有上傳新圖】
                // 檢查 deleteImageIds，如果使用者在前端按了 "X" 刪除舊圖，則執行刪除。
                // 如果沒有 deleteImageIds，則保留原樣。

                if (deleteImageIds != null && !deleteImageIds.isEmpty()) {
                    System.out.println("執行指定圖片刪除，數量: " + deleteImageIds.size());
                    for (Integer picId : deleteImageIds) {
                        productPicRepository.deleteById(picId);
                    }
                    productPicRepository.flush();
                } else {
                    System.out.println("無新圖也無刪除指令，保留原圖片");
                }
            }

            // 4. 清除圖片快取 (重要：確保前端重新載入時抓到最新的)
            imageCacheService.evictCache(savedProId);
            System.out.println("已清除商品 " + savedProId + " 的圖片快取");

            System.out.println("=== saveProductWithImages 結束 ===");
            return savedProduct;

        } catch (Exception e) {
            System.err.println("儲存商品發生錯誤: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("儲存失敗: " + e.getMessage(), e);
        }
    }

    // ==================== 統計 ====================

    @Override
    @Transactional(readOnly = true)
    public long countSellerProducts(Integer sellerId) {
        List<Product> products = getSellerProducts(sellerId);
        return products.size();
    }

    @Override
    @Transactional(readOnly = true)
    public long countActiveProducts(Integer sellerId) {
        List<Product> products = getSellerProducts(sellerId);
        return products.stream()
                .filter(p -> p.getProState() != null && p.getProState() == 1)
                .count();
    }
}