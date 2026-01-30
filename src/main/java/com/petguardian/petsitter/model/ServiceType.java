package com.petguardian.petsitter.model;

import java.util.Arrays;

/**
 * ServiceType (服務項目定義)
 * 用於 PetSitter 模組內部，對應資料庫 service_items 表格
 * 修正為與資料庫一致: 1:散步, 2:餵食, 3:洗澡
 */
public enum ServiceType {
    WALKING(1, "散步"),
    FEEDING(2, "餵食"),
    BATHING(3, "洗澡");

    private final int id;
    private final String label;

    ServiceType(int id, String label) {
        this.id = id;
        this.label = label;
    }

    public int getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public static ServiceType fromId(Integer id) {
        if (id == null)
            return null;
        return Arrays.stream(values())
                .filter(s -> s.id == id)
                .findFirst()
                .orElse(null);
    }
}
