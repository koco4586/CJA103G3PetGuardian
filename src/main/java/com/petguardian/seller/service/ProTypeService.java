package com.petguardian.seller.service;

import com.petguardian.seller.model.ProType;

import java.util.List;
import java.util.Optional;

/**
 * 商品類別管理 Service Interface
 */
public interface ProTypeService {

    /**
     * 取得所有商品類別
     */
    List<ProType> getAllProTypes();

    /**
     * 根據ID取得商品類別
     */
    Optional<ProType> getProTypeById(Integer proTypeId);

    /**
     * 新增商品類別
     */
    ProType addProType(String proTypeName);

    /**
     * 更新商品類別
     */
    ProType updateProType(Integer proTypeId, String proTypeName);

    /**
     * 刪除商品類別
     */
    boolean deleteProType(Integer proTypeId);

    /**
     * 檢查類別名稱是否已存在
     */
    boolean isProTypeNameExists(String proTypeName);

    /**
     * 檢查類別是否被商品使用中
     */
    boolean isProTypeInUse(Integer proTypeId);

    /**
     * 統計類別下的商品數量
     */
    long countProductsByProType(Integer proTypeId);
}