package com.petguardian.pet.controller;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.petguardian.pet.model.PetVO;
import com.petguardian.pet.model.PetDTO; // 引入 DTO
import com.petguardian.pet.service.PetServiceImpl;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
//import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/pet")
public class PetController {

    @Autowired
    private PetServiceImpl petService;

    // 1. 保留：圖片顯示功能
    @GetMapping("/img/{petId}")
    @ResponseBody
    public byte[] getImg(@PathVariable Integer petId, HttpServletResponse res) {
        byte[] image = petService.getPetImage(petId);
        if (image != null) {
            res.setContentType("image/jpeg");
            return image;
        }
        return null;
    }
    
    @GetMapping("/dashboard") // 這是網址路徑，對應 window.location.href
    public String showDashboard(Model model, HttpSession session) {
        // 假設你從 session 拿 memId
        Integer memId = (Integer) session.getAttribute("memId");
        if (memId == null) memId = 1; // 測試用

        // 抓取該會員的所有寵物清單
        List<PetDTO> petlist = petService.getPetsByMemId(memId); 
        model.addAttribute("petlist", petlist);
        
        return "frontend/dashboard-pets"; // 這是你的 HTML 檔案名稱
    }
    
  @GetMapping("/select_page")
    public String showPetSelectPage(Model model) {
        // 如果頁面需要預載資料，可以在這裡 model.addAttribute
        // 注意：回傳字串必須與 templates 下的檔案路徑一致
        return "frontend/pet/petselect"; 
    }

    // 2. 更新：列出所有（現在使用 DTO 讓 HTML 能顯示類型名稱）
  @GetMapping("/all")
  public String getAll(@RequestParam(defaultValue = "1") Integer whichPage, Model model) {
      
	// 暫時加上這一行來模擬會員編號為 1 的人登入
	    // 這樣 HTML 判斷 memId == 1 的寵物時，就會出現修改按鈕
//	    if (session.getAttribute("memId") == null) {
//	        session.setAttribute("memId", 1); 
//	    }
	  
	  
	  Map<String, Object> pageData = petService.getPetsPageData(whichPage);
	  System.out.println("資料筆數: " + pageData.get("petlist"));
	  model.addAllAttributes(pageData); // 確保 pageData 裡面有一個 key 叫做 "petList"
      model.addAttribute("whichPage", whichPage);
      return "frontend/pet/petlistallpet2_getfromsession"; // 回傳你修正過後的「清單頁」
  }

    // 3. 保留：首頁導向
    

    // 4. 更新：依名稱查詢（使用 DTO 確保清單顯示正常）
  @PostMapping("/byName")
  public String getByName(@RequestParam String petName, Model model) {
      if (petName == null || petName.trim().isEmpty()) {
          model.addAttribute("errorMsgs", List.of("請輸入寵物姓名"));
          return "frontend/pet/petselect";
      }
      
      // 1. 取得查詢結果
      List<PetDTO> list = petService.findPetsByNameDTO(petName); 
      
      if (list.isEmpty()) {
          model.addAttribute("errorMsgs", List.of("查無此寵物姓名"));
          return "frontend/pet/petselect";
      }

      // 2. 取出第一筆寵物 (因為你的詳情頁需要單個 pet 物件)
      PetDTO pet = list.get(0);
      Integer petId = pet.getPetId();

      // 3. 補齊詳情頁需要的「上一筆/下一筆」分頁資訊 (這段跟 getOne 一樣)
      List<Integer> allIds = petService.getAllPetIds(); 
      int currentIndex = allIds.indexOf(petId);
      int total = allIds.size();

      Integer prevId = (currentIndex > 0) ? allIds.get(currentIndex - 1) : null;
      Integer nextId = (currentIndex < total - 1) ? allIds.get(currentIndex + 1) : null;

      // 4. 注意！這裡的 Key 要叫 "pet" 而不是 "list"
      model.addAttribute("pet", pet); 
      model.addAttribute("prevId", prevId);
      model.addAttribute("nextId", nextId);
      model.addAttribute("currentIndex", currentIndex);
      model.addAttribute("total", total);

      return "frontend/pet/petlistonepet"; 
  }

    @GetMapping("/one")
    // 5. 更新：單筆查詢（使用 DTO）
    public String getOne(@RequestParam(value="petId", required=false) String petIdStr, Model model) {
    	java.util.List<String> errorMsgs = new java.util.LinkedList<>();
        model.addAttribute("errorMsgs", errorMsgs);

        // 2. 檢查是否為空
        if (petIdStr == null || petIdStr.trim().isEmpty()) {
            errorMsgs.add("請輸入寵物編號");
            return "frontend/pet/petselect"; // 回到查詢頁
        }

        Integer petId = null;
        try {
            // 3. 嘗試轉成數字
            petId = Integer.valueOf(petIdStr);
        } catch (NumberFormatException e) {
            errorMsgs.add("寵物編號格式不正確，請輸入數字");
            return "frontend/pet/petselect";
        }

        // 4. 取得寵物詳情 (原本的邏輯)
        PetDTO petDTO = petService.getOnePetDTO(petId);
        if (petDTO == null) {
            errorMsgs.add("查無資料");
            return "frontend/pet/petselect"; // 回到查詢頁，這時 HTML 就能顯示「查無資料」了
        }

        // --- 以下為原本成功後的邏輯 ---
        List<Integer> allIds = petService.getAllPetIds(); 
        int currentIndex = allIds.indexOf(petId);
        int total = allIds.size();

        Integer prevId = (currentIndex > 0) ? allIds.get(currentIndex - 1) : null;
        Integer nextId = (currentIndex < total - 1) ? allIds.get(currentIndex + 1) : null;

        model.addAttribute("pet", petDTO);
        model.addAttribute("prevId", prevId);
        model.addAttribute("nextId", nextId);
        model.addAttribute("currentIndex", currentIndex);
        model.addAttribute("total", total);

        return "frontend/pet/petlistonepet";
    }

    // 6. 保留：Base64 新增功能
    @PostMapping("/insertBase64")
    @ResponseBody
    public String insertBase64(@RequestParam String petImageBase64, @RequestParam String petName,
                               @RequestParam String typeId, @RequestParam String petGender,
                               @RequestParam(required = false) String petAge, @RequestParam String sizeId,
                               @RequestParam String petDescription)
//    						 ,jakarta.servlet.http.HttpSession session)//會員有了的話把上面的sc後面小括號刪掉並打開這行
    
//    { // 🔴 注入 session
//        
//        // 取得目前操作者的 ID
//        Integer memId = (Integer) session.getAttribute("memId");
//        
//        // 如果沒登入不能新增 (目前測試可先註解)
//        // if (memId == null) return "error: 請先登入";
//
//        // 🔴 傳入 memId 給 Service
//        petService.addPetFromBase64(petImageBase64, petName, petType, petGender, petAge, petSize, petDesc, memId);
//        return "success";
    
    
    {
        petService.addPetFromBase64(petImageBase64, petName, typeId, petGender, petAge, sizeId, petDescription);
        return "success";
    }
     

    // 7. 保留：一般表單新增
    @PostMapping("/insert")
    public String insert(@ModelAttribute PetVO petVO, @RequestParam MultipartFile petImage) throws Exception {
        petService.addPet(petVO, petImage);
        return "redirect:/pet/all";
    }
    
 // 9. 新增：跳轉到新增寵物頁面，並加入權限檢查
    @GetMapping("/add_page")
    public String showAddPage(jakarta.servlet.http.HttpSession session) {
        // 🔴 權限判斷：檢查是否登入
//        Integer memId = (Integer) session.getAttribute("memId", user.getMemId());
//        
//        // --- 測試用模擬登入 (當會員功能還沒做好時，取消下面這行註解即可測試) ---
//        // if (memId == null) { session.setAttribute("memId", 1); memId = 1; }
//        // -----------------------------------------------------------
//
//        if (memId == null) {
//            // 如果沒登入，跳轉到登入頁面 (請根據你組員設定的登入路徑修改)
//            // 目前我們先註解掉跳轉邏輯，方便你開發
//            // return "redirect:/member/login"; 
//            
//            // 如果想暫時讓沒登入的人也能看，就直接回傳頁面
//            return "pet_add_page"; 
//        }
        
        return "frontend/pet/pet1"; // 這是你剛剛貼給我的那個新增 HTML 的檔名
    }
    
    
    
    
    @GetMapping("/confirm")
    public String showConfirmPage() {
        return "frontend/pet/Petconfirm"; // 對應 templates/pet/Petconfirm.html
    }
    
    @GetMapping("/getOne_For_Update")
    public String showEditPage(@RequestParam("petId") Integer petId, Model model) {
        // 1. 抓取要修改的那筆資料
        PetDTO petDTO = petService.getOnePetDTO(petId);
        
        // 2. 把資料傳給修改頁面
        model.addAttribute("pet", petDTO); 
        
        // 3. 回傳修改頁面的 HTML 路徑 (請確認你的檔案路徑)
        // 假設你的修改頁面是在 templates/frontend/pet/pet_update.html
        return "frontend/pet/petupdate_pet_input"; 
    }
    
    
    // 8. 保留：更新功能
    @PostMapping("/update")
    @ResponseBody
    public String update(
    		
    					 @ModelAttribute PetVO petVO,
    					 @RequestParam(value = "petId", required = false) Integer petId,
                         @RequestParam(required = false) MultipartFile upFiles,
                         @RequestParam(required = false) String petImageBase64, // 接收 JS 產生的圖
                         @RequestParam(required = false) String deleteImage)   // 接收刪除旗標
//           				,@RequestParam(required = false) HttpSession session  //記得把image後面的)去掉才能打開這行註解
                        		 

                        		 throws Exception {
    	
    	 // 從 session 拿真正登入的人 ID(有會員時再打開，以及打開最上面的Http跟上面的Http註解(並且按照後面提示去小修改
//        Integer currentMemId = (Integer) session.getAttribute("memId");
//        
//        // 🔴 安全檢查：如果登入者不是寵物的主人，拒絕執行並跳回列表
//        // 你可能需要先從 DB 查出這隻寵物原本的主人是誰
//        PetDTO originalPet = petService.getOnePetDTO(petVO.getPetId());
//        if (!originalPet.getMemId().equals(currentMemId)) {
//            return "redirect:/pet/all"; // 或者導向錯誤頁面
//        }
        
        // 呼叫我們之前在 Service 準備好的混合更新方法
        // 如果你 Service 還沒改名，建議統一呼叫一個處理 Base64 的方法
    	
    	// 如果 ModelAttribute 沒綁定到，手動塞進去
        if (petVO.getPetId() == null && petId != null) {
            petVO.setPetId(petId);
        }
        
        PetDTO originalPet = petService.getOnePetDTO(petVO.getPetId());
        if (originalPet != null) {
            petVO.setMemId(originalPet.getMemId()); // 把舊的會員 ID 塞回 VO
        } else {
            // 如果查不到，暫時塞一個測試用 ID (例如 1)，避免報錯
            petVO.setMemId(1); 
        }
        
    	 System.out.println("===== 進入 pet update Controller =====");
        petService.updatePetWithCanvas(petVO, petImageBase64, deleteImage);
        
        return "success";
    }
    
    @PostMapping("/delete")
    @ResponseBody // ✅ 注意：加上這個，讓回傳的字串直接當成網頁內容
    public String deletePet(@RequestParam("petId") Integer petId, HttpServletRequest request) {
        
        // 1. 執行刪除
        petService.deletePet(petId);
        
        // 2. 判斷跳轉目標
        String referer = request.getHeader("Referer");
        String redirectUrl = "/pet/dashboard"; // 預設回首頁
        if (referer != null && referer.contains("listAllPet")) {
            redirectUrl = "/pet/All";
        }

        // 3. 回傳一段 JS 腳本
        return "<script>" +
               "alert('寵物編號 " + petId + " 刪除成功！');" +
               "window.location.href='" + redirectUrl + "';" +
               "</script>";
    }
}