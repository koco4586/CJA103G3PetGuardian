package com.petguardian.sitter.service;

// import java.util.ArrayList; // [Refactor] Unused
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
// import com.petguardian.booking.model.BookingScheduleRepository; // [Refactor] Unused
import com.petguardian.booking.model.BookingOrderRepository;
import com.petguardian.booking.model.BookingOrderVO;
import com.petguardian.sitter.model.SitterDashboardDTO;
// [NEW] Repositories for search
// import com.petguardian.petsitter.model.PetSitterServiceRepository; // [Refactor] 移除未使用 import
import com.petguardian.petsitter.service.PetSitterService; // [Refactor] 新增 Service import
import com.petguardian.petsitter.model.PetSitterServiceVO;
// import com.petguardian.petsitter.model.PetSitterServicePetTypeRepository; // [Refactor] Unused
// import com.petguardian.petsitter.model.PetSitterServicePetTypeVO; // [Refactor] Unused
import java.time.LocalDate;
// import java.util.stream.Collectors; // [Refactor] Unused

// import jakarta.persistence.criteria.Join; // [Refactor] Unused
// import jakarta.persistence.criteria.JoinType; // [Refactor] Unused
// import jakarta.persistence.criteria.Predicate; // [Refactor] Unused
// import org.springframework.data.jpa.domain.Specification; // [Refactor] Unused
import com.petguardian.service.model.ServiceAreaVO;
// import com.petguardian.area.model.AreaVO; // [Refactor] Unused

/**
 * 保姆業務邏輯實作
 * 
 * 提供保姆資料管理、保姆搜尋等功能的實作
 */
@Service("sitterService")
@Transactional(readOnly = true)
public class SitterServiceImpl implements SitterService {

    @Autowired
    private SitterRepository repository;

    @Autowired
    private MemberRegisterRepository memberRepository;

    @Autowired
    private SitterMemberRepository sitterMemberRepository;

    @Autowired
    private PetSitterService petSitterService; // [Refactor] 改用 Service 注入

    @Autowired
    private SitterSearchService searchService;

    @Autowired
    private SitterScheduleService scheduleService;

    @Autowired
    private BookingOrderRepository bookingOrderRepository;

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
        List<SitterVO> sitters = repository.findAll();
        // 同步會員停權狀態
        for (SitterVO sitter : sitters) {
            memberRepository.findById(sitter.getMemId()).ifPresent(member -> {
                // 如果會員已停權 (0)，則顯示保母狀態為停權 (1)
                if (member.getMemStatus() == 0) {
                    sitter.setSitterStatus((byte) 1);
                }
            });
        }
        return sitters;
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
        return scheduleService.updateServiceTime(sitterId, serviceTime);
    }

    // ========== 排程相關功能 (透過會員 ID) ==========

    @Override
    @Transactional(readOnly = true)
    public List<BookingScheduleVO> getScheduleByMember(Integer memId, int year, int month) {
        return scheduleService.getScheduleByMember(memId, year, month);
    }

    @Override
    @Transactional
    public void updateScheduleForMember(Integer memId, LocalDate date, String status) {
        scheduleService.updateScheduleForMember(memId, date, status);
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
        return searchService.searchSitters(criteria);
    }

    /**
     * 取得所有啟用中的保姆（用於無篩選條件時）
     * 
     * @return List<SitterSearchDTO> 所有啟用中的保姆列表
     */
    @Override
    public List<SitterSearchDTO> getAllActiveSitters() {
        return searchService.getAllActiveSitters();
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
     * 更新保姆的一週行程 (從前端傳來的複雜 JSON 資料解析並儲存)
     */
    @Override
    @Transactional
    public void updateWeeklySchedule(Integer sitterId,
            java.util.Map<String, java.util.Map<String, String>> scheduleData) {
        scheduleService.updateWeeklySchedule(sitterId, scheduleData);
    }

    /**
     * [Refactor] 取得保姆儀表板所需的整合資料
     * 修改說明: 2026-02-01 優化 DTO 結構，移除對 SitterVO 的直接依賴
     */
    @Override
    @Transactional(readOnly = true)
    public SitterDashboardDTO getDashboardData(Integer memId) {
        // 1. 查詢保姆資料 (使用優化版查詢，一次載入關聯的 ServiceArea 和 Area)
        SitterVO sitter = repository.findByMemIdWithAreas(memId);
        if (sitter == null) {
            return null;
        }

        // 2. 統計數據
        // 服務數量
        // [Refactor] 使用 PetSitterService 取得服務列表，減少直接 Repository 依賴
        List<PetSitterServiceVO> services = petSitterService.getServicesBySitter(sitter.getSitterId());

        // 服務地區數量 (使用 Hibernate Lazy Loading 直接取得)
        List<ServiceAreaVO> areas = sitter.getServiceAreas();

        // 待審核訂單 (Status = 0)
        List<BookingOrderVO> pendingOrders = bookingOrderRepository.findBySitterIdAndOrderStatus(sitter.getSitterId(),
                0);

        // 3. [Refactor] 將 Entity 轉換為 Inner DTO，確保資料結構解耦
        // 使用 Builder 模式進行 DTO 組裝
        return SitterDashboardDTO.builder()
                .sitterInfo(SitterDashboardDTO.SitterInfoDTO.builder()
                        .sitterId(sitter.getSitterId())
                        .memId(sitter.getMemId())
                        .sitterName(sitter.getSitterName())
                        .sitterAdd(sitter.getSitterAdd())
                        .sitterStatus(sitter.getSitterStatus())
                        .serviceTime(sitter.getServiceTime())
                        .ratingCount(sitter.getSitterRatingCount())
                        .starCount(sitter.getSitterStarCount())
                        // 同步會員大頭貼
                        .memImage(memberRepository.findById(memId).map(m -> m.getMemImage()).orElse(null))
                        .build())
                .serviceCount(services.size())
                .areaCount(areas != null ? areas.size() : 0)
                .pendingOrderCount(pendingOrders != null ? pendingOrders.size() : 0)
                .averageRating(sitter.getAverageRating())
                .services(services)
                .areas(areas)
                .pendingOrders(pendingOrders)
                .build();
    }

    /**
     * 取得保姆的歷史評價 (僅包含有文字評論的訂單)
     */
    @Override
    public List<BookingOrderVO> getSitterReviews(Integer sitterId) {
        return bookingOrderRepository.findBySitterIdAndSitterReviewIsNotNullOrderByEndTimeDesc(sitterId);
    }
}