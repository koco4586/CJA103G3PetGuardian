package com.petguardian.chat.dto;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Member Profile.
 * Used for UI enrichment and caching.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberProfileDTO implements Serializable {
    private Integer memberId;
    private String memberName;
    private String memberImage;
}
