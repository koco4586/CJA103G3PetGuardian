package com.petguardian.seller.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductPicRepository extends JpaRepository<ProductPic, Integer> {

    List<ProductPic> findByProduct_ProId(Integer proId);
}
