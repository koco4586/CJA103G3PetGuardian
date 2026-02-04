package com.petguardian.pet.service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.petguardian.pet.model.PetServiceItem;
import com.petguardian.pet.model.PetserItemVO;
import com.petguardian.pet.model.PetserItemrepository;

@Service
public class PetserItemSerImpl implements PetserItemService {
 
    @Autowired
    private PetserItemrepository repo;

    // 1. 顯示所有上架服務
    @Override
    public List<PetserItemVO> getAllItemsForDisplay() {
        List<PetServiceItem> entities = repo.findByServiceStatus(1);
        return convertToVOList(entities);
    }

    // 2. 實作關鍵字搜尋 (解決 Method must implement 報錯)
    @Override
    public List<PetserItemVO> searchItemsByKeyword(String keyword) {
        List<PetServiceItem> entities = repo.findByServiceTypeContaining(keyword);
        return convertToVOList(entities);
    }

    // 3. 取得單一項目細節 (解決 Method must implement 報錯)
    @Override
    public PetserItemVO getOneItemDetail(Integer serviceItemId) {
        PetServiceItem entity = repo.findById(serviceItemId).orElse(null);
        if (entity != null) {
            return convertToVO(entity);
        }
        return null;
    }

    // --- 封裝轉換邏輯，避免代碼重複與 Type mismatch 錯誤 ---
    private List<PetserItemVO> convertToVOList(List<PetServiceItem> entities) {
        return entities.stream()
                       .map(this::convertToVO)
                       .collect(Collectors.toList());
    }

    private PetserItemVO convertToVO(PetServiceItem e) {
        PetserItemVO vo = new PetserItemVO();
        vo.setServiceItemId(e.getServiceItemId());
        vo.setServiceType(e.getServiceType());
        
        // 處理價格 (動態抓取)
        Integer price = e.getServicePrice();
        vo.setPriceText(price != null ? "NT$ " + price : "價格依保姆而定");

        // 處理描述
        vo.setServiceDesc(e.getServiceDesc() != null ? e.getServiceDesc() : "詳細服務內容以保母頁面為主");
        
        // 如果未來有 Sitter ID 關聯，代碼寫在這裡
        // vo.setSitterId(e.getSitterId());
        
        return vo;
    }
}