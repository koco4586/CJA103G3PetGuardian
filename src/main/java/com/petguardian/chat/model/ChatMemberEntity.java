package com.petguardian.chat.model;

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
 * Used by chat module to avoid modifying teammate's Member.
 */
@Entity
@Immutable
@Table(name = "member")
@Data
@NoArgsConstructor
public class ChatMemberEntity implements Serializable {

    @Id
    @Column(name = "mem_id")
    private Integer memId;

    @Column(name = "mem_name")
    private String memName;
}
