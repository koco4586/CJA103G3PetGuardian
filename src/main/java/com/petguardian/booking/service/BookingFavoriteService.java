package com.petguardian.booking.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.petguardian.booking.model.BookingFavoriteRepository;
import com.petguardian.booking.model.BookingFavoriteVO;
import com.petguardian.petsitter.model.PetSitterServiceRepository;
import com.petguardian.sitter.model.SitterRepository;

/**
 * 收藏服務
 * 職責：處理會員收藏/取消收藏保母的功能，以及查詢收藏列表
 */
@Service
@Transactional
public class BookingFavoriteService {

    @Autowired
    private BookingFavoriteRepository sitterFavoriteRepository;

    @Autowired
    private SitterRepository sitterRepository;

    @Autowired
    private PetSitterServiceRepository petSitterServiceRepository;

    /**
     * 取得會員的所有收藏保母列表
     * 處理流程：
     * 1. 查詢該會員的所有收藏紀錄
     * 2. 逐一補充每個收藏的保母資訊（保母名稱、基礎價格）
     * 3. 若查詢失敗則設定預設值
     */
    public List<BookingFavoriteVO> getSitterFavoritesByMember(Integer memId) {
        // 查詢該會員的所有收藏
        List<BookingFavoriteVO> favorites = sitterFavoriteRepository.findByMemId(memId);

        // 補充每個收藏的保母資料
        for (BookingFavoriteVO fav : favorites) {
            try {
                // 查詢保母資料 
                var sitter = sitterRepository.findById(fav.getSitterId()).orElse(null);

                if (sitter != null) {
                    // 設定保母名稱
                    fav.setSitterName(sitter.getSitterName());

                    // 查詢保母的服務價格 
                    // 取得該保母的所有服務項目
                    var services = petSitterServiceRepository.findBySitter_SitterId(fav.getSitterId());

                    // 若有服務項目，取第一個服務的價格作為基礎價格
                    if (services != null && !services.isEmpty()) {
                        fav.setBasePrice(services.get(0).getDefaultPrice());
                    } else {
                        fav.setBasePrice(0); // 若無服務項目，價格設為0
                    }
                } else {
                    // 保母不存在時的預設值
                    fav.setSitterName("未知保母");
                    fav.setBasePrice(0);
                }
            } catch (Exception e) {
                // 例外處理：查詢失敗時設定預設值 
                fav.setSitterName("未知保母");
                fav.setBasePrice(0);
            }
        }

        return favorites;
    }

    /**
     * 切換收藏狀態（已收藏則取消，未收藏則新增）
     * 處理流程：
     * 1. 查詢該會員是否已收藏該保母
     * 2. 若已收藏 → 移除收藏紀錄，返回 false
     * 3. 若未收藏 → 新增收藏紀錄，返回 true
     */
    public boolean toggleSitterFavorite(Integer memId, Integer sitterId) {
        // 步驟 1：查詢是否已經收藏
        var existingFav = sitterFavoriteRepository.findByMemIdAndSitterId(memId, sitterId);

        if (existingFav.isPresent()) {
            // 步驟 2：已存在，則移除收藏（取消收藏）
            sitterFavoriteRepository.delete(existingFav.get());
            return false; // 返回 false 表示已取消收藏
        } else {
            // 步驟 3：不存在，則新增收藏紀錄
            BookingFavoriteVO newFav = new BookingFavoriteVO();
            newFav.setMemId(memId);
            newFav.setSitterId(sitterId);

            sitterFavoriteRepository.save(newFav);
            return true; // 返回 true 表示已新增收藏
        }
    }
}