package com.petguardian.petsitter.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for pet sitter service pet type information.
 * Decouples View layer from JPA Entity.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PetSitterServicePetTypeDTO {
    private Integer servicePetId;
    private Integer sitterId;
    private Integer serviceItemId;
    private Integer sizeId;
    private Integer typeId;

    /**
     * Factory method: Convert from Entity
     */
    public static PetSitterServicePetTypeDTO fromEntity(PetSitterServicePetTypeVO vo) {
        if (vo == null)
            return null;
        return new PetSitterServicePetTypeDTO(
                vo.getServicePetId(),
                vo.getSitterId(),
                vo.getServiceItemId(),
                vo.getSizeId(),
                vo.getTypeId());
    }

    /**
     * Convert to Entity
     */
    public PetSitterServicePetTypeVO toEntity() {
        PetSitterServicePetTypeVO vo = new PetSitterServicePetTypeVO();
        vo.setServicePetId(this.servicePetId);
        vo.setSitterId(this.sitterId);
        vo.setServiceItemId(this.serviceItemId);
        vo.setSizeId(this.sizeId);
        vo.setTypeId(this.typeId);
        return vo;
    }
}
