package com.petguardian.seller.service;

import com.petguardian.seller.model.ProType;
import com.petguardian.seller.model.Product;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 商品管理 Service Interface
 * 商品 CRUD、圖片管理、賣家商品查詢
 */
public interface ProductService {

    // 基本查詢

    /**
     * 取得賣家的所有商品（按上架時間降序）
     */
    List<Product> getSellerProducts(Integer memId);

    /**
     * 根據商品ID取得商品
     */
    Optional<Product> getProductById(Integer proId);

    /**
     * 取得所有商品類別
     */
    List<ProType> getAllProTypes();

    // 商品 CRUD

    /**
     * 儲存商品（新增或更新）
     */
    Product saveProduct(Product product);

    /**
     * 刪除商品
     */
    void deleteProduct(Integer proId);

    /**
     * 賣家刪除自己的商品（會驗證權限）
     */
    boolean deleteProductBySeller(Integer sellerId, Integer proId);

    // 圖片管理

    /**
     * 取得商品的所有圖片（Base64 格式）
     * 回傳 List，每個 Map 包含：productPicId, imageBase64
     */
    List<Map<String, Object>> getProductImages(Integer proId);

    /**
     * 取得商品主圖（第一張圖）Base64
     * 若無圖片則回傳預設圖
     */
    String getProductMainImage(Integer proId);

    /**
     * 儲存商品圖片（可多張）
     */
    void saveProductImages(Integer proId, List<MultipartFile> images);

    /**
     * 刪除指定的商品圖片
     */
    void deleteProductImage(Integer productPicId);

    // 整合查詢（給 Controller 用）

    /**
     * 取得賣家商品列表（含主圖）
     * 回傳 List，每個 Map 包含：
     * - product: Product
     * - mainImage: 主圖 Base64
     */
    List<Map<String, Object>> getSellerProductsWithImages(Integer sellerId);

    /**
     * 儲存商品（含圖片處理）
     * 處理：新增或更新商品、儲存新圖片、刪除指定圖片
     */
    Product saveProductWithImages(Integer sellerId, Integer proId, String proName,
                                  Integer proTypeId, Integer proPrice, String proDescription,
                                  Integer stockQuantity, Integer proState,
                                  List<MultipartFile> newImages, List<Integer> deleteImageIds);

    // 統計

    /**
     * 計算賣家商品總數
     */
    long countSellerProducts(Integer sellerId);

    /**
     * 計算上架中商品數量（proState = 1）
     */
    long countActiveProducts(Integer sellerId);
}