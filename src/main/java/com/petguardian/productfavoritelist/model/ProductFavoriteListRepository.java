package com.petguardian.productfavoritelist.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductFavoriteListRepository extends JpaRepository<ProductFavoriteListVO, ProductFavoriteListId> {

    // 根據會員ID查詢收藏列表（依收藏時間降序）
    List<ProductFavoriteListVO> findByMemIdOrderByFavTimeDesc(Integer memId);

    // 根據商品ID查詢收藏該商品的所有會員
    List<ProductFavoriteListVO> findByProIdOrderByFavTimeDesc(Integer proId);

    // 查詢會員是否收藏某商品
    Optional<ProductFavoriteListVO> findByMemIdAndProId(Integer memId, Integer proId);

    // 檢查會員是否收藏某商品
    boolean existsByMemIdAndProId(Integer memId, Integer proId);

    // 統計會員收藏的商品數量
    Long countByMemId(Integer memId);

    // 統計商品被收藏的次數
    Long countByProId(Integer proId);

    // 刪除收藏
    void deleteByMemIdAndProId(Integer memId, Integer proId);
}
