package com.petguardian.sitter.model;

import java.io.Serializable;

import org.hibernate.annotations.Immutable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 會員資料唯讀 Entity (Sitter模組專用)
 * 對應資料表: member (唯讀)
 * 
 * 用於保姆模組查詢會員基本資料，避免直接依賴會員模組的 MemberVO
 * 實現與會員註冊/登入功能的解耦
 */
@Entity
@Immutable
@Table(name = "member")
@Data
@NoArgsConstructor
public class SitterMemberVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "mem_id")
    private Integer memId;

    @Column(name = "mem_name")
    private String memName;

    @Column(name = "mem_tel")
    private String memTel;

    @Column(name = "mem_email")
    private String memEmail;

    @Column(name = "mem_add")
    private String memAdd;

    @Column(name = "mem_image")
    private String memImage;
    
    @Column(name = "mem_status")
    private Integer memStatus;
}
