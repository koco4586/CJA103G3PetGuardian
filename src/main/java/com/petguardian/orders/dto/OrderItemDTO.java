package com.petguardian.orders.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 訂單項目 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDTO {
    private Integer proId;
    private Integer quantity;
    private Integer proPrice;
}
