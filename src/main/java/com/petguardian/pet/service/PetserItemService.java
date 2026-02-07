package com.petguardian.pet.service;

import java.util.List;
import com.petguardian.pet.model.PetserItemVO;

// 介面不需要 @Service 註解
public interface PetserItemService {
    
	List<PetserItemVO> getAllItemsForDisplay();

    // 2. 缺少的：關鍵字搜尋（對應你的 Repository 模糊查詢）
    List<PetserItemVO> searchItemsByKeyword(String keyword);

    // 3. 缺少的：取得單一項目細節（用於點進去查看詳細描述）
    PetserItemVO getOneItemDetail(Integer serviceItemId);
}