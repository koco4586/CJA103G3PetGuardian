package com.petguardian.pet.service;

import java.util.List;
import com.petguardian.pet.model.PetserItemVO;

// 介面不需要 @Service 註解
public interface PetserItemService {
    
    // 只定義方法簽署，結尾用分號 ;
    List<PetserItemVO> getAllItemsForDisplay();
}