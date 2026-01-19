package com.petguardian.orders.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 購物車項目 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {
    private Integer proId;
    private Integer sellerId;
    private String proName;
    private Integer proPrice;
    private Integer quantity;

    /**
     * 計算小計金額
     */
    public Integer getSubtotal() {
        if (proPrice == null || quantity == null) {
            return 0;
        }
        return proPrice * quantity;
    }
}
