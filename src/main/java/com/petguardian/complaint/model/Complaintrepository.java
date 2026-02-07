package com.petguardian.complaint.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Collection;

@Repository
public interface Complaintrepository extends JpaRepository<ComplaintVO, Integer> {
    // 繼承後，基礎的 CRUD（save, findAll, delete...）就都自動有了

    /**
     * 計算指定檢舉狀態的檢舉數量
     */
    long countByReportStatus(Integer reportStatus);

    /**
     * 批次統計多個訂單的檢舉總數 (解決 N+1 問題)
     * 回傳：訂單ID 與 檢舉次數 的 Object 陣列列表
     */
    @Query("SELECT c.bookingOrderId, COUNT(c) FROM ComplaintVO c WHERE c.bookingOrderId IN :ids GROUP BY c.bookingOrderId")
    List<Object[]> countComplaintsByBookingOrderIds(@Param("ids") Collection<Integer> bookingOrderIds);

    /**
     * 批次統計多個評價的檢舉總數 (解決 N+1 問題)
     * 回傳：評價ID 與 檢舉次數 的 Object 陣列列表
     */
    @Query("SELECT c.evaluateId, COUNT(c) FROM ComplaintVO c WHERE c.evaluateId IN :ids GROUP BY c.evaluateId")
    List<Object[]> countComplaintsByEvaluateIds(@Param("ids") Collection<Integer> evalIds);
}