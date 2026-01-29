package com.petguardian.evaluate.model;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EvaluateRepository extends JpaRepository<EvaluateVO, Integer> {

    /**
     * 根據訂單編號找尋評價
     * 這是有效的，因為 bookingOrderId 是資料庫有的欄位
     */
    List<EvaluateVO> findByBookingOrderId(Integer bookingOrderId);

    // 請務必把下面這個方法刪除或註解掉！！
    // List<EvaluateVO> findBySenderName(String senderName);
    
}
