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
}
