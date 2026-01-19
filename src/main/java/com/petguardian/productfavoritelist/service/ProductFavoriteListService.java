package com.petguardian.productfavoritelist.service;

import com.petguardian.productfavoritelist.model.ProductFavoriteListVO;

import java.util.List;
import java.util.Map;

public interface ProductFavoriteListService {

    /**
     * 新增收藏
     */
    ProductFavoriteListVO addFavorite(Integer memId, Integer proId);

    /**
     * 取消收藏
     */
    void removeFavorite(Integer memId, Integer proId);

    /**
     * 查詢會員的收藏列表
     */
    List<ProductFavoriteListVO> getFavoritesByMemberId(Integer memId);

    /**
     * 查詢商品的收藏者列表
     */
    List<ProductFavoriteListVO> getFavoritesByProductId(Integer proId);

    /**
     * 檢查是否已收藏
     */
    boolean isFavorited(Integer memId, Integer proId);

    /**
     * 切換收藏狀態（收藏/取消收藏）
     */
    Map<String, Object> toggleFavorite(Integer memId, Integer proId);

    /**
     * 統計會員收藏數量
     */
    Long countMemberFavorites(Integer memId);

    /**
     * 統計商品被收藏次數
     */
    Long countProductFavorites(Integer proId);

    /**
     * 查詢會員的收藏列表（包含商品詳細資訊）
     */
    List<Map<String, Object>> getFavoritesWithProductInfo(Integer memId);

    /**
     * 取得會員收藏的商品 ID 集合（用於商城頁面標示收藏狀態）
     */
    java.util.Set<Integer> getFavoriteProductIds(Integer memId);
}
