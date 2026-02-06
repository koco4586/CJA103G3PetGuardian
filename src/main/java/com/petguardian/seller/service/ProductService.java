package com.petguardian.seller.service;

import com.petguardian.seller.model.ProType;
import com.petguardian.seller.model.Product;

import java.util.List;
import java.util.Map;
import java.util.Optional;

// 商品管理 Service Interface
public interface ProductService {

    // 取得賣家的所有商品（按上架時間降序）
    List<Product> getSellerProducts(Integer memId);

    // 根據商品ID取得商品
    Optional<Product> getProductById(Integer proId);

    // 取得所有商品類別
    List<ProType> getAllProTypes();

    // 儲存商品（新增或更新）
    Product saveProduct(Product product);

    // 刪除商品
    void deleteProduct(Integer proId);

    // 賣家刪除自己的商品（會驗證權限）
    boolean deleteProductBySeller(Integer sellerId, Integer proId);

    // 取得商品的所有圖片（URL格式），回傳 List，每個 Map 包含 productPicId 和 imageUrl
    List<Map<String, Object>> getProductImages(Integer proId);

    // 取得商品主圖URL，若無圖片則回傳預設圖
    String getProductMainImage(Integer proId);

    // 儲存商品圖片URL
    void saveProductImageUrl(Integer proId, String imageUrl);

    // 刪除指定的商品圖片
    void deleteProductImage(Integer productPicId);

    // 取得賣家商品列表（含主圖URL），回傳 List，每個 Map 包含 product 和 mainImage
    List<Map<String, Object>> getSellerProductsWithImages(Integer sellerId);

    // 儲存商品（含圖片URL處理），處理新增或更新商品、儲存新圖片URL、刪除指定圖片
    Product saveProductWithImages(Integer sellerId, Integer proId, String proName,
                                  Integer proTypeId, Integer proPrice, String proDescription,
                                  Integer stockQuantity, Integer proState,
                                  String imageUrl, List<Integer> deleteImageIds);

    // 計算賣家商品總數
    long countSellerProducts(Integer sellerId);

    // 計算上架中商品數量（proState = 1）
    long countActiveProducts(Integer sellerId);
}