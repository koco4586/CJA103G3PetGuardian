package com.petguardian.booking.service;

import java.util.List;

import com.petguardian.booking.model.BookingOrderVO;
import com.petguardian.booking.model.MemberDTO;
import com.petguardian.booking.model.PetDTO;
import com.petguardian.booking.model.SitterDTO;

public interface BookingExternalDataService {
    // 獲取會員資料
    MemberDTO getMemberInfo(Integer memId);
    
    // 獲取寵物資料
    PetDTO getPetInfo(Integer petId);
    
    // 獲取保母與服務價格資訊
    SitterDTO getSitterInfo(Integer sitterId, Integer serviceItemId);

    List<BookingOrderVO> getOrdersByMemberId(Integer memId);
    
}