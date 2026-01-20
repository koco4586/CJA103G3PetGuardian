package com.petguardian.area.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for area information.
 * Decouples View layer from JPA Entity.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AreaDTO {
    private Integer areaId;
    private String cityName;
    private String district;

    /**
     * Factory method: Convert from Entity
     */
    public static AreaDTO fromEntity(AreaVO vo) {
        if (vo == null)
            return null;
        return new AreaDTO(
                vo.getAreaId(),
                vo.getCityName(),
                vo.getDistrict());
    }

    /**
     * Convert to Entity
     */
    public AreaVO toEntity() {
        AreaVO vo = new AreaVO();
        vo.setAreaId(this.areaId);
        vo.setCityName(this.cityName);
        vo.setDistrict(this.district);
        return vo;
    }

    /**
     * Get full area name (city + district)
     */
    public String getFullName() {
        return cityName + district;
    }
}
