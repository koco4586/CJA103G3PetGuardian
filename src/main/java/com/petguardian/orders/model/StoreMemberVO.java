package com.petguardian.orders.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.io.Serializable;

/**
 * 會員資料 Read-only Entity
 * 僅供二手商城模組使用，避免修改其他模組的 MemberVO
 */
@Entity
@Immutable
@Table(name = "member")
@Data
@NoArgsConstructor
public class StoreMemberVO implements Serializable {

    @Id
    @Column(name = "mem_id")
    private Integer memId;

    @Column(name = "mem_name")
    private String memName;

    // 會員頭像（URL 或路徑） seller/dashboard 用
    @Column(name = "mem_image")
    private String memImage;

    // 商城評價總星星數 seller/dashboard 用
    @Column(name = "mem_shop_rating_score")
    private Integer memShopRatingScore;

    // 商城評價總數量 seller/dashboard 用
    @Column(name = "mem_shop_rating_count")
    private Integer memShopRatingCount;
}