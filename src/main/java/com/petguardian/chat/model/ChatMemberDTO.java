package com.petguardian.chat.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for chat member information.
 * Decouples View layer from JPA Entity.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMemberDTO {
    private Integer memId;
    private String memName;

    /**
     * Factory method: Convert from Entity
     */
    public static ChatMemberDTO fromEntity(ChatMemberVO vo) {
        if (vo == null)
            return null;
        return new ChatMemberDTO(vo.getMemId(), vo.getMemName());
    }
}
