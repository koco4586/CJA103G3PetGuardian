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
        List<PetServiceItem> entities = repo.findByServiceStatus(1); // 或是 repo.findByServiceStatus(1)

        return entities.stream().map(e -> {
            PetserItemVO vo = new PetserItemVO();
            vo.setServiceItemId(e.getServiceItemId());
            vo.setServiceType(e.getServiceType());

            // // 處理 Null 檢查
            // vo.setServiceDesc(e.getServiceDetail() != null ? e.getServiceDetail() :
            // "暫無描述");
            // vo.setPriceText("NT$ " + (e.getServicePrice() != null ? e.getServicePrice() :
            // 0));

            return vo;
        }).collect(Collectors.toList());
    }
}
