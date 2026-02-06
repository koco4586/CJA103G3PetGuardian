package com.petguardian.seller.service;

import com.petguardian.seller.model.*;
import com.petguardian.store.service.ImageCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProTypeRepository proTypeRepository;

    @Autowired
    private ProductPicRepository productPicRepository;

    @Autowired
    private ImageCacheService imageCacheService;

    // 預設圖片（當商品沒有圖片時使用）
    private static final String DEFAULT_IMAGE = "/images/default-product.png";

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
            // 檢查 URL 是否存在且非空白
            if (pic.getProPic() != null && !pic.getProPic().trim().isEmpty()) {
                Map<String, Object> picData = new HashMap<>();
                picData.put("productPicId", pic.getProductPicId());
                // 直接回傳圖片URL，不再做 Base64 轉換
                picData.put("imageUrl", pic.getProPic());
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
            ProductPic firstPic = pics.get(0);
            // 直接回傳URL字串
            if (firstPic.getProPic() != null && !firstPic.getProPic().trim().isEmpty()) {
                return firstPic.getProPic();
            }
        }

        return DEFAULT_IMAGE;
    }

    @Override
    @Transactional
    public void saveProductImageUrl(Integer proId, String imageUrl) {
        if (proId == null || imageUrl == null || imageUrl.trim().isEmpty()) {
            return;
        }

        Optional<Product> productOpt = productRepository.findById(proId);
        if (productOpt.isPresent()) {
            ProductPic pic = new ProductPic();
            pic.setProduct(productOpt.get());
            pic.setProPic(imageUrl.trim());
            productPicRepository.save(pic);
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
                                         String imageUrl, List<Integer> deleteImageIds) {
        System.out.println("=== ProductServiceImpl.saveProductWithImages 開始 ===");

        try {
            Product product = null;

            // 1. 判斷新增或編輯
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

            // 3. 圖片URL處理
            boolean hasNewUrl = (imageUrl != null && !imageUrl.trim().isEmpty());

            if (hasNewUrl) {
                // 使用者輸入了新的圖片URL，清除舊圖片並存入新URL
                System.out.println("偵測到新圖片URL，執行舊圖全數清除並寫入新URL");

                List<ProductPic> oldPics = productPicRepository.findByProduct_ProId(savedProId);
                if (oldPics != null && !oldPics.isEmpty()) {
                    productPicRepository.deleteAll(oldPics);
                    productPicRepository.flush();
                    System.out.println("已移除 " + oldPics.size() + " 張舊圖片紀錄");
                }

                // 儲存新圖片URL
                ProductPic pic = new ProductPic();
                pic.setProduct(savedProduct);
                pic.setProPic(imageUrl.trim());
                productPicRepository.save(pic);
                System.out.println("新圖片URL已儲存: " + imageUrl.trim());

            } else {
                // 使用者沒有輸入新URL，檢查是否有要刪除的圖片
                if (deleteImageIds != null && !deleteImageIds.isEmpty()) {
                    System.out.println("執行指定圖片刪除，數量: " + deleteImageIds.size());
                    for (Integer picId : deleteImageIds) {
                        productPicRepository.deleteById(picId);
                    }
                    productPicRepository.flush();
                } else {
                    System.out.println("無新URL也無刪除指令，保留原圖片");
                }
            }

            // 4. 清除圖片快取
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