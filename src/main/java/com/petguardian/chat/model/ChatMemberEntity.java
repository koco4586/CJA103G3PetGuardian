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

    @Override
    public boolean equals(Object object) {
        if (this == object)
            return true;
        if (!(object instanceof ChatMemberEntity))
            return false;
        ChatMemberEntity that = (ChatMemberEntity) object;
        return memId != null && memId.equals(that.memId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
