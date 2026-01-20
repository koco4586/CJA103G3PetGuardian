package com.petguardian.service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for service area information.
 * Decouples View layer from JPA Entity.
 * Uses IDs instead of full entity objects to avoid circular dependencies.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceAreaDTO {
    private Integer sitterId;
    private Integer areaId;

    /**
     * Factory method: Convert from Entity
     */
    public static ServiceAreaDTO fromEntity(ServiceAreaVO vo) {
        if (vo == null)
            return null;
        return new ServiceAreaDTO(
                vo.getSitter() != null ? vo.getSitter().getSitterId() : null,
                vo.getArea() != null ? vo.getArea().getAreaId() : null);
    }

    /**
     * Create composite key from this DTO
     */
    public ServiceAreaId toCompositeKey() {
        return new ServiceAreaId(this.sitterId, this.areaId);
    }
}
