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

    // 請務必把下面這個方法刪除或註解掉！！
    // List<EvaluateVO> findBySenderName(String senderName);

}
