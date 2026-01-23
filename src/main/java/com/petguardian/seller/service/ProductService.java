package com.petguardian.seller.service;

import com.petguardian.seller.model.ProType;
import com.petguardian.seller.model.Product;
import com.petguardian.wallet.model.Wallet;

import java.util.List;
import java.util.Optional;

public interface ProductService {
    List<Product> getSellerProuducts(Integer memId);
    Optional<Product> getProuductById(Integer proId);

    List<Product> getSellerProducts(Integer memId);

    Optional<Product> getProductById(Integer proId);

    Product saveProduct(Product product);
    void deleteById(Integer proid);

    void deleteProduct(Integer proId);

    List<ProType> getAllProTypes();

    Product getProduct(Integer proId);

    interface WalletService {

        // 根據會員 ID 查詢錢包
        Optional<Wallet> getWalletByMemId(Integer memId);

        // 增加餘額（撥款）
        void addBalance(Integer memId, Integer amount);

        // 扣除餘額（付款）
        void deductBalance(Integer memId, Integer amount);

        // 建立錢包
        Wallet createWallet(Integer memId);
    }
}
