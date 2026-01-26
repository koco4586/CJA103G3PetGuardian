package com.petguardian.petsitter.model;

import java.util.Arrays;

/**
 * PetSize (寵物體型定義)
 * 用於 PetSitter 模組內部，對應資料庫 pet_size 表格
 */
public enum PetSize {
    SMALL(1, "小型"),
    MEDIUM(2, "中型"),
    LARGE(3, "大型");

    private final int id;
    private final String label;

    PetSize(int id, String label) {
        this.id = id;
        this.label = label;
    }

    public int getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    // 提供靜態方法供 Service 呼叫驗證 (例如傳入 1 回傳 SMALL)
    public static PetSize fromId(Integer id) {
        if (id == null)
            return null;
        return Arrays.stream(values())
                .filter(s -> s.id == id)
                .findFirst()
                .orElse(null);
    }
}
