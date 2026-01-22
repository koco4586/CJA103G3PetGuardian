package com.petguardian.orders.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * 結帳頁面 DTO
 * 整合主商品、賣家資訊、加購商品等所有結帳所需資料
 * Thymeleaf 模板只需透過 ${checkout.xxx} 即可存取
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutResponseDTO {

    /**
     * 主商品資訊
     */
    private ProductDisplayDTO mainProduct;

    /**
     * 主商品購買數量
     */
    private Integer mainQuantity;

    /**
     * 購物車所有商品（含主商品與加購商品）
     */
    private List<CartItemDisplayDTO> cartItems;

    /**
     * 賣家資訊（含評分統計與評價列表）
     */
    private SellerInfoDTO sellerInfo;

    /**
     * 同店加購商品列表
     */
    private List<ProductDisplayDTO> upsellProducts;

    /**
     * 訂單總金額（無運費）
     */
    private Integer orderTotal;

    /**
     * 當前會員 ID
     */
    private Integer memId;

    /**
     * 購物車商品展示 DTO（內部類別）
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CartItemDisplayDTO {
        private Integer proId;
        private Integer sellerId;
        private String proName;
        private Integer proPrice;
        private Integer quantity;
        private Integer subtotal;
        private String imageBase64;
        private Integer stockQuantity;
    }
}
