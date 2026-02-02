package com.petguardian.pet.service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.petguardian.pet.model.PetServiceItem;
import com.petguardian.pet.model.PetserItemVO;
import com.petguardian.pet.model.PetserItemrepository;

@Service // 註解要放在實作類別上
public class PetserItemSerImpl implements PetserItemService {
	
    @Autowired
    private PetserItemrepository repo; // 這裡可以放 repo

    @Override // 表示這是實作介面的方法
    public List<PetserItemVO> getAllItemsForDisplay() {
        // 從資料庫撈取資料
    	List<PetServiceItem> entities = repo.findAll(); // 或是 repo.findByServiceStatus(1)
        
        return entities.stream().map(e -> {
            PetserItemVO vo = new PetserItemVO();
            vo.setServiceItemId(e.getServiceItemId());
            vo.setServiceType(e.getServiceType());

            // 處理 Null 檢查
            vo.setServiceDesc("詳細服務內容以保母頁面為主");
            vo.setPriceText("價格依保姆而定");
            return vo;
        }).collect(Collectors.toList());
    }
}
