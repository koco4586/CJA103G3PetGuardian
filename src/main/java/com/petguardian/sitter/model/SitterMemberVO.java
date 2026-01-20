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
 * Read-only entity for member table.
 * Used by sitter module to avoid modifying member module's MemberVO.
 * Decouples sitter application from member registration/login functionality.
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

    @Column(name = "mem_add")
    private String memAdd;
}
