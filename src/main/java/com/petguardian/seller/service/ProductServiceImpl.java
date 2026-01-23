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
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 商品管理 Service 實作
 */
@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProTypeRepository proTypeRepository;

    @Autowired
    private ProductPicRepository productPicRepository;

    // 預設圖片（1x1 灰色像素）
    private static final String DEFAULT_IMAGE = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8/5+hHgAHggJ/PchI7wAAAABJRU5ErkJggg==";

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
    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    @Override
    public void deleteProduct(Integer proId) {
        if (proId != null) {
            productRepository.deleteById(proId);
        }
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
            Map<String, Object> picData = new HashMap<>();
            picData.put("productPicId", pic.getProductPicId());

            // 轉成 Base64
            if (pic.getProPic() != null) {
                String base64 = "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(pic.getProPic());
                picData.put("imageBase64", base64);
            } else {
                picData.put("imageBase64", DEFAULT_IMAGE);
            }

            result.add(picData);
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
            if (firstPic.getProPic() != null) {
                return "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(firstPic.getProPic());
            }
        }

        return DEFAULT_IMAGE;
    }

    @Override
    public void saveProductImages(Integer proId, List<MultipartFile> images) {
        if (images == null || images.isEmpty() || proId == null) {
            return;
        }

        // 取得商品
        Optional<Product> productOpt = productRepository.findById(proId);
        if (!productOpt.isPresent()) {
            return;
        }
        Product product = productOpt.get();

        // 儲存每張圖片
        for (MultipartFile image : images) {
            try {
                if (image != null && !image.isEmpty()) {
                    ProductPic pic = new ProductPic();
                    pic.setProduct(product);
                    pic.setProPic(image.getBytes());
                    productPicRepository.save(pic);
                }
            } catch (Exception e) {
                System.err.println("儲存圖片失敗: " + e.getMessage());
            }
        }
    }

    @Override
    public void deleteProductImage(Integer productPicId) {
        if (productPicId != null) {
            productPicRepository.deleteById(productPicId);
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
    public Product saveProductWithImages(Integer sellerId, Integer proId, String proName,
                                         Integer proTypeId, Integer proPrice, String proDescription,
                                         Integer stockQuantity, Integer proState,
                                         List<MultipartFile> newImages, List<Integer> deleteImageIds) {

        Product product;

        if (proId != null && proId > 0) {
            // 編輯現有商品
            product = productRepository.findById(proId)
                    .orElseThrow(() -> new RuntimeException("商品不存在"));

            // 驗證商品是否屬於當前賣家
            if (!product.getMemId().equals(sellerId)) {
                throw new RuntimeException("無權限編輯此商品");
            }
        } else {
            // 新增商品
            product = new Product();
            product.setMemId(sellerId);
        }

        // 設定商品類別
        ProType proType = proTypeRepository.findById(proTypeId)
                .orElseThrow(() -> new RuntimeException("商品類別不存在"));

        product.setProType(proType);
        product.setProTypeId(proTypeId);
        product.setProName(proName);
        product.setProPrice(proPrice);
        product.setProDescription(proDescription);
        product.setStockQuantity(stockQuantity);
        product.setProState(proState);

        // 儲存商品
        Product savedProduct = productRepository.save(product);

        // 刪除指定的圖片
        if (deleteImageIds != null && !deleteImageIds.isEmpty()) {
            for (Integer picId : deleteImageIds) {
                deleteProductImage(picId);
            }
        }

        // 儲存新圖片
        if (newImages != null && !newImages.isEmpty()) {
            // 過濾空檔案
            List<MultipartFile> validImages = new ArrayList<>();
            for (MultipartFile img : newImages) {
                if (img != null && !img.isEmpty()) {
                    validImages.add(img);
                }
            }
            if (!validImages.isEmpty()) {
                saveProductImages(savedProduct.getProId(), validImages);
            }
        }

        return savedProduct;
    }

    @Override
    public boolean deleteProductBySeller(Integer sellerId, Integer proId) {
        // 驗證商品是否屬於當前賣家
        Optional<Product> productOpt = productRepository.findById(proId);
        if (!productOpt.isPresent()) {
            return false;
        }

        Product product = productOpt.get();
        if (!product.getMemId().equals(sellerId)) {
            return false;
        }

        // 刪除商品（圖片會因為級聯刪除而一併刪除，或需手動處理）
        productRepository.deleteById(proId);
        return true;
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

    // ==================== 保留 ====================

    @Override
    @Deprecated
    public List<Product> getSellerProuducts(Integer memId) {
        return getSellerProducts(memId);
    }

    @Override
    @Deprecated
    public Optional<Product> getProuductById(Integer proId) {
        return getProductById(proId);
    }

    @Override
    @Deprecated
    public void deleteById(Integer proid) {
        deleteProduct(proid);
    }
}