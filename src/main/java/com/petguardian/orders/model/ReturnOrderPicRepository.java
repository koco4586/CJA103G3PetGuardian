package com.petguardian.orders.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReturnOrderPicRepository extends JpaRepository<ReturnOrderPicVO, Integer> {

    // 根據退貨單ID查詢所有圖片
    List<ReturnOrderPicVO> findByReturnOrder_ReturnId(Integer returnId);

}
