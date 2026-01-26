package com.petguardian.sitter.model;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for sitter application information.
 * Decouples View layer from JPA Entity.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SitterApplicationDTO {
    private Integer appId;
    private Integer memId;
    private Integer sitterId;
    private String appIntro;
    private String appExperience;
    private Byte appStatus;
    private LocalDateTime appReviewAt;
    private String appReviewNote;
    private LocalDateTime appCreatedAt;

    /**
     * Factory method: Convert from Entity
     */
    public static SitterApplicationDTO fromEntity(SitterApplicationVO vo) {
        if (vo == null)
            return null;
        return new SitterApplicationDTO(
                vo.getAppId(),
                vo.getMemId(),
                vo.getSitterId(),
                vo.getAppIntro(),
                vo.getAppExperience(),
                vo.getAppStatus(),
                vo.getAppReviewAt(),
                vo.getAppReviewNote(),
                vo.getAppCreatedAt());
    }

    /**
     * Convert to Entity
     */
    public SitterApplicationVO toEntity() {
        SitterApplicationVO vo = new SitterApplicationVO();
        vo.setAppId(this.appId);
        vo.setMemId(this.memId);
        vo.setSitterId(this.sitterId);
        vo.setAppIntro(this.appIntro);
        vo.setAppExperience(this.appExperience);
        vo.setAppStatus(this.appStatus);
        vo.setAppReviewAt(this.appReviewAt);
        vo.setAppReviewNote(this.appReviewNote);
        vo.setAppCreatedAt(this.appCreatedAt);
        return vo;
    }
}
