package com.petguardian.store.service;

import com.petguardian.seller.model.Product;
import com.petguardian.seller.model.ProductRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class StoreServiceImpl implements StoreService {

    @Autowired
    private ProductRepository productDAO;

    // 上架狀態常數 (0:下架, 1:上架)
    public static final Integer STATE_ACTIVE = 1;
    public static final Integer STATE_INACTIVE = 0;

    @Override
    public Product saveProduct(Product product) {
        if (product.getProState() == null) {
            product.setProState(STATE_ACTIVE); // 預設上架
        }
        return productDAO.save(product);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Product> getProductById(Integer proId) {
        return productDAO.findById(proId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> getAllProducts() {
        return productDAO.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> getAllActiveProducts() {
        return productDAO.findByProState(STATE_ACTIVE);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> getProductsBySeller(Integer memId) {
        return productDAO.findByMemId(memId);
    }

    @Override
    @Transactional
    public void deductStock(Integer proId, Integer quantity) {
        if (proId == null) {
            throw new IllegalArgumentException("商品 ID 不能為 null");
        }
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("扣除數量必須大於 0");
        }

        Product product = productDAO.findById(proId)
                .orElseThrow(() -> new IllegalArgumentException("商品不存在: " + proId));

        int currentStock = product.getStockQuantity() != null ? product.getStockQuantity() : 0;

        if (currentStock < quantity) {
            throw new IllegalArgumentException(
                    "商品「" + product.getProName() + "」庫存不足，目前庫存: " + currentStock);
        }

        int newStock = currentStock - quantity;
        product.setStockQuantity(newStock);

        // 庫存歸零自動下架
        if (newStock == 0) {
            product.setProState(STATE_INACTIVE);
        }

        productDAO.save(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> getOtherActiveProductsBySeller(Integer sellerId, List<Integer> excludeProIds) {
        if (sellerId == null) {
            return List.of();
        }

        // 取得該賣家所有上架商品
        List<Product> sellerProducts = productDAO.findByMemIdAndProState(sellerId, STATE_ACTIVE);

        // 排除已在購物車中的商品
        if (excludeProIds != null && !excludeProIds.isEmpty()) {
            sellerProducts = sellerProducts.stream()
                    .filter(p -> !excludeProIds.contains(p.getProId()))
                    .toList();
        }

        return sellerProducts;
    }
}
