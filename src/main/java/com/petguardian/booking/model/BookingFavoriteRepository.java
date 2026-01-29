package com.petguardian.booking.model;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingFavoriteRepository extends JpaRepository<BookingFavoriteVO, Integer> {
    // 檢查特定會員是否收藏過特定保母
    Optional<BookingFavoriteVO> findByMemIdAndSitterId(Integer memId, Integer sitterId);
    
    // 取得該會員所有的收藏保母 ID (之後做清單頁面會用到)
    List<BookingFavoriteVO> findByMemId(Integer memId);
}
