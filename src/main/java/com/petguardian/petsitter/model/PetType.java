package com.petguardian.petsitter.model;

import java.util.Arrays;

/**
 * PetType (寵物種類定義)
 * 用於 PetSitter 模組內部，對應資料庫 pet_type 表格
 */
public enum PetType {
    CAT(1, "貓"),
    DOG(2, "狗");

    private final int id;
    private final String label;

    PetType(int id, String label) {
        this.id = id;
        this.label = label;
    }

    public int getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    // 提供靜態方法供 Service 呼叫驗證 (例如傳入 1 回傳 CAT)
    public static PetType fromId(Integer id) {
        if (id == null)
            return null;
        return Arrays.stream(values())
                .filter(t -> t.id == id)
                .findFirst()
                .orElse(null);
    }
}
