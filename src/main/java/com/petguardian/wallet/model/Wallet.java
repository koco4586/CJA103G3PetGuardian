package com.petguardian.wallet.model;

import com.petguardian.orders.model.StoreMemberVO;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "wallet")
@Getter
@Setter
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wallet_id")
    private Integer walletId;

    // ✨ 新增：建立與會員的關聯
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mem_id", nullable = false, unique = true, insertable = false, updatable = false)
    private StoreMemberVO member;

    @Column(name = "mem_id", nullable = false, unique = true)
    private Integer memId;

    @Column(name = "balance", nullable = false)
    private Integer balance = 0;

    // 取得會員名稱的方法
    public String getMemberName() {
        return member != null ? member.getMemName() : "未知會員";
    }
}