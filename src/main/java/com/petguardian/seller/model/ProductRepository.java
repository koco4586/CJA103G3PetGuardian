package com.petguardian.seller.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {

    // 查詢賣家的所有商品
    List<Product> findByMemIdOrderByLaunchedTimeDesc(Integer memId);

    // 查詢賣家上架中的商品
    List<Product> findByMemIdAndProStateOrderByLaunchedTimeDesc(Integer memId, Integer proState);

    // 查詢特定賣家(會員)的商品 (給賣家後台用)
    List<Product> findByMemId(Integer memId);

    // 查詢上架中的商品 (給前台商城用)
    List<Product> findByProState(Integer proState);

    // 查詢特定賣家的上架商品 (給加購區用)
    List<Product> findByMemIdAndProState(Integer memId, Integer proState);
}
