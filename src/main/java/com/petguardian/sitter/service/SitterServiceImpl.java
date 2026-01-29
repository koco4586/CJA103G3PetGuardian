package com.petguardian.sitter.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.petguardian.member.repository.register.MemberRegisterRepository;
import com.petguardian.sitter.model.SitterRepository;
import com.petguardian.sitter.model.SitterVO;
import com.petguardian.sitter.model.SitterSearchCriteria;
import com.petguardian.sitter.model.SitterSearchDTO;
import com.petguardian.sitter.model.SitterMemberRepository;
import com.petguardian.sitter.model.SitterMemberVO;
import com.petguardian.booking.model.BookingScheduleVO;
import com.petguardian.booking.model.BookingScheduleRepository;
// [NEW] Repositories for search
import com.petguardian.petsitter.model.PetSitterServiceRepository;
import com.petguardian.petsitter.model.PetSitterServiceVO;
import com.petguardian.petsitter.model.PetSitterServicePetTypeRepository;
import com.petguardian.petsitter.model.PetSitterServicePetTypeVO;
import java.time.LocalDate;
import java.util.stream.Collectors;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import com.petguardian.service.model.ServiceAreaVO;
import com.petguardian.area.model.AreaVO;

/**
 * 保姆業務邏輯實作
 * 
 * 提供保姆資料管理、保姆搜尋等功能的實作
 */
@Service("sitterService")
public class SitterServiceImpl implements SitterService {

    @Autowired
    private SitterRepository repository;

    @Autowired
    private MemberRegisterRepository memberRepository;

    @Autowired
    private com.petguardian.area.model.AreaRepository areaRepository;

    @Autowired
    private BookingScheduleRepository bookingScheduleRepository;

    @Autowired
    private SitterMemberRepository sitterMemberRepository;

    // [NEW] 注入服務與寵物類型 Repository
    @Autowired
    private PetSitterServiceRepository petSitterServiceRepository;

    @Autowired
    private PetSitterServicePetTypeRepository petSitterServicePetTypeRepository;

    /**
     * 建立保姆資料
     * 
     * @param memId      會員編號
     * @param sitterName 保姆姓名
     * @param sitterAdd  服務地址
     * @return SitterVO 新增的保姆物件
     * @throws IllegalArgumentException 若會員不存在
     * @throws IllegalStateException    若該會員已是保姆
     */
    @Override
    @Transactional
    public SitterVO createSitter(Integer memId, String sitterName, String sitterAdd) {
        // 1. 驗證會員是否存在
        if (!memberRepository.existsById(memId)) {
            throw new IllegalArgumentException("會員不存在: " + memId);
        }

        // 2. 檢查是否已是保姆
        SitterVO existing = repository.findByMemId(memId);
        if (existing != null) {
            throw new IllegalStateException("該會員已是保姆,無法重複建立");
        }

        // 3. 建立保姆資料
        SitterVO vo = new SitterVO();
        vo.setMemId(memId);
        vo.setSitterName(sitterName);
        vo.setSitterAdd(sitterAdd);
        // sitterCreatedAt, sitterStatus, serviceTime, sitterRatingCount,
        // sitterStarCount 由資料庫預設值處理
        return repository.save(vo);
    }

    /**
     * 依會員編號查詢保姆
     * 
     * @param memId 會員編號
     * @return SitterVO 該會員的保姆資料,若不存在則返回 null
     */
    @Override
    public SitterVO getSitterByMemId(Integer memId) {
        return repository.findByMemId(memId);
    }

    /**
     * 依保姆編號查詢
     * 
     * @param sitterId 保姆編號
     * @return SitterVO 保姆資料,若不存在則返回 null
     */
    @Override
    public SitterVO getSitterById(Integer sitterId) {
        return repository.findById(sitterId).orElse(null);
    }

    /**
     * 查詢所有保姆
     * 
     * @return List<SitterVO> 所有保姆列表
     */
    @Override
    public List<SitterVO> getAllSitters() {
        return repository.findAll();
    }

    /**
     * 依狀態查詢保姆
     * 
     * @param status 保姆狀態 (0:啟用, 1:停用)
     * @return List<SitterVO> 符合狀態的保姆列表
     */
    @Override
    public List<SitterVO> getSittersByStatus(Byte status) {
        return repository.findBySitterStatus(status);
    }

    /**
     * 更新保姆狀態
     * 
     * @param sitterId 保姆編號
     * @param status   新狀態 (0:啟用, 1:停用)
     * @return SitterVO 更新後的保姆物件
     * @throws IllegalArgumentException 若保姆不存在
     */
    @Override
    @Transactional
    public SitterVO updateSitterStatus(Integer sitterId, Byte status) {
        Optional<SitterVO> optional = repository.findById(sitterId);
        if (!optional.isPresent()) {
            throw new IllegalArgumentException("保姆不存在: " + sitterId);
        }
        SitterVO vo = optional.get();
        vo.setSitterStatus(status);
        return repository.save(vo);
    }

    /**
     * 更新保姆資訊
     * 
     * @param sitterId   保姆編號
     * @param sitterName 保姆姓名
     * @param sitterAdd  服務地址
     * @return SitterVO 更新後的保姆物件
     * @throws IllegalArgumentException 若保姆不存在
     */
    @Override
    @Transactional
    public SitterVO updateSitterInfo(Integer sitterId, String sitterName, String sitterAdd) {
        Optional<SitterVO> optional = repository.findById(sitterId);
        if (!optional.isPresent()) {
            throw new IllegalArgumentException("保姆不存在: " + sitterId);
        }
        SitterVO vo = optional.get();
        vo.setSitterName(sitterName);
        vo.setSitterAdd(sitterAdd);
        return repository.save(vo);
    }

    /**
     * 更新保姆服務時間（營業時間設定）
     * 
     * @param sitterId    保姆編號
     * @param serviceTime 服務時間（24字元字串，0=不可預約, 1=可預約）
     * @return SitterVO 更新後的保姆物件
     * @throws IllegalArgumentException 若保姆不存在
     */
    @Override
    @Transactional
    public SitterVO updateServiceTime(Integer sitterId, String serviceTime) {
        System.out.println("=== Service 層 Debug (Direct Update) ===");
        System.out.println("收到 sitterId: " + sitterId);
        System.out.println("收到 serviceTime: " + serviceTime);

        // 使用自定義的 JPQL 更新，繞過 JPA 的髒檢查機制確保寫入
        repository.updateServiceTime(sitterId, serviceTime);

        // 重新查詢以確認更新
        Optional<SitterVO> optional = repository.findById(sitterId);
        if (!optional.isPresent()) {
            throw new IllegalArgumentException("保姆不存在: " + sitterId);
        }

        SitterVO saved = optional.get();
        System.out.println("DB 更新後 serviceTime: " + saved.getServiceTime());
        System.out.println("================================");

        return saved;
    }

    // ========== 排程相關功能 (透過會員 ID) ==========

    @Override
    @Transactional(readOnly = true)
    public List<BookingScheduleVO> getScheduleByMember(Integer memId, int year, int month) {
        SitterVO sitter = repository.findByMemId(memId);
        if (sitter == null) {
            throw new IllegalArgumentException("會員尚未成為保姆");
        }
        Integer sitterId = sitter.getSitterId();

        // 由於 BookingScheduleRepository 只有 findAll，暫時用 Java filter
        List<BookingScheduleVO> allSchedules = bookingScheduleRepository.findAll();

        return allSchedules.stream()
                .filter(s -> s.getSitterId().equals(sitterId))
                .filter(s -> {
                    LocalDate d = s.getScheduleDate();
                    return d.getYear() == year && d.getMonthValue() == month;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateScheduleForMember(Integer memId, LocalDate date, String status) {
        SitterVO sitter = repository.findByMemId(memId);
        if (sitter == null) {
            throw new IllegalArgumentException("會員尚未成為保姆");
        }
        Integer sitterId = sitter.getSitterId();

        // 檢查該日期是否已有資料 (使用 Repository 現有方法)
        Optional<BookingScheduleVO> existingOpt = bookingScheduleRepository.findBySitterIdAndScheduleDate(sitterId,
                date);

        BookingScheduleVO schedule;
        if (existingOpt.isPresent()) {
            schedule = existingOpt.get();
            schedule.setBookingStatus(status);
        } else {
            schedule = new BookingScheduleVO();
            schedule.setSitterId(sitterId);
            schedule.setScheduleDate(date);
            schedule.setBookingStatus(status);
        }
        bookingScheduleRepository.save(schedule);
    }

    // ========== 會員搜尋保姆功能 ==========

    /**
     * 根據條件搜尋保姆
     * 
     * @param criteria 搜尋條件（地區、服務項目、寵物類型、價格範圍、排序方式）
     * @return List<SitterSearchDTO> 符合條件的保姆列表
     */
    @Override
    public List<SitterSearchDTO> searchSitters(SitterSearchCriteria criteria) {
        // 1. 根據地區篩選取得保姆列表
        List<SitterVO> sitters;

        // 處理地區篩選邏輯（使用縣市和區域名稱）
        if (criteria.getCityName() != null && !criteria.getCityName().isEmpty()) {
            List<Integer> areaIds = new ArrayList<>();

            // 情況 1: 只選擇縣市（沒有選擇區域）
            if (criteria.getDistrict() == null || criteria.getDistrict().isEmpty()) {
                // 查詢該縣市的所有區域
                List<com.petguardian.area.model.AreaVO> areas = areaRepository.findByCityName(criteria.getCityName());
                for (com.petguardian.area.model.AreaVO area : areas) {
                    areaIds.add(area.getAreaId());
                }
            }
            // 情況 2: 選擇縣市 + 區域
            else {
                // 查詢該縣市+區域對應的單一 area_id
                com.petguardian.area.model.AreaVO area = areaRepository.findByCityNameAndDistrict(
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
    public List<SitterSearchDTO> getAllActiveSitters() {
        List<SitterVO> sitters = repository.findBySitterStatusOrderBySitterRatingCountDesc((byte) 0);
        List<SitterSearchDTO> results = new ArrayList<>();

        for (SitterVO sitter : sitters) {
            results.add(convertToSearchDTO(sitter));
        }

        return results;
    }

    /**
     * 依會員 ID 查詢會員資訊
     */
    @Override
    public SitterMemberVO getSitterMemberById(Integer memId) {
        return sitterMemberRepository.findById(memId).orElse(null);
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
            for (com.petguardian.service.model.ServiceAreaVO sa : sitter.getServiceAreas()) {
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

    private Integer getServiceIdByName(String serviceName) {
        if (serviceName == null)
            return null;
        switch (serviceName) {
            case "到府照顧":
                return 1;
            case "到府遛狗":
                return 2;
            case "寵物寄宿":
                return 3;
            case "寵物安親":
                return 4;
            default:
                return null;
        }
    }

    private String getServiceNameById(Integer id) {
        if (id == null)
            return null;
        switch (id) {
            case 1:
                return "到府照顧";
            case 2:
                return "到府遛狗";
            case 3:
                return "寵物寄宿";
            case 4:
                return "寵物安親";
            default:
                return null;
        }
    }

    private Integer getPetTypeIdByName(String petTypeName) {
        if (petTypeName == null)
            return null;
        switch (petTypeName) {
            case "貓":
                return 1;
            case "狗":
                return 2;
            default:
                return null;
        }
    }

    private String getPetTypeNameById(Integer id) {
        if (id == null)
            return null;
        switch (id) {
            case 1:
                return "貓";
            case 2:
                return "狗";
            default:
                return null;
        }
    }

    /**
     * 更新保姆的一週行程 (從前端傳來的複雜 JSON 資料解析並儲存)
     */
    @Override
    @Transactional
    public void updateWeeklySchedule(Integer sitterId,
            java.util.Map<String, java.util.Map<String, String>> scheduleData) {
        // 1. 建立 24 小時的狀態字串（合併七天的資料）
        char[] serviceTimeArray = new char[24];
        // 初始化為全部不可預約
        for (int i = 0; i < 24; i++) {
            serviceTimeArray[i] = '0';
        }

        // 2. 遍歷七天的資料
        for (int day = 0; day < 7; day++) {
            String dayKey = String.valueOf(day);
            if (scheduleData.containsKey(dayKey)) {
                java.util.Map<String, String> daySchedule = scheduleData.get(dayKey);

                for (int hour = 0; hour < 24; hour++) {
                    String hourStr = String.valueOf(hour);
                    if (daySchedule.containsKey(hourStr)) {
                        String status = daySchedule.get(hourStr);
                        // 0: 可預約 (前端傳來的狀態)
                        // service_time: 0=不可預約, 1=可預約 (資料庫儲存的狀態)
                        if ("0".equals(status)) {
                            // 只要任何一天這個時段是可預約，就設為可預約
                            serviceTimeArray[hour] = '1';
                        }
                    }
                }
            }
        }

        String serviceTime = new String(serviceTimeArray);

        // 3. 更新資料庫
        updateServiceTime(sitterId, serviceTime);
    }
}