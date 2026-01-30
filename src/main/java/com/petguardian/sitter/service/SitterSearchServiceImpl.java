package com.petguardian.sitter.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.petguardian.area.model.AreaRepository;
import com.petguardian.area.model.AreaVO;
import com.petguardian.petsitter.model.PetSitterServicePetTypeRepository;
import com.petguardian.petsitter.model.PetSitterServicePetTypeVO;
import com.petguardian.petsitter.model.PetSitterServiceRepository;
import com.petguardian.petsitter.model.PetSitterServiceVO;
import com.petguardian.service.model.ServiceAreaVO;
import com.petguardian.sitter.model.SitterRepository;
import com.petguardian.sitter.model.SitterSearchCriteria;
import com.petguardian.sitter.model.SitterSearchDTO;
import com.petguardian.sitter.model.SitterVO;

@Service
public class SitterSearchServiceImpl implements SitterSearchService {

    @Autowired
    private SitterRepository repository;

    @Autowired
    private AreaRepository areaRepository;

    @Autowired
    private PetSitterServiceRepository petSitterServiceRepository;

    @Autowired
    private PetSitterServicePetTypeRepository petSitterServicePetTypeRepository;

    /**
     * 根據條件搜尋保姆
     * 
     * @param criteria 搜尋條件（地區、服務項目、寵物類型、價格範圍、排序方式）
     * @return List<SitterSearchDTO> 符合條件的保姆列表
     */
    @Override
    @Transactional(readOnly = true)
    public List<SitterSearchDTO> searchSitters(SitterSearchCriteria criteria) {
        // 1. 根據地區篩選取得保姆列表
        List<SitterVO> sitters;

        // 處理地區篩選邏輯（使用縣市和區域名稱）
        if (criteria.getCityName() != null && !criteria.getCityName().isEmpty()) {
            List<Integer> areaIds = new ArrayList<>();

            // 情況 1: 只選擇縣市（沒有選擇區域）
            if (criteria.getDistrict() == null || criteria.getDistrict().isEmpty()) {
                // 查詢該縣市的所有區域
                List<AreaVO> areas = areaRepository.findByCityName(criteria.getCityName());
                for (AreaVO area : areas) {
                    areaIds.add(area.getAreaId());
                }
            }
            // 情況 2: 選擇縣市 + 區域
            else {
                // 查詢該縣市+區域對應的單一 area_id
                AreaVO area = areaRepository.findByCityNameAndDistrict(
                        criteria.getCityName(),
                        criteria.getDistrict());
                if (area != null) {
                    areaIds.add(area.getAreaId());
                }
            }

            // 如果找到對應的 area_id，進行搜尋
            if (!areaIds.isEmpty()) {
                sitters = repository.findByServiceAreas(areaIds, (byte) 0);
            } else {
                // 找不到對應地區，返回空列表
                return new ArrayList<>();
            }
        }
        // 原有邏輯：使用 areaIds 直接篩選（保留向後相容）
        else if (criteria.getAreaIds() != null && !criteria.getAreaIds().isEmpty()) {
            sitters = repository.findByServiceAreas(criteria.getAreaIds(), (byte) 0);
        }
        // 無地區篩選，取得所有啟用保姆
        else {
            sitters = repository.findBySitterStatusOrderBySitterRatingCountDesc((byte) 0);
        }

        // 2. 將 SitterVO 轉換為 SitterSearchDTO，並進行進一步篩選
        List<SitterSearchDTO> results = new ArrayList<>();

        for (SitterVO sitter : sitters) {
            SitterSearchDTO dto = convertToSearchDTO(sitter);

            // 3. 根據服務項目篩選
            if (criteria.getServiceItemIds() != null && !criteria.getServiceItemIds().isEmpty()) {
                boolean hasMatchingService = dto.getServiceNames() != null &&
                        dto.getServiceNames().stream()
                                .anyMatch(serviceName -> criteria.getServiceItemIds()
                                        .contains(getServiceIdByName(serviceName)));
                if (!hasMatchingService) {
                    continue; // 不符合，跳過
                }
            }

            // 4. 根據寵物類型篩選
            if (criteria.getPetTypeIds() != null && !criteria.getPetTypeIds().isEmpty()) {
                boolean hasMatchingPetType = dto.getPetTypes() != null &&
                        dto.getPetTypes().stream()
                                .anyMatch(petType -> criteria.getPetTypeIds().contains(getPetTypeIdByName(petType)));
                if (!hasMatchingPetType) {
                    continue; // 不符合，跳過
                }
            }

            // 5. 根據價格範圍篩選
            if (criteria.getMinPrice() != null && dto.getMinPrice() != null) {
                if (dto.getMinPrice() < criteria.getMinPrice()) {
                    continue; // 最低價低於篩選條件，跳過
                }
            }
            if (criteria.getMaxPrice() != null && dto.getMaxPrice() != null) {
                if (dto.getMaxPrice() > criteria.getMaxPrice()) {
                    continue; // 最高價高於篩選條件，跳過
                }
            }

            results.add(dto);
        }

        // 6. 排序
        sortResults(results, criteria.getSortBy());

        return results;
    }

    /**
     * 取得所有啟用中的保姆（用於無篩選條件時）
     * 
     * @return List<SitterSearchDTO> 所有啟用中的保姆列表
     */
    @Override
    @Transactional(readOnly = true)
    public List<SitterSearchDTO> getAllActiveSitters() {
        List<SitterVO> sitters = repository.findBySitterStatusOrderBySitterRatingCountDesc((byte) 0);
        List<SitterSearchDTO> results = new ArrayList<>();

        for (SitterVO sitter : sitters) {
            results.add(convertToSearchDTO(sitter));
        }

        return results;
    }

    // ========== 私有輔助方法 ==========

    /**
     * 將 SitterVO 轉換為 SitterSearchDTO
     */
    private SitterSearchDTO convertToSearchDTO(SitterVO sitter) {
        SitterSearchDTO dto = new SitterSearchDTO();

        // 保姆基本資訊
        dto.setSitterId(sitter.getSitterId());
        dto.setSitterName(sitter.getSitterName());
        dto.setSitterAdd(sitter.getSitterAdd());

        // 評價資訊
        dto.setRatingCount(sitter.getSitterRatingCount());
        dto.setStarCount(sitter.getSitterStarCount());

        // 計算平均評分
        if (sitter.getSitterRatingCount() != null && sitter.getSitterRatingCount() > 0) {
            double avgRating = (double) sitter.getSitterStarCount() / sitter.getSitterRatingCount();
            dto.setAverageRating(avgRating);
        } else {
            dto.setAverageRating(0.0);
        }

        // [Modified] 查詢服務項目與價格
        List<String> serviceNames = new ArrayList<>();
        Integer minPrice = Integer.MAX_VALUE;
        Integer maxPrice = 0;

        List<PetSitterServiceVO> services = petSitterServiceRepository.findBySitter_SitterId(sitter.getSitterId());
        for (PetSitterServiceVO svc : services) {
            // 轉換 ID 為名稱 (Hardcoded Mapping, V18 Schema 相容)
            String name = getServiceNameById(svc.getServiceItemId());
            if (name != null) {
                serviceNames.add(name);
            }
            if (svc.getDefaultPrice() != null) {
                if (svc.getDefaultPrice() < minPrice)
                    minPrice = svc.getDefaultPrice();
                if (svc.getDefaultPrice() > maxPrice)
                    maxPrice = svc.getDefaultPrice();
            }
        }

        dto.setServiceNames(serviceNames);
        dto.setMinPrice(minPrice == Integer.MAX_VALUE ? 0 : minPrice);
        dto.setMaxPrice(maxPrice);

        // [Modified] 查詢寵物類型
        List<String> petTypes = new ArrayList<>();
        List<PetSitterServicePetTypeVO> types = petSitterServicePetTypeRepository.findBySitterId(sitter.getSitterId());
        for (PetSitterServicePetTypeVO type : types) {
            String typeName = getPetTypeNameById(type.getTypeId());
            if (typeName != null && !petTypes.contains(typeName)) {
                petTypes.add(typeName);
            }
        }
        dto.setPetTypes(petTypes);

        // 填入服務地區
        if (sitter.getServiceAreas() != null && !sitter.getServiceAreas().isEmpty()) {
            java.util.List<String> areas = new ArrayList<>();
            for (ServiceAreaVO sa : sitter.getServiceAreas()) {
                if (sa.getArea() != null) {
                    areas.add(sa.getArea().getCityName() + sa.getArea().getDistrict());
                }
            }
            dto.setServiceAreas(areas);
        } else {
            dto.setServiceAreas(new ArrayList<>());
        }

        return dto;
    }

    /**
     * 根據排序條件排序結果
     */
    private void sortResults(List<SitterSearchDTO> results, String sortBy) {
        if (sortBy == null) {
            return;

        }

        switch (sortBy) {
            case "price_asc":
                results.sort((a, b) -> {
                    if (a.getMinPrice() == null)
                        return 1;
                    if (b.getMinPrice() == null)
                        return -1;
                    return a.getMinPrice().compareTo(b.getMinPrice());
                });

                break;
            case "price_desc":
                results.sort((a, b) -> {
                    if (a.getMaxPrice() == null)
                        return 1;
                    if (b.getMaxPrice() == null)
                        return -1;
                    return b.getMaxPrice().compareTo(a.getMaxPrice());
                });
                break;
            case "rating_desc":
            default:
                results.sort((a, b) -> {
                    if (a.getAverageRating() == null)
                        return 1;
                    if (b.getAverageRating() == null)
                        return -1;
                    return b.getAverageRating().compareTo(a.getAverageRating());
                });
                break;
        }
    }

    // [New] Hardcoded Helper Methods (To Replace Missing Tables)

    // [Refactored] Use Enums instead of Hardcoded Helpers

    private Integer getServiceIdByName(String serviceName) {
        if (serviceName == null)
            return null;
        return java.util.Arrays.stream(com.petguardian.petsitter.model.ServiceType.values())
                .filter(s -> s.getLabel().equals(serviceName))
                .findFirst()
                .map(com.petguardian.petsitter.model.ServiceType::getId)
                .orElse(null);
    }

    private String getServiceNameById(Integer id) {
        com.petguardian.petsitter.model.ServiceType type = com.petguardian.petsitter.model.ServiceType.fromId(id);
        return (type != null) ? type.getLabel() : null;
    }

    private Integer getPetTypeIdByName(String petTypeName) {
        if (petTypeName == null)
            return null;
        return java.util.Arrays.stream(com.petguardian.petsitter.model.PetType.values())
                .filter(t -> t.getLabel().equals(petTypeName))
                .findFirst()
                .map(com.petguardian.petsitter.model.PetType::getId)
                .orElse(null);
    }

    private String getPetTypeNameById(Integer id) {
        com.petguardian.petsitter.model.PetType type = com.petguardian.petsitter.model.PetType.fromId(id);
        return (type != null) ? type.getLabel() : null;
    }
}
