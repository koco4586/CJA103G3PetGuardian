package com.petguardian.pet.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.petguardian.pet.model.PetserItemrepository;
import com.petguardian.pet.service.PetserItemService;
import com.petguardian.pet.model.PetserItemVO;

@Controller
@RequestMapping("/pet")
public class PetServiceController {

    @Autowired
    private PetserItemrepository petserItemrepository;

    @Autowired
    private PetserItemService petserItemService;

    /**
     * 顯示寵物保姆服務介紹頁面
     * URL: GET /pet/services
     */
    @GetMapping("/services")
    public String showPetServices(Model model) {
        // 使用 Service 獲取經過處理的 VO 清單，確保顯示效果
        List<PetserItemVO> serviceList = petserItemService.getAllItemsForDisplay();

        // 如果沒有資料，嘗試直接從 Repo 拿取所有資料 (防止 status 為空時顯示不到)
        if (serviceList == null || serviceList.isEmpty()) {
            model.addAttribute("serviceList", petserItemrepository.findAll());
        } else {
            model.addAttribute("serviceList", serviceList);
        }

        return "frontend/pet/Petser_item";
    }

    /**
     * 顯示寵物服務細項 (相容舊路徑)
     * URL: GET /pet/Petser_item
     */
    @GetMapping("/Petser_item")
    public String showpetset(Model model) {
        // 同步跳轉邏輯，確保這個網址也能看到資料
        return showPetServices(model);
    }

    /**
     * 根據服務類型搜尋保姆
     * URL: GET /pet/sitters/search
     */
    @GetMapping("/sitters/search")
    public String searchSittersByService(@RequestParam(required = false) String service, Model model) {
        if (service != null && !service.trim().isEmpty()) {
            model.addAttribute("serviceFilter", service);
        }
        // forward 到保姆搜尋控制器
        return "forward:/booking/search?service=" + (service != null ? service : "");
    }
}
