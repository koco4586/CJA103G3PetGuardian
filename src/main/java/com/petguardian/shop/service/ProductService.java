package com.petguardian.shop.service;

import com.petguardian.shop.model.ProType;
import com.petguardian.shop.model.Product;

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

}
