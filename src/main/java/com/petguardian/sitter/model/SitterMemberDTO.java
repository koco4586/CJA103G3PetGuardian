package com.petguardian.sitter.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for sitter member information.
 * Decouples View layer from JPA Entity.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SitterMemberDTO {
    private Integer memId;
    private String memName;
    private String memAdd;

    /**
     * Factory method: Convert from Entity
     */
    public static SitterMemberDTO fromEntity(SitterMemberVO sitterMemberVO) {
        if (sitterMemberVO == null)
            return null;
        return new SitterMemberDTO(
                sitterMemberVO.getMemId(),
                sitterMemberVO.getMemName(),
                sitterMemberVO.getMemAdd());
    }
}
