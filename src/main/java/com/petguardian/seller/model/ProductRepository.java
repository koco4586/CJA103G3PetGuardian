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
}
