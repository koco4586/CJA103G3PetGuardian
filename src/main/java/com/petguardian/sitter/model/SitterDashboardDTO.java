package com.petguardian.sitter.model;

import lombok.Data;
import java.util.List;
import com.petguardian.booking.model.BookingOrderVO;
import com.petguardian.petsitter.model.PetSitterServiceVO;
import com.petguardian.service.model.ServiceAreaVO;

@Data
public class SitterDashboardDTO {
    private SitterVO sitter;

    // 統計數據
    private int serviceCount;
    private int areaCount;
    private int pendingOrderCount;
    private double averageRating;

    // 詳細列表
    private List<PetSitterServiceVO> services;
    private List<ServiceAreaVO> areas;
    private List<BookingOrderVO> pendingOrders;
}
