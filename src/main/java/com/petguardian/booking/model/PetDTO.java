package com.petguardian.booking.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PetDTO {
    private Integer petId;    // 對應 SQL 的 pet_id
    private String name;      // 寵物名字
    private String type;      // 寵物種類 (貓、狗)
}
