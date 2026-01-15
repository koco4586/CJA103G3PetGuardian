package com.product.productpic;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductPicDAO extends JpaRepository<ProductPicVO, Integer> {
    List<ProductPicVO> findByProId(Integer proId);
}
