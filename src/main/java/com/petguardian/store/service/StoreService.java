package com.petguardian.store.service;

import com.petguardian.seller.model.Product;

import java.util.List;
import java.util.Optional;

public interface StoreService {
    // 建立/更新商品
    Product saveProduct(Product product);

    // 查詢單一商品
    Optional<Product> getProductById(Integer proId);

    // 查詢所有商品
    List<Product> getAllProducts();

    // 查詢上架商品 (商城用)
    List<Product> getAllActiveProducts();

    // 查詢賣家商品
    List<Product> getProductsBySeller(Integer memId);

    // 扣除庫存（結帳用）- 庫存歸零自動下架
    void deductStock(Integer proId, Integer quantity);

    // 查詢賣家的其他上架商品（排除指定商品，用於加購區）
    List<Product> getOtherActiveProductsBySeller(Integer sellerId, List<Integer> excludeProIds);
}
