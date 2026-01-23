package com.petguardian.booking.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SitterDTO {
    private Integer sitterId;      // 對應 SQL 的 sitter_id
    private String sitterName;     // 保母名稱
    private Integer serviceItemId; // 服務項目編號
    private Integer price;         // 每小時單價 (用來算 reservation_fee)
    
}
