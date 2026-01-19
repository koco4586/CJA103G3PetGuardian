package com.petguardian.seller.service;

import com.petguardian.seller.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProTypeRepository proTypeRepository;

    @Override
    public List<Product> getSellerProuducts(Integer memId) {
        return List.of();
    }

    @Override
    public Optional<Product> getProuductById(Integer proId) {
        return Optional.empty();
    }

    @Override
    public List<Product> getSellerProducts(Integer memId) {
        return productRepository.findByMemIdOrderByLaunchedTimeDesc(memId);
    }

    @Override
    public Optional<Product> getProductById(Integer proId) {
        return productRepository.findById(proId);
    }

    @Override
    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    @Override
    public void deleteById(Integer proid) {

    }

    @Override
    public void deleteProduct(Integer proId) {
        productRepository.deleteById(proId);
    }

    @Override
    public List<ProType> getAllProTypes() {
        return proTypeRepository.findAll();
    }
}