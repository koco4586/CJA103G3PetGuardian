package com.petguardian.sitter.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.util.List;

import com.petguardian.booking.model.BookingOrderVO;
import com.petguardian.petsitter.model.PetSitterServiceVO;
import com.petguardian.service.model.ServiceAreaVO;

/**
 * 保姆儀表板資料传输对象 (DTO)
 * 
 * 用於封裝保姆儀表板所需的各類資訊，包含：
 * 1. 保姆基本資訊 (使用 SitterInfoDTO 解耦)
 * 2. 統計數據 (服務數、地區數、訂單數、評分)
 * 3. 詳細列表 (服務、地區、訂單)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SitterDashboardDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 保姆基本資訊
     * 使用專屬的 Inner DTO 取代原本的 SitterVO Entity。
     * 用途：與資料庫 Entity 解耦，避免直接暴露資料庫結構與敏感欄位。
     */
    private SitterInfoDTO sitterInfo;

    // 統計數據
    private int serviceCount;
    private int areaCount;
    private int pendingOrderCount;

    /**
     * 平均評分
     * 使用 Double 包裝類別以處理可能的 null 狀態
     */
    private Double averageRating;

    // 詳細列表
    private List<PetSitterServiceVO> services;
    private List<ServiceAreaVO> areas;
    private List<BookingOrderVO> pendingOrders;

    /**
     * 會員資料 (用於避免重複查詢)
     * 包含會員基本資訊，供 Controller 層使用
     */
    private SitterMemberVO member;

    /**
     * 靜態內部類別：保姆基本資訊 DTO
     * 
     * 用途：
     * 1. 僅包含前端顯示所需的欄位
     * 2. 提供與 SitterVO 的映射目標
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SitterInfoDTO implements Serializable {
        private static final long serialVersionUID = 1L;

        private Integer sitterId;
        private Integer memId;
        private String sitterName;
        private String sitterAdd;

        /** 會員大頭貼 (Base64 or Path) */
        private String memImage;

        /** 保姆狀態 (0:啟用, 1:停權) */
        private Byte sitterStatus;

        /** 服務時間排程字串 */
        private String serviceTime;

        private Integer ratingCount;
        private Integer starCount;
    }
}
