package com.petguardian.productfavoritelist.service;

import com.petguardian.seller.model.ProductRepository;
import com.petguardian.seller.model.Product;
import com.petguardian.seller.model.ProductPicRepository;
import com.petguardian.seller.model.ProductPic;
import com.petguardian.productfavoritelist.model.ProductFavoriteListRepository;
import com.petguardian.productfavoritelist.model.ProductFavoriteListVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductFavoriteListServiceImpl implements ProductFavoriteListService {

    @Autowired
    private ProductFavoriteListRepository favoriteListDAO;

    @Autowired
    private ProductRepository productDAO;

    @Autowired
    private ProductPicRepository productPicDAO;

    @Override
    public ProductFavoriteListVO addFavorite(Integer memId, Integer proId) {
        if (memId == null) {
            throw new IllegalArgumentException("會員ID不能為 null");
        }
        if (proId == null) {
            throw new IllegalArgumentException("商品ID不能為 null");
        }

        // 檢查是否已收藏
        if (favoriteListDAO.existsByMemIdAndProId(memId, proId)) {
            throw new IllegalArgumentException("已收藏此商品");
        }

        // 新增收藏
        ProductFavoriteListVO favorite = new ProductFavoriteListVO();
        favorite.setMemId(memId);
        favorite.setProId(proId);

        return favoriteListDAO.save(favorite);
    }

    @Override
    public void removeFavorite(Integer memId, Integer proId) {
        if (memId == null) {
            throw new IllegalArgumentException("會員ID不能為 null");
        }
        if (proId == null) {
            throw new IllegalArgumentException("商品ID不能為 null");
        }

        // 檢查是否已收藏
        if (!favoriteListDAO.existsByMemIdAndProId(memId, proId)) {
            throw new IllegalArgumentException("尚未收藏此商品");
        }

        // 取消收藏
        favoriteListDAO.deleteByMemIdAndProId(memId, proId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductFavoriteListVO> getFavoritesByMemberId(Integer memId) {
        if (memId == null) {
            throw new IllegalArgumentException("會員ID不能為 null");
        }
        return favoriteListDAO.findByMemIdOrderByFavTimeDesc(memId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductFavoriteListVO> getFavoritesByProductId(Integer proId) {
        if (proId == null) {
            throw new IllegalArgumentException("商品ID不能為 null");
        }
        return favoriteListDAO.findByProIdOrderByFavTimeDesc(proId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isFavorited(Integer memId, Integer proId) {
        if (memId == null || proId == null) {
            throw new IllegalArgumentException("會員ID和商品ID不能為 null");
        }
        return favoriteListDAO.existsByMemIdAndProId(memId, proId);
    }

    @Override
    public Map<String, Object> toggleFavorite(Integer memId, Integer proId) {
        if (memId == null) {
            throw new IllegalArgumentException("會員ID不能為 null");
        }
        if (proId == null) {
            throw new IllegalArgumentException("商品ID不能為 null");
        }

        Map<String, Object> result = new HashMap<>();
        boolean isFavorited = favoriteListDAO.existsByMemIdAndProId(memId, proId);

        if (isFavorited) {
            // 已收藏 -> 取消收藏
            favoriteListDAO.deleteByMemIdAndProId(memId, proId);
            result.put("action", "removed");
            result.put("message", "已取消收藏");
            result.put("isFavorited", false);
        } else {
            // 未收藏 -> 新增收藏
            ProductFavoriteListVO favorite = new ProductFavoriteListVO();
            favorite.setMemId(memId);
            favorite.setProId(proId);
            ProductFavoriteListVO saved = favoriteListDAO.save(favorite);

            result.put("action", "added");
            result.put("message", "已加入收藏");
            result.put("isFavorited", true);
            result.put("data", saved);
        }

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Long countMemberFavorites(Integer memId) {
        if (memId == null) {
            throw new IllegalArgumentException("會員ID不能為 null");
        }
        return favoriteListDAO.countByMemId(memId);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countProductFavorites(Integer proId) {
        if (proId == null) {
            throw new IllegalArgumentException("商品ID不能為 null");
        }
        return favoriteListDAO.countByProId(proId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getFavoritesWithProductInfo(Integer memId) {
        if (memId == null) {
            throw new IllegalArgumentException("會員ID不能為 null");
        }

        List<ProductFavoriteListVO> favorites = favoriteListDAO.findByMemIdOrderByFavTimeDesc(memId);
        List<Map<String, Object>> result = new ArrayList<>();

        for (ProductFavoriteListVO fav : favorites) {
            Map<String, Object> favData = new HashMap<>();
            favData.put("memId", fav.getMemId());
            favData.put("proId", fav.getProId());
            favData.put("favTime", fav.getFavTime());

            // 從 product 表取得商品資訊
            Product product = productDAO.findById(fav.getProId()).orElse(null);
            if (product != null) {
                favData.put("productTitle", product.getProName());
                favData.put("productPrice", product.getProPrice());
                favData.put("stockQuantity", product.getStockQuantity());

                // 取得商品圖片並轉為 Base64
                String base64Image = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8/5+hHgAHggJ/PchI7wAAAABJRU5ErkJggg=="; // 預設圖片
                List<ProductPic> pics = productPicDAO.findByProduct_ProId(product.getProId());
                if (!pics.isEmpty() && pics.get(0).getProPic() != null) {
                    base64Image = "data:image/jpeg;base64,"
                            + Base64.getEncoder().encodeToString(pics.get(0).getProPic());
                }
                favData.put("productImg", base64Image);

                // proState: 0=待售, 1=已售出, 2=下架
                String status;
                if (product.getProState() == 1) {
                    status = "已售出";
                } else if (product.getProState() == 2) {
                    status = "已下架";
                } else {
                    status = "販售中";
                }
                favData.put("productStatus", status);
            } else {
                favData.put("productTitle", "商品已下架");
                favData.put("productPrice", 0);
                favData.put("productImg",
                        "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8/5+hHgAHggJ/PchI7wAAAABJRU5ErkJggg==");
                favData.put("productStatus", "已下架");
            }

            result.add(favData);
        }

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Integer> getFavoriteProductIds(Integer memId) {
        if (memId == null) {
            return Collections.emptySet();
        }

        List<ProductFavoriteListVO> favorites = favoriteListDAO.findByMemIdOrderByFavTimeDesc(memId);
        return favorites.stream()
                .map(ProductFavoriteListVO::getProId)
                .collect(Collectors.toSet());
    }
}
