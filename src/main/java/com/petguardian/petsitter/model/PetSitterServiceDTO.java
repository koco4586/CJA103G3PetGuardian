package com.petguardian.petsitter.model;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for pet sitter service information.
 * Decouples View layer from JPA Entity.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PetSitterServiceDTO {
    private Integer serviceItemId;
    private Integer sitterId;
    private Integer defaultPrice;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Factory method: Convert from Entity
     */
    public static PetSitterServiceDTO fromEntity(PetSitterServiceVO vo) {
        if (vo == null)
            return null;
        return new PetSitterServiceDTO(
                vo.getServiceItemId(),
                vo.getSitter() != null ? vo.getSitter().getSitterId() : null,
                vo.getDefaultPrice(),
                vo.getCreatedAt(),
                vo.getUpdatedAt());
    }

    /**
     * Create composite key from this DTO
     */
    public PetSitterServiceId toCompositeKey() {
        return new PetSitterServiceId(this.serviceItemId, this.sitterId);
    }
}
