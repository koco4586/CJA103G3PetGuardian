package com.petguardian.evaluate.model;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * 評價系統的 Repository
 * 繼承 JpaRepository 即可獲得基本 CRUD 功能
 */
@Repository
public interface EvaluateRepository extends JpaRepository<EvaluateVO, Integer> {

    /**
     * 根據訂單編號找尋評價
     * 用於將會員與保姆的評價組合在一起（框中框邏輯）
     */
    List<EvaluateVO> findByBookingOrderId(Integer bookingOrderId);

    /**
     * 根據發送者名稱查詢 (測試用)
     */
    List<EvaluateVO> findBySenderName(String senderName);
}
