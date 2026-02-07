package com.petguardian.evaluate.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Collection;

@Repository
public interface EvaluateRepository extends JpaRepository<EvaluateVO, Integer> {

    /**
     * 根據訂單編號找尋評價
     * 這是有效的，因為 bookingOrderId 是資料庫有的欄位
     */
    List<EvaluateVO> findByBookingOrderId(Integer bookingOrderId);

    /**
     * 根據接收者 ID (保姆 ID) 查詢所有評價
     * 
     * @param receiverId 保姆 ID
     * @return 該保姆的所有評價列表
     */
    List<EvaluateVO> findByReceiverId(Integer receiverId);

    /**
     * 根據接收者 ID 和角色類型查詢評價
     * 
     * @param receiverId 接收者 ID（會員或保母）
     * @param roleType   角色類型（0=保母評會員, 1=會員評保母）
     * @return 符合條件的評價列表
     */
    List<EvaluateVO> findByReceiverIdAndRoleType(Integer receiverId, Integer roleType);

    /**
     * 批次根據訂單編號找尋評價 (解決 N+1 問題)
     */
    List<EvaluateVO> findByBookingOrderIdIn(Collection<Integer> bookingOrderIds);

    /**
     * SQL 聚合優化：獲取保姆的平均星數
     */
    @Query("SELECT AVG(e.starRating) FROM EvaluateVO e WHERE e.receiverId = :sitterId AND e.roleType = 1 AND (e.isHidden IS NULL OR e.isHidden = 0)")
    Double getAverageRatingBySitterId(@Param("sitterId") Integer sitterId);

    /**
     * SQL 聚合優化：獲取保姆的評論總數
     */
    @Query("SELECT COUNT(e) FROM EvaluateVO e WHERE e.receiverId = :sitterId AND e.roleType = 1 AND (e.isHidden IS NULL OR e.isHidden = 0)")
    Long getReviewCountBySitterId(@Param("sitterId") Integer sitterId);
}
