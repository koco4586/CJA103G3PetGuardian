package com.petguardian.sitter.model;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for sitter information.
 * Decouples View layer from JPA Entity.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SitterDTO {
    private Integer sitterId;
    private Integer memId;
    private String sitterName;
    private String sitterAdd;
    private LocalDateTime sitterCreatedAt;
    private Byte sitterStatus;
    private String serviceTime;
    private Integer sitterRatingCount;
    private Integer sitterStarCount;

    /**
     * Factory method: Convert from Entity
     */
    public static SitterDTO fromEntity(SitterVO vo) {
        if (vo == null)
            return null;
        return new SitterDTO(
                vo.getSitterId(),
                vo.getMemId(),
                vo.getSitterName(),
                vo.getSitterAdd(),
                vo.getSitterCreatedAt(),
                vo.getSitterStatus(),
                vo.getServiceTime(),
                vo.getSitterRatingCount(),
                vo.getSitterStarCount());
    }

    /**
     * Convert to Entity
     */
    public SitterVO toEntity() {
        SitterVO vo = new SitterVO();
        vo.setSitterId(this.sitterId);
        vo.setMemId(this.memId);
        vo.setSitterName(this.sitterName);
        vo.setSitterAdd(this.sitterAdd);
        vo.setSitterCreatedAt(this.sitterCreatedAt);
        vo.setSitterStatus(this.sitterStatus);
        vo.setServiceTime(this.serviceTime);
        vo.setSitterRatingCount(this.sitterRatingCount);
        vo.setSitterStarCount(this.sitterStarCount);
        return vo;
    }
}
