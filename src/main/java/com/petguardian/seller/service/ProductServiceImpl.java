package com.petguardian.seller.service;

import com.petguardian.seller.model.ProType;
import com.petguardian.seller.model.ProTypeRepository;
import com.petguardian.seller.model.Product;
import com.petguardian.seller.model.ProductPic;
import com.petguardian.seller.model.ProductPicRepository;
import com.petguardian.seller.model.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProTypeRepository proTypeRepository;

    @Autowired
    private ProductPicRepository productPicRepository;

    private static final String DEFAULT_IMAGE = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8/5+hHgAHggJ/PchI7wAAAABJRU5ErkJggg==";

    @Override
    @Transactional(readOnly = true)
    public List<Product> getSellerProducts(Integer memId) {
        if (memId == null) {
            return new ArrayList<>();
        }
        return productRepository.findByMemIdOrderByLaunchedTimeDesc(memId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Product> getProductById(Integer proId) {
        if (proId == null) {
            return Optional.empty();
        }
        return productRepository.findById(proId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProType> getAllProTypes() {
        return proTypeRepository.findAll();
    }

    @Override
    @Transactional
    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    @Override
    @Transactional
    public void deleteProduct(Integer proId) {
        if (proId != null) {
            productRepository.deleteById(proId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getProductImages(Integer proId) {
        List<Map<String, Object>> result = new ArrayList<>();

        if (proId == null) {
            return result;
        }

        List<ProductPic> pics = productPicRepository.findByProduct_ProId(proId);
        for (ProductPic pic : pics) {
            if (pic.getProPic() != null && pic.getProPic().length > 0) {
                Map<String, Object> picData = new HashMap<>();
                picData.put("productPicId", pic.getProductPicId());
                picData.put("imageBase64", "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(pic.getProPic()));
                result.add(picData);
            }
        }

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public String getProductMainImage(Integer proId) {
        if (proId == null) {
            return DEFAULT_IMAGE;
        }

        List<ProductPic> pics = productPicRepository.findByProduct_ProId(proId);

        if (pics != null && !pics.isEmpty()) {
            ProductPic firstPic = pics.get(0);
            if (firstPic.getProPic() != null && firstPic.getProPic().length > 0) {
                return "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(firstPic.getProPic());
            }
        }

        return DEFAULT_IMAGE;
    }

    @Override
    @Transactional
    public void saveProductImages(Integer proId, List<MultipartFile> images) {
        if (images == null || images.isEmpty() || proId == null) {
            return;
        }

        Optional<Product> productOpt = productRepository.findById(proId);
        if (!productOpt.isPresent()) {
            throw new RuntimeException("商品不存在: " + proId);
        }
        Product product = productOpt.get();

        for (MultipartFile image : images) {
            try {
                if (image != null && !image.isEmpty()) {
                    ProductPic pic = new ProductPic();
                    pic.setProduct(product);
                    pic.setProPic(image.getBytes());
                    productPicRepository.save(pic);
                }
            } catch (Exception e) {
                System.err.println("儲存圖片失敗: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("儲存圖片失敗: " + e.getMessage(), e);
            }
        }
    }

    @Override
    @Transactional
    public void deleteProductImage(Integer productPicId) {
        if (productPicId != null) {
            try {
                productPicRepository.deleteById(productPicId);
            } catch (Exception e) {
                System.err.println("刪除圖片失敗 ID: " + productPicId + ", 錯誤: " + e.getMessage());
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getSellerProductsWithImages(Integer sellerId) {
        List<Map<String, Object>> result = new ArrayList<>();
        List<Product> products = getSellerProducts(sellerId);

        for (Product product : products) {
            Map<String, Object> productData = new HashMap<>();
            productData.put("product", product);
            productData.put("mainImage", getProductMainImage(product.getProId()));
            result.add(productData);
        }

        return result;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Product saveProductWithImages(Integer sellerId, Integer proId, String proName,
                                         Integer proTypeId, Integer proPrice, String proDescription,
                                         Integer stockQuantity, Integer proState,
                                         List<MultipartFile> newImages, List<Integer> deleteImageIds) {

        Product product = null;

        try {
            if (proId != null && proId > 0) {
                product = productRepository.findById(proId)
                        .orElseThrow(() -> new RuntimeException("商品不存在"));

                if (!product.getMemId().equals(sellerId)) {
                    throw new RuntimeException("無權限編輯此商品");
                }
            } else {
                product = new Product();
                product.setMemId(sellerId);
            }

            ProType proType = proTypeRepository.findById(proTypeId)
                    .orElseThrow(() -> new RuntimeException("商品類別不存在"));

            product.setProType(proType);
            product.setProName(proName);
            product.setProPrice(proPrice);
            product.setProDescription(proDescription);
            product.setStockQuantity(stockQuantity);
            product.setProState(proState);

            Product savedProduct = productRepository.saveAndFlush(product);
            System.out.println("商品已儲存，ID: " + savedProduct.getProId());

            if (deleteImageIds != null && !deleteImageIds.isEmpty()) {
                System.out.println("準備刪除 " + deleteImageIds.size() + " 張圖片");
                for (Integer picId : deleteImageIds) {
                    try {
                        productPicRepository.deleteById(picId);
                        System.out.println("已刪除圖片 ID: " + picId);
                    } catch (Exception e) {
                        System.err.println("刪除圖片失敗 ID: " + picId + ", 錯誤: " + e.getMessage());
                    }
                }
                productPicRepository.flush();
            }

            if (newImages != null && !newImages.isEmpty()) {
                List<MultipartFile> validImages = new ArrayList<>();
                for (MultipartFile img : newImages) {
                    if (img != null && !img.isEmpty()) {
                        validImages.add(img);
                    }
                }

                if (!validImages.isEmpty()) {
                    System.out.println("準備儲存 " + validImages.size() + " 張新圖片");

                    for (int i = 0; i < validImages.size(); i++) {
                        MultipartFile image = validImages.get(i);
                        try {
                            byte[] imageBytes = image.getBytes();
                            System.out.println("圖片 " + (i+1) + " 大小: " + imageBytes.length + " bytes");

                            if (imageBytes.length > 10 * 1024 * 1024) {
                                throw new RuntimeException("圖片太大，請選擇小於10MB的圖片");
                            }

                            ProductPic pic = new ProductPic();
                            pic.setProduct(savedProduct);
                            pic.setProPic(imageBytes);

                            ProductPic savedPic = productPicRepository.save(pic);
                            System.out.println("圖片 " + (i+1) + " 已儲存，ID: " + savedPic.getProductPicId());

                        } catch (Exception e) {
                            System.err.println("儲存圖片 " + (i+1) + " 失敗: " + e.getMessage());
                            e.printStackTrace();
                            throw new RuntimeException("儲存圖片失敗: " + e.getMessage(), e);
                        }
                    }

                    productPicRepository.flush();
                    System.out.println("所有圖片已儲存完成");
                }
            }

            return savedProduct;

        } catch (Exception e) {
            System.err.println("=== 儲存商品過程發生錯誤 ===");
            System.err.println("錯誤訊息: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("儲存商品失敗: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public boolean deleteProductBySeller(Integer sellerId, Integer proId) {
        Optional<Product> productOpt = productRepository.findById(proId);
        if (!productOpt.isPresent()) {
            return false;
        }

        Product product = productOpt.get();
        if (!product.getMemId().equals(sellerId)) {
            return false;
        }

        productRepository.deleteById(proId);
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public long countSellerProducts(Integer sellerId) {
        List<Product> products = getSellerProducts(sellerId);
        return products.size();
    }

    @Override
    @Transactional(readOnly = true)
    public long countActiveProducts(Integer sellerId) {
        List<Product> products = getSellerProducts(sellerId);
        return products.stream()
                .filter(p -> p.getProState() != null && p.getProState() == 1)
                .count();
    }

    @Override
    @Deprecated
    public List<Product> getSellerProuducts(Integer memId) {
        return getSellerProducts(memId);
    }

    @Override
    @Deprecated
    public Optional<Product> getProuductById(Integer proId) {
        return getProductById(proId);
    }

    @Override
    @Deprecated
    public void deleteById(Integer proid) {
        deleteProduct(proid);
    }
}