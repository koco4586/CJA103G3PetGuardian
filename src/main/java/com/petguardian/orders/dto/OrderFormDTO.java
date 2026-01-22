package com.petguardian.orders.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * 結帳表單 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderFormDTO {
    private Integer sellerId;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private String specialInstructions;
    private Integer paymentMethod;
    private List<OrderItemDTO> items;
}
