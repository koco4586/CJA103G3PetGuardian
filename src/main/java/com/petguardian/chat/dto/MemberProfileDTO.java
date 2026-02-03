package com.petguardian.chat.dto;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Unified DTO for Member Profile.
 * 
 * Used for:
 * UI enrichment (display name, avatar)
 * Redis caching (member metadata)
 * View layer binding (replaces legacy ChatMemberDTO)
 * 
 * Provides backward-compatible getters for smooth migration
 * from deprecated ChatMemberDTO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
public class MemberProfileDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer memberId;
    private String memberName;
    private String memberImage;

}
