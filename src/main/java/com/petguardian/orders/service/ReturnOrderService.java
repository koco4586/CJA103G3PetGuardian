package com.petguardian.orders.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.petguardian.orders.model.ReturnOrderPicVO;
import com.petguardian.orders.model.ReturnOrderVO;
import org.springframework.web.multipart.MultipartFile;

public interface ReturnOrderService {

    /**
     * 申請退貨（不含圖片）
     */
    Map<String, Object> applyReturn(Integer orderId, String returnReason);

    /**
     * 申請退貨（含圖片上傳）
     */
    Map<String, Object> applyReturn(Integer orderId, String returnReason, List<MultipartFile> images);

    /**
     * 根據退貨單ID取得圖片列表
     */
    List<ReturnOrderPicVO> getReturnOrderPics(Integer returnId);

    /**
     * 查詢退貨單
     */
    Optional<ReturnOrderVO> getReturnOrderById(Integer returnId);

    /**
     * 根據訂單ID查詢退貨單
     */
    Optional<ReturnOrderVO> getReturnOrderByOrderId(Integer orderId);

    /**
     * 查詢所有退貨單
     */
    List<ReturnOrderVO> getAllReturnOrders();

    /**
     * 查詢買家的退貨單
     */
    List<ReturnOrderVO> getReturnOrdersByBuyerId(Integer buyerMemId);

    /**
     * 更新退貨狀態
     */
    ReturnOrderVO updateReturnStatus(Integer returnId, Integer newStatus);
}