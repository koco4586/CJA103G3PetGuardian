package com.petguardian.seller.service;

import com.petguardian.seller.model.ProType;
import com.petguardian.seller.model.ProTypeRepository;
import com.petguardian.seller.model.Product;
import com.petguardian.seller.model.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 商品類別管理 Service 實作
 */
@Service
public class ProTypeServiceImpl implements ProTypeService {

    @Autowired
    private ProTypeRepository proTypeRepository;

    @Autowired
    private ProductRepository productRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ProType> getAllProTypes() {
        return proTypeRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProType> getProTypeById(Integer proTypeId) {
        if (proTypeId == null) {
            return Optional.empty();
        }
        return proTypeRepository.findById(proTypeId);
    }

    @Override
    @Transactional
    public ProType addProType(String proTypeName) {
        // 檢查名稱是否為空
        if (proTypeName == null || proTypeName.trim().isEmpty()) {
            throw new RuntimeException("類別名稱不能為空");
        }

        // 檢查名稱是否已存在
        if (isProTypeNameExists(proTypeName.trim())) {
            throw new RuntimeException("類別名稱已存在");
        }

        ProType proType = new ProType();
        proType.setProTypeName(proTypeName.trim());
        return proTypeRepository.save(proType);
    }

    @Override
    @Transactional
    public ProType updateProType(Integer proTypeId, String proTypeName) {
        // 檢查名稱是否為空
        if (proTypeName == null || proTypeName.trim().isEmpty()) {
            throw new RuntimeException("類別名稱不能為空");
        }

        // 取得現有類別
        ProType proType = proTypeRepository.findById(proTypeId)
                .orElseThrow(() -> new RuntimeException("商品類別不存在"));

        // 檢查新名稱是否與其他類別重複
        List<ProType> allTypes = proTypeRepository.findAll();
        for (ProType type : allTypes) {
            if (!type.getProTypeId().equals(proTypeId)
                    && type.getProTypeName().equals(proTypeName.trim())) {
                throw new RuntimeException("類別名稱已存在");
            }
        }

        proType.setProTypeName(proTypeName.trim());
        return proTypeRepository.save(proType);
    }

    @Override
    @Transactional
    public boolean deleteProType(Integer proTypeId) {
        // 檢查類別是否存在
        Optional<ProType> proTypeOpt = proTypeRepository.findById(proTypeId);
        if (!proTypeOpt.isPresent()) {
            return false;
        }

        // 檢查是否有商品使用此類別
        if (isProTypeInUse(proTypeId)) {
            throw new RuntimeException("此類別下仍有商品，無法刪除");
        }

        proTypeRepository.deleteById(proTypeId);
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isProTypeNameExists(String proTypeName) {
        if (proTypeName == null || proTypeName.trim().isEmpty()) {
            return false;
        }

        List<ProType> allTypes = proTypeRepository.findAll();
        for (ProType type : allTypes) {
            if (type.getProTypeName().equals(proTypeName.trim())) {
                return true;
            }
        }
        return false;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isProTypeInUse(Integer proTypeId) {
        return countProductsByProType(proTypeId) > 0;
    }

    @Override
    @Transactional(readOnly = true)
    public long countProductsByProType(Integer proTypeId) {
        if (proTypeId == null) {
            return 0;
        }

        List<Product> allProducts = productRepository.findAll();
        return allProducts.stream()
                .filter(p -> p.getProType() != null && proTypeId.equals(p.getProType().getProTypeId()))
                .count();
    }
}