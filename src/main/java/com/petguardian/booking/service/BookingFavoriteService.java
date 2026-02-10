package com.petguardian.booking.service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.petguardian.booking.model.BookingFavoriteRepository;
import com.petguardian.booking.model.BookingFavoriteVO;
import com.petguardian.petsitter.model.PetSitterServiceRepository;
import com.petguardian.sitter.model.SitterMemberRepository;
import com.petguardian.sitter.model.SitterMemberVO;
import com.petguardian.sitter.model.SitterRepository;
import com.petguardian.sitter.model.SitterVO;

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
    
    @Autowired
    private SitterMemberRepository sitterMemberRepository;

    /**
     * 取得會員的所有收藏保母列表
     * 處理流程：
     * 1. 查詢該會員的所有收藏紀錄
     * 2. 逐一補充每個收藏的保母資訊（保母名稱、基礎價格）
     * 3. 若查詢失敗則設定預設值
     */
    public List<BookingFavoriteVO> getSitterFavoritesByMember(Integer memId) {
        // 查詢該會員的所有收藏
    	return sitterFavoriteRepository.findByMemId(memId);
    }

    /**
     * 取得收藏清單並補齊保母資訊 (給會員中心收藏頁面用)
     */
    public List<BookingFavoriteVO> getSitterFavoritesWithDetail(Integer memId) {
        // 1. 拿到所有的收藏紀錄
        List<BookingFavoriteVO> favorites = sitterFavoriteRepository.findByMemId(memId);
        if (favorites.isEmpty()) return favorites;

        // 2. 收集所有的 SitterId
        Set<Integer> sitterIds = favorites.stream()
                .map(BookingFavoriteVO::getSitterId)
                .collect(Collectors.toSet());

        // 3. 批次查詢保母基本資料
        List<SitterVO> sitters = sitterRepository.findAllById(sitterIds);
        Map<Integer, SitterVO> sitterMap = sitters.stream()
                .collect(Collectors.toMap(SitterVO::getSitterId, s -> s));

        List<Integer> memIds = sitters.stream()
                .map(SitterVO::getMemId)
                .collect(Collectors.toList());
        Map<Integer, SitterMemberVO> memberMap = new java.util.HashMap<>();
        sitterMemberRepository.findAllById(memIds).forEach(m -> {
        	 memberMap.put(m.getMemId(), m);
        });
        // 4. 填回 VO
        java.util.Iterator<BookingFavoriteVO> iterator = favorites.iterator();
        while (iterator.hasNext()) {
            BookingFavoriteVO fav = iterator.next();
            SitterVO s = sitterMap.get(fav.getSitterId());
            // 檢查保母是否存在
            if (s == null) {
                iterator.remove(); 
                continue;
            }
            // 取得對應的會員資料
            SitterMemberVO member = memberMap.get(s.getMemId());
            // 核心過濾邏輯：
            boolean isSitterActive = (s.getSitterStatus() != null && s.getSitterStatus() == 0);
            boolean isMemberActive = (member != null && member.getMemStatus() != null && member.getMemStatus() == 1);
            if (!isSitterActive || !isMemberActive) {
                iterator.remove(); // 只要任何一個狀態不對，就讓這筆收藏在前端消失
                continue;
            }
            // 通過檢查，填入詳細資料 (保留您要的平均分與次數)
            fav.setSitterName(s.getSitterName());
            fav.setSitterMemId(s.getMemId());
            fav.setAvgRating(s.getAverageRating());       // 成功保留平均分
            fav.setRatingCount(s.getSitterRatingCount()); // 成功保留評價數
            fav.setBasePrice(500); 
            // 處理頭像
            String img = (member.getMemImage() != null) ? member.getMemImage() : "/images/default-avatar.png";
            fav.setMemImage(img);
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