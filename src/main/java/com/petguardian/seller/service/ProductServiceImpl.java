package com.petguardian.seller.service;

import com.petguardian.seller.model.ProType;
import com.petguardian.seller.model.ProTypeRepository;
import com.petguardian.seller.model.Product;
import com.petguardian.seller.model.ProductPic;
import com.petguardian.seller.model.ProductPicRepository;
import com.petguardian.seller.model.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProTypeRepository proTypeRepository;

    @Autowired
    private ProductPicRepository productPicRepository;

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
        if (images == null || images.isEmpty() || proId == null) {
            return;
        }

        Optional<Product> productOpt = productRepository.findById(proId);
        if (!productOpt.isPresent()) {
            throw new RuntimeException("商品不存在: " + proId);
        }
        Product product = productOpt.get();

        // 檢查現有圖片數量
        List<ProductPic> existingPics = productPicRepository.findByProduct_ProId(proId);
        int existingCount = existingPics.size();

        // 計算可以新增的圖片數量
        int availableSlots = MAX_IMAGE_COUNT - existingCount;
        if (availableSlots <= 0) {
            throw new RuntimeException("已達到最大圖片數量限制（" + MAX_IMAGE_COUNT + "張）");
        }

        // 只處理允許數量內的圖片
        int imagesToSave = Math.min(images.size(), availableSlots);

        for (int i = 0; i < imagesToSave; i++) {
            MultipartFile image = images.get(i);
            try {
                if (image != null && !image.isEmpty()) {
                    ProductPic pic = new ProductPic();
                    pic.setProduct(product);
                    pic.setProPic(image.getBytes());
                    productPicRepository.save(pic);
                }
            } catch (Exception e) {
                System.err.println("儲存圖片失敗: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("儲存圖片失敗: " + e.getMessage(), e);
            }
        }
    }

    @Override
    @Transactional
    public void deleteProductImage(Integer productPicId) {
        if (productPicId != null) {
            try {
                productPicRepository.deleteById(productPicId);
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
    public Product saveProductWithImages(Integer sellerId, Integer proId, String proName, Integer proTypeId, Integer proPrice, String proDescription, Integer stockQuantity, Integer proState, List<MultipartFile> newImages, List<Integer> deleteImageIds) {
        System.out.println("=== ProductServiceImpl.saveProductWithImages 開始 ===");

        try {
            Product product = null;
            // 1. 判斷是新增還是編輯
            if (proId != null && proId > 0) {
                System.out.println("模式: 編輯現有商品 ID: " + proId);
                product = productRepository.findById(proId)
                        .orElseThrow(() -> new RuntimeException("商品不存在"));

                // 驗證權限
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

            // 儲存商品基本資訊
            Product savedProduct = productRepository.saveAndFlush(product);

            // 3. 執行刪除舊圖片 (如果有收到待刪除 ID)
            if (deleteImageIds != null && !deleteImageIds.isEmpty()) {
                System.out.println("執行刪除圖片，數量: " + deleteImageIds.size());
                for (Integer picId : deleteImageIds) {
                    productPicRepository.deleteById(picId);
                }
                productPicRepository.flush(); // 強制同步資料庫
            }

            // 4. 【核心改進】計算剩餘有效的圖片數量
            // 這裡使用 stream 排除資料庫中 pro_pic 為 null 的無效紀錄
            List<ProductPic> remainingPics = productPicRepository.findByProduct_ProId(savedProduct.getProId())
                    .stream()
                    .filter(p -> p.getProPic() != null && p.getProPic().length > 0)
                    .collect(java.util.stream.Collectors.toList());

            int remainingCount = remainingPics.size();
            System.out.println("剩餘有效圖片數量: " + remainingCount);

            // 5. 處理新上傳的圖片
            if (newImages != null && !newImages.isEmpty()) {
                // 過濾無效檔案
                List<MultipartFile> validImages = new java.util.ArrayList<>();
                for (MultipartFile img : newImages) {
                    if (img != null && !img.isEmpty()) {
                        validImages.add(img);
                    }
                }

                if (!validImages.isEmpty()) {
                    // MAX_IMAGE_COUNT 目前設定為 1
                    int availableSlots = MAX_IMAGE_COUNT - remainingCount;
                    System.out.println("可用位置: " + availableSlots);

                    if (availableSlots > 0) {
                        // 只儲存名額內的圖片 (以你的情況通常就是 1 張)
                        int imagesToSave = Math.min(validImages.size(), availableSlots);

                        for (int i = 0; i < imagesToSave; i++) {
                            MultipartFile image = validImages.get(i);
                            ProductPic pic = new ProductPic();
                            pic.setProduct(savedProduct);
                            pic.setProPic(image.getBytes());
                            productPicRepository.save(pic);
                            System.out.println("成功儲存新圖片");
                        }
                        productPicRepository.flush();
                    } else {
                        System.out.println("已達到圖片限制，跳過上傳。請先刪除舊圖。");
                    }
                }
            }

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