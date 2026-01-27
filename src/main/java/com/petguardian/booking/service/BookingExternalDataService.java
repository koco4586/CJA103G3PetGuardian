package com.petguardian.booking.service;

import java.util.List;

import com.petguardian.booking.model.BookingOrderVO;
import com.petguardian.member.model.Member;
import com.petguardian.pet.model.PetVO;
import com.petguardian.petsitter.model.PetSitterServiceVO;

public interface BookingExternalDataService {
    // 獲取會員資料
    Member getMemberInfo(Integer memId);

    // 獲取寵物資料
    PetVO getPetInfo(Integer petId);

    // 獲取保母與服務價格資訊
    PetSitterServiceVO getSitterInfo(Integer sitterId, Integer serviceItemId);

    List<BookingOrderVO> getOrdersByMemberId(Integer memId);
}