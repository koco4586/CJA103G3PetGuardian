package com.petguardian.pet.controller;

//import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.petguardian.pet.model.PetVO;
import com.petguardian.booking.model.BookingOrderVO;
import com.petguardian.booking.service.BookingService;
import com.petguardian.complaint.model.ComplaintVO;
import com.petguardian.complaint.model.Complaintservice;
import com.petguardian.evaluate.model.EvaluateDTO;
import com.petguardian.evaluate.model.EvaluateVO;
import com.petguardian.evaluate.service.EvaluateService;
//import com.petguardian.evaluate.model.EvaluateDTO;
//import com.petguardian.evaluate.model.EvaluateRepository;

import com.petguardian.pet.model.PetDTO; // 引入 DTO
import com.petguardian.pet.service.PetService;

import jakarta.servlet.http.HttpServletResponse;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/pet")
public class PetController {

    @Autowired
    private PetService petService;

    @Autowired
    private Complaintservice complaintservice;

    @Autowired
    private EvaluateService evaluateService; // 注意這裡開頭是小寫 e

    @Autowired
    private BookingService bookingOrderSvc; // 🔴 必須加上這一行，變數名稱要對齊你呼叫的名字

    // @Autowired
    // private EvaluateRepository evaluateRepository; // 注入實例，解決 static 報錯問題

    @GetMapping("/index")
    public String index() {
        return "/frontend/index"; // 對應 templates/index.html
    }

    @GetMapping("/review")
    public String showReviewPage(Model model) {
        // 這裡只負責開門，讓使用者看到網頁
        return "/frontend/review";
    }

    // 🔹 前台申訴頁面（一般會員用）
    @PostMapping("/review")
    public String userComplaint(HttpSession session, Model model, ComplaintVO vo) {
        // 檢查是否登入（可選）
        // Integer memberId = (Integer) session.getAttribute("memberId");
        //
        // if (memberId == null) {
        // return "redirect:/member/login";
        // }

        // 可以傳入會員資料到前端
        // model.addAttribute("memberId", memberId);
        if (vo.getBookingOrderId() == null) {
            // 這裡可以做錯誤處理，暫時先手動補一個值測試
            vo.setBookingOrderId(1);
        }

        if (vo.getReportMemId() == null) {
            vo.setReportMemId(1001); // 先暫時給會員編號 1
        }

        // 設定被檢舉人 (to_reported_mem_id) 建議也補一個，不然可能換它報錯
        if (vo.getToReportedMemId() == null) {
            vo.setToReportedMemId(1002);
        }

        if (vo.getReportReason() == null || vo.getReportReason().trim().isEmpty()) {
            vo.setReportReason("使用者未填寫內容 (系統預設)");
        }

        // 3. 狀態預設為 0
        vo.setReportStatus(0);

        complaintservice.insert(vo);
        return "frontend/review"; // 對應 templates/frontend/complaint.html
    }

    @PostMapping("/submitComplaint")
    @ResponseBody
    public ResponseEntity<?> handleComplaint(ComplaintVO vo) {
        try {
            // --- 1. 補全後端必要的隱藏欄位 (防止資料庫 NOT NULL 報錯) ---

            // 如果前端沒傳訂單 ID，預設給 1 (測試用)
            if (vo.getBookingOrderId() == null) {
                vo.setBookingOrderId(1);
            }

            // 補上申訴時間

            // 補上初始狀態 (例如 0: 待處理)
            vo.setReportStatus(0);

            // 模擬當前登入者 (實際開發應從 Session 取得)
            vo.setReportMemId(1001);
            vo.setToReportedMemId(1002);

            // --- 2. 執行存檔 ---
            complaintservice.insert(vo);

            return ResponseEntity.ok("success");

        } catch (Exception e) {
            // --- 3. 關鍵：這行會讓真正的錯誤原因出現在你的 Console 下方 ---
            e.printStackTrace();
            return ResponseEntity.status(500).body("後端存檔失敗：" + e.getMessage());
        }
    }

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
    public String showDashboard(Model model, @RequestParam(defaultValue = "1") Integer whichPage, HttpSession session) {
        // 假設你從 session 拿 memId
        Integer memId = (Integer) session.getAttribute("memId");
        if (memId == null)
            memId = 1001; // 測試用

        // 抓取該會員的所有寵物清單

        Map<String, Object> pageData = petService.getPetsPageData(whichPage, memId);

        model.addAllAttributes(pageData);
        model.addAttribute("whichPage", whichPage);

        return "frontend/dashboard-pets"; // 這是你的 HTML 檔案名稱

    }

    @GetMapping("/select_page")
    public String showPetSelectPage(Model model) {
        // 如果頁面需要預載資料，可以在這裡 model.addAttribute
        // 注意：回傳字串必須與 templates 下的檔案路徑一致
        return "frontend/pet/petselect";
    }

    @GetMapping("/listone")
    public String getPetDetail(@RequestParam("petId") Integer petId, Model model) {
        // 1. 修正名稱：由 petSvc 改為 petService
        // 2. 修正方法：既然你其他地方用 getOnePetDTO，這裡也統一使用，確保資料完整
        PetDTO pet = petService.getOnePetDTO(petId);

        // 2. 將資料放入 model 傳給前端
        model.addAttribute("pet", pet);

        // 3. 回傳你的詳情頁面名稱
        return "frontend/pet/petlistonepet";
    }

    // 2. 更新：列出所有（現在使用 DTO 讓 HTML 能顯示類型名稱）
    @GetMapping("/all")
    public String getAll(@RequestParam(defaultValue = "1") Integer whichPage, Model model,
            HttpSession session) {

        // 暫時加上這一行來模擬會員編號為 1 的人登入
        // 這樣 HTML 判斷 memId == 1 的寵物時，就會出現修改按鈕
        // if (session.getAttribute("memId") == null) {
        // session.setAttribute("memId", 1);
        // }

        session.setAttribute("memId", 1001);
        Integer memId = 1001;
        // Integer memId = (Integer) session.getAttribute("memId");
        Map<String, Object> pageData = petService.getPetsPageData(whichPage, memId);
        System.out.println("資料筆數: " + pageData.get("petlist"));
        model.addAllAttributes(pageData); // 確保 pageData 裡面有一個 key 叫做 "petList"
        model.addAttribute("whichPage", whichPage);
        return "frontend/pet/petlistallpet2_getfromsession"; // 回傳你修正過後的「清單頁」
    }

    // 3. 保留：首頁導向

    // 4. 更新：依名稱查詢（使用 DTO 確保清單顯示正常）
    @PostMapping("/byName")
    public String getByName(@RequestParam String petName, Model model, HttpSession session) {
        if (petName == null || petName.trim().isEmpty()) {
            model.addAttribute("errorMsgs", List.of("請輸入寵物姓名"));
            return "frontend/pet/petselect";
        }

        // 從 session 拿真實 ID
        Integer currentMemId = (Integer) session.getAttribute("memId");

        List<PetDTO> list = petService.findPetsByNameDTO(petName);
        if (list.isEmpty()) {
            model.addAttribute("errorMsgs", List.of("查無此寵物姓名"));
            return "frontend/pet/petselect";
        }

        PetDTO pet = list.get(0);

        // 🔴 只在這裡加入判斷：如果不是本人，且也不是保姆（有訂單），就擋掉
        if (!pet.getMemId().equals(currentMemId) && !petService.hasOrderRelation(currentMemId, pet.getPetId())) {
            model.addAttribute("errorMsgs", List.of("您無權查看此寵物資料"));
            return "frontend/pet/petselect";
        }

        // --- 以下完全維持你原本的邏輯 (分頁/導航) ---
        Integer petId = pet.getPetId();
        List<Integer> allIds = petService.getAllPetIds(currentMemId);
        int currentIndex = allIds.indexOf(petId);
        int total = allIds.size();
        model.addAttribute("pet", pet);
        model.addAttribute("prevId", (currentIndex > 0) ? allIds.get(currentIndex - 1) : null);
        model.addAttribute("nextId", (currentIndex < total - 1) ? allIds.get(currentIndex + 1) : null);
        model.addAttribute("currentIndex", currentIndex);
        model.addAttribute("total", total);
        return "frontend/pet/petlistonepet";
    }

    // 5. 更新：單筆查詢 (維持你原本的結構)
    @GetMapping("/one")
    public String getOne(@RequestParam(value = "petId", required = false) String petIdStr, Model model,
            HttpSession session) {
        java.util.List<String> errorMsgs = new java.util.LinkedList<>();
        model.addAttribute("errorMsgs", errorMsgs);

        if (petIdStr == null || petIdStr.trim().isEmpty()) {
            errorMsgs.add("請輸入寵物編號");
            return "frontend/pet/petselect";
        }

        Integer petId = null;
        try {
            petId = Integer.valueOf(petIdStr);
        } catch (NumberFormatException e) {
            errorMsgs.add("寵物編號格式不正確");
            return "frontend/pet/petselect";
        }

        PetDTO petDTO = petService.getOnePetDTO(petId);
        if (petDTO == null) {
            errorMsgs.add("查無資料");
            return "frontend/pet/petselect";
        }

        // 🔴 關鍵判斷：從 session 拿 ID 並比對權限
        Integer currentMemId = (Integer) session.getAttribute("memId");
        if (!petDTO.getMemId().equals(currentMemId) && !petService.hasOrderRelation(currentMemId, petId)) {
            errorMsgs.add("您無權查看此寵物資料");
            return "frontend/pet/petselect";
        }

        // --- 以下完全維持你原本的成功後邏輯 ---
        List<Integer> allIds = petService.getAllPetIds(currentMemId);
        int currentIndex = allIds.indexOf(petId);
        int total = allIds.size();
        model.addAttribute("pet", petDTO);
        model.addAttribute("prevId", (currentIndex > 0) ? allIds.get(currentIndex - 1) : null);
        model.addAttribute("nextId", (currentIndex < total - 1) ? allIds.get(currentIndex + 1) : null);
        model.addAttribute("currentIndex", currentIndex);
        model.addAttribute("total", total);
        return "frontend/pet/petlistonepet";
    }

    @PostMapping("/insertBase64")
    @ResponseBody
    public String insertBase64(@ModelAttribute PetVO petVO,
            @RequestParam("petImageBase64") String petImageBase64,
            HttpSession session) {

        try {

            Integer testMemId = 1001;
            petVO.setMemId(testMemId);
            // // 1. 取得 Session 中的會員編號
            // Integer memId = (Integer) session.getAttribute("memId");
            //
            // // --- 除錯用：如果 memId 是空的，直接回傳錯誤 ---
            // if (memId == null) {
            // return "error: 登入逾時或尚未登入，請重新登入再上傳";
            // }

            // 2. 處理圖片解碼
            if (petImageBase64 != null && petImageBase64.contains(",")) {
                String base64Data = petImageBase64.split(",")[1];
                petVO.setPetImage(java.util.Base64.getDecoder().decode(base64Data));
            }

            petService.addPetBase64(petVO);
            return "success";
        } catch (Exception e) {
            e.printStackTrace();
            return "error: " + e.getMessage();
        }
    }

    // 7. 保留：一般表單新增
    @PostMapping("/insert")
    public String insert(@ModelAttribute PetVO petVO,
            @RequestParam MultipartFile petImage,
            HttpSession session) throws Exception {

        // 1. 從 Session 取得你登入時存入的 "memId" (例如 1001)
        Integer memId = (Integer) session.getAttribute("memId");

        // 2. 檢查是否登入（安全性檢查）
        if (memId == null) {
            // 如果 Session 過期或沒登入，導向登入頁面
            return "redirect:/member/login";
        }

        // 3. 關鍵步驟：將目前登入者的 ID 賦予給這隻寵物
        petVO.setMemId(memId);

        // 4. 執行新增
        petService.addPet(petVO, petImage);

        // 5. 新增完畢後跳轉（建議跳轉到顯示該會員所有寵物的頁面）
        return "redirect:/pet/all";
    }

    // 9. 新增：跳轉到新增寵物頁面，並加入權限檢查
    @GetMapping("/add_page")
    public String showAddPage(jakarta.servlet.http.HttpSession session) {
        // 🔴 權限判斷：檢查是否登入
        // Integer memId = (Integer) session.getAttribute("memId", user.getMemId());
        //
        // // --- 測試用模擬登入 (當會員功能還沒做好時，取消下面這行註解即可測試) ---
        // // if (memId == null) { session.setAttribute("memId", 1); memId = 1; }
        // // -----------------------------------------------------------
        //
        // if (memId == null) {
        // // 如果沒登入，跳轉到登入頁面 (請根據你組員設定的登入路徑修改)
        // // 目前我們先註解掉跳轉邏輯，方便你開發
        // // return "redirect:/member/login";
        //
        // // 如果想暫時讓沒登入的人也能看，就直接回傳頁面
        // return "pet_add_page";
        // }

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
            @RequestParam("petImageBase64") String petImageBase64,

            @RequestParam(required = false) String deleteImage, // 接收刪除旗標
            HttpSession session) // 記得把image後面的)去掉才能打開這行註解
            throws Exception {

        // 從 session 拿真正登入的人 ID(有會員時再打開，以及打開最上面的Http跟上面的Http註解(並且按照後面提示去小修改

        // 2. 增加安全檢查，防止 session 真的消失
        if (session == null) {
            System.out.println("⚠️ 錯誤：找不到 Session");
            return "error: session_expired";
        }

        Integer currentMemId = (Integer) session.getAttribute("memId");
        //
        // // 🔴 安全檢查：如果登入者不是寵物的主人，拒絕執行並跳回列表
        // // 你可能需要先從 DB 查出這隻寵物原本的主人是誰
        // PetDTO originalPet = petService.getOnePetDTO(petVO.getPetId());
        // if (!originalPet.getMemId().equals(currentMemId)) {
        // return "redirect:/pet/all"; // 或者導向錯誤頁面
        // }

        // 呼叫我們之前在 Service 準備好的混合更新方法
        // 如果你 Service 還沒改名，建議統一呼叫一個處理 Base64 的方法

        // 如果 ModelAttribute 沒綁定到，手動塞進去
        if (currentMemId == null) {
            return "error: 請先登入";
        }

        if (petVO.getPetId() == null && petId != null) {
            petVO.setPetId(petId);
        }

        // 2. 【新增保險絲】：攔截 petId 為 null 的情況
        if (petVO.getPetId() == null) {
            System.out.println("⚠️ [錯誤] 更新請求遺失 petId，已成功攔截防止崩潰");
            return "error: petId is missing"; // 直接回傳錯誤字串，不要往後跑 Service
        }

        PetDTO originalPet = petService.getOnePetDTO(petVO.getPetId());
        if (originalPet != null) {
            petVO.setMemId(originalPet.getMemId()); // 把舊的會員 ID 塞回 VO
        } else {
            // 如果查不到，暫時塞一個測試用 ID (例如 1)，避免報錯
            petVO.setMemId(1);
        }

        try {
            // 2. ❗ 核心步驟：撈出舊資料，確保時間與會員 ID 不遺失
            PetVO oldPet = petService.getOnePet(petVO.getPetId());
            if (oldPet == null)
                return "error: 找不到該寵物資料";

            // 繼承舊有重要欄位，防止被前端傳來的 null 覆蓋
            petVO.setMemId(oldPet.getMemId());
            petVO.setCreatedTime(oldPet.getCreatedTime());

            // 3. 處理圖片邏輯
            if (petImageBase64 != null && petImageBase64.contains(",")) {
                // A. 如果有新的 Base64 圖就解碼
                byte[] imageBytes = java.util.Base64.getDecoder().decode(petImageBase64.split(",")[1]);
                petVO.setPetImage(imageBytes);
            } else if (upFiles != null && !upFiles.isEmpty()) {
                // B. 或者是有上傳檔案 (MultipartFile)
                petVO.setPetImage(upFiles.getBytes());
            } else {
                // C. 都沒有就維持舊圖
                petVO.setPetImage(oldPet.getPetImage());
            }

            // 4. 執行更新 (建議統一呼叫 updatePetBase64 或 updatePet)
            System.out.println("===== 執行 pet update =====");
            petService.updatePetBase64(petVO);

            return "success";

        } catch (Exception e) {
            e.printStackTrace();
            return "error: " + e.getMessage();
        }
    }

    @PostMapping("/delete")

    public String deletePet(@RequestParam("petId") Integer petId, HttpSession session,
            RedirectAttributes redirectAttributes) {

        try {
            // 1. 執行刪除
            petService.deletePet(petId);

            // 3. 刪除後返回原本的列表頁面 (Dashboard)
            return "redirect:/pet/dashboard";
        } catch (Exception e) {
            return "error: 刪除失敗";
        }
    }

    /**
     * API 端點：根據保姆 ID 撈取所有評價資料
     * URL: /pet/evaluate/list/{sitterId}
     * 
     * @param sitterId 保姆 ID
     * @return 該保姆的所有評價列表 (JSON 格式)
     */
    @GetMapping("/evaluate/list/{sitterId}")
    @ResponseBody
    public ResponseEntity<List<EvaluateVO>> getReviewsBySitterId(@PathVariable Integer sitterId) {
        try {
            List<EvaluateVO> reviews = evaluateService.getReviewsBySitterId(sitterId);
            return ResponseEntity.ok(reviews);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/Petser_item")
    public String showpetset(Model model) {
        // 這裡只負責開門，讓使用者看到網頁
        return "/frontend/pet/Petser_item";
    }

    @GetMapping("/evaluate")
    public String showEvaluatePage(HttpSession session, Model model, @RequestParam Integer orderId) {
        Integer memId = (Integer) session.getAttribute("memId");
        Integer roleId = (Integer) session.getAttribute("roleId");

        BookingOrderVO order = bookingOrderSvc.getOrderById(orderId);

        if (order != null) {
            boolean isSitterOfOrder = memId.equals(order.getSitterId());
            model.addAttribute("isSitter", isSitterOfOrder);
            model.addAttribute("currentOrderId", order.getBookingOrderId());
            model.addAttribute("sitterId", order.getSitterId());
            model.addAttribute("orderInfo", order);

            // 🌟 這裡最重要：一定要撈出該訂單的所有評價列表
            // 否則 HTML 裡的 th:each="order : ${reviewGroups}" 永遠抓不到東西
            List<EvaluateDTO> reviewGroups = evaluateService.getByBookingOrderId(orderId);
            model.addAttribute("reviewGroups", reviewGroups);
        }
        // 2. 仿造人家的判斷邏輯：沒登入就踢走
        if (memId == null) {
            System.out.println("評價頁面攔截：未登入會員");
            return "redirect:/front/loginpage";
        }

        // 3. 仿造人家的資料傳遞
        model.addAttribute("memId", memId);
        model.addAttribute("currentRole", roleId); // 這裡會抓到 1001，可能是你們定義的角色代碼

        // 如果你想顯示名字，暫時抓不到 Service 就先傳個空字串或從 session 抓
        model.addAttribute("memName", session.getAttribute("memName"));

        return "frontend/evaluate";
    }

    @PostMapping("/evaluate/save")
    @ResponseBody
    public String saveEvaluate(@RequestBody Map<String, Object> payload, HttpSession session) {
        try {
            EvaluateVO vo = new EvaluateVO();

            // --- 1. 抓取訂單編號並處理變數宣告 ---
            Object orderObj = payload.get("bookingOrderId");
            String orderStr = (orderObj == null) ? "" : orderObj.toString().trim();

            // 驗證是否遺失
            if (orderStr.isEmpty() || "null".equals(orderStr) || "undefined".equals(orderStr)) {
                return "error: 遺失訂單編號 (bookingOrderId)";
            }

            // 安全轉型為 Integer
            Integer orderId = Double.valueOf(orderStr).intValue();
            vo.setBookingOrderId(orderId);

            // --- 2. 從訂單撈取資料，取得對方 ID ---
            BookingOrderVO order = bookingOrderSvc.getOrderById(orderId);
            if (order == null) {
                return "error: 找不到訂單資料";
            }

            // --- 3. 取得當前登入者 ID (senderId) ---
            Integer memId = (Integer) session.getAttribute("memId");
            if (memId == null) {
                return "error: 請先登入";
            }
            vo.setSenderId(memId);

            // --- 4. 判斷角色並設定 receiverId ---
            Object roleObj = session.getAttribute("roleId");
            String currentRole;

            // 🔴 重要：你的系統定義
            // roleId = 0 → 會員評保姆
            // roleId = 1 → 保姆評會員
            if (roleObj != null && "1".equals(roleObj.toString())) {
                // roleId = 1 → 保姆
                currentRole = "SITTER";
                vo.setReceiverId(order.getMemId()); // 保姆評價會員 → receiverId = 會員ID
            } else {
                // roleId = 0 或 null → 會員
                currentRole = "MEMBER";
                vo.setReceiverId(order.getSitterId()); // 會員評價保姆 → receiverId = 保姆ID
            }

            // --- 5. 設定其他資訊 ---
            vo.setContent(String.valueOf(payload.getOrDefault("content", "")));
            String starRating = String.valueOf(payload.getOrDefault("starRating", "5"));
            vo.setStarRating(Double.valueOf(starRating).intValue());

            // --- 6. 執行存檔 ---
            evaluateService.handleSubmission(vo, currentRole);

            return "success";
        } catch (Exception e) {
            e.printStackTrace();
            return "error: " + e.getMessage();
        }
    }
}