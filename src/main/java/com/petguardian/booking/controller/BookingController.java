package com.petguardian.booking.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.petguardian.booking.model.BookingOrderVO;
import com.petguardian.booking.model.MemberDTO;
import com.petguardian.booking.model.PetDTO;
import com.petguardian.booking.service.BookingExternalDataService;
import com.petguardian.booking.service.BookingService;

import jakarta.servlet.http.HttpSession;
/**
 * 前台預約流程控制器：處理預約建立、列表查詢與取消訂單等功能
 */
//================= 對接開發標註 =================
//1. SitterID: 目前假資料預設是 1~5。
// 待【保姆】完成後，需確認 SitterVO 的主鍵名稱。
//2. MemID & PetID: 目前假資料是 1001 開始。
// 待【會員】完成後，需確認登入狀態下的 Session 如何取得當前 memId。
//3. ServiceItemId: 目前對應 service_items 表的 1(散步), 2(餵食), 3(洗澡)。
//==============================================
@Controller
@RequestMapping("/booking")
public class BookingController {
	
	@Autowired
    private BookingService bookingService;

    @Autowired
    private BookingExternalDataService externalDataService; 

    /**
     * 【1. 顯示預約表單頁面】
     * 從首頁或保母頁點擊預約後，跳轉至填寫時間的頁面
     */
    @GetMapping("/add")
    public String showAddForm(
            @RequestParam Integer sitterId,
            @RequestParam Integer memId,
            @RequestParam Integer petId,
            @RequestParam Integer serviceItemId,
            Model model) {
        try {
//        	從Interface獲取基礎資料 (如果這裡連不到資料庫，會直接跳到 catch)
            MemberDTO member = externalDataService.getMemberInfo(memId);
            PetDTO pet = externalDataService.getPetInfo(petId);
         
            // 初始化預約物件並將 RequestParam 傳入的值預填進去
            BookingOrderVO order = new BookingOrderVO();
            order.setSitterId(sitterId);
            order.setMemId(memId);
            order.setPetId(petId);
            order.setServiceItemId(serviceItemId);

            model.addAttribute("order", order);
            model.addAttribute("memberName", member.getName());
            model.addAttribute("petName", pet.getName());
         
            // 確保初始進入頁面時，錯誤訊息為空，避免顯示上一次的失敗內容
            model.addAttribute("errorMessage", null); 

        } catch (Exception e) {
            model.addAttribute("errorMessage", "資料讀取失敗。");
            model.addAttribute("order", new BookingOrderVO());
        }
        return "backend/booking/add-booking"; 
    }

    /**
     * 【2. 接收預約表單提交】
     * 作用：處理按下「確認預約」後的存檔邏輯。
     */
    @PostMapping("/submit")
    public String submitBooking(@ModelAttribute("order") BookingOrderVO order, Model model) {
        try {
        	// 呼叫 Service 執行核心邏輯：檢查時間、算錢、存檔、改排程字串
            BookingOrderVO savedOrder = bookingService.createBooking(order);
            // 成功存檔後，將含有 ID 的結果傳給「明細確認頁」
            model.addAttribute("result", savedOrder);
            return "backend/booking/detail-booking"; 
        } catch (Exception e) {
        	// 處理失敗情況
            String errorMsg = e.getMessage();
            if (errorMsg != null && errorMsg.contains("foreign key constraint fails")) {
                errorMsg = "預約失敗：指定的保姆、會員或寵物編號不存在，請回上頁重新選擇。";
            } else if (errorMsg != null && (errorMsg.contains("JPA") || errorMsg.contains("Connection"))) {
                errorMsg = "連線異常，請稍後再試。";
            } else if (errorMsg == null) {
                errorMsg = "填寫資料有誤，請檢查時間格式。";
            }
            model.addAttribute("errorMessage", errorMsg);
            reloadMemberPetData(order, model); // 失敗時重新抓取名字
            return "backend/booking/add-booking";
        }
    }
    
    /**
     * 【3. 查詢會員預約列表】
     * 讓會員查看自己過去與未來的預約清單。
     * @param memId 會員編號
     */
    @GetMapping("/list/member/{memId}")
    public String memberBookingList(@PathVariable Integer memId, Model model) {
        // 需在 Service 實作 getOrdersByMemberId 方法並回傳清單
         List<BookingOrderVO> list = bookingService.getOrdersByMemberId(memId);
      // 幫每筆訂單補上名字
         for (BookingOrderVO order : list) {
             String name = externalDataService.getSitterInfo(order.getSitterId(), order.getServiceItemId()).getSitterName();
             order.setSitterName(name);
         }
         
       // model.addAttribute("bookingList", list);
      // 2. 把這份清單取名為 "bookingList" 送往網頁
         model.addAttribute("bookingList", list);
         model.addAttribute("memId", memId);
        return "backend/booking/booking-member-list"; 
    }

    /**
     * 【4. 查詢保母預約清單】
     * 讓保母查看目前收到的所有服務請求。
     * @param sitterId 保母編號
     */
    @GetMapping("/list/sitter/{sitterId}")
    public String sitterBookingList(@PathVariable Integer sitterId, Model model) {
        // 需在 Service 實作 getOrdersBySitterId 方法
        // model.addAttribute("bookingList", bookingService.getOrdersBySitterId(sitterId));
        return "backend/booking/booking-sitter-list";
    }

    /**
     * 【5. 取消預約】
     * 會員或保母取消尚未開始的預約，並寫入取消原因。
     * @param orderId 訂單編號
     * @param reason 取消的原因
     */
    @PostMapping("/cancel")
    public String cancelBooking(@RequestParam Integer orderId, @RequestParam String reason, RedirectAttributes ra) {
        try {
        	// 1. 先找出這筆訂單，才知道要把頁面導回哪一個會員
            BookingOrderVO order = bookingService.getOrderById(orderId); 
            Integer memberId = order.getMemId();

            // 2. 執行取消邏輯 (需要 Service 有這個方法)
            bookingService.cancelBooking(orderId, reason);
            
            // 3. 重點：導回正確的會員 ID 路徑
            ra.addFlashAttribute("successMessage", "訂單已成功取消");
            return "redirect:/booking/list/member/" + memberId;
        } catch (Exception e) {
        	// 在 Redirect 時要傳遞錯誤訊息，必須使用 RedirectAttributes
            ra.addFlashAttribute("errorMessage", "取消失敗：" + e.getMessage());
            return "redirect:/error"; // 跳轉到錯誤頁面
        }
    }
    
    /**
     * 【側邊欄連接點：查詢會員預約列表】
     * 作用：讓側邊欄點擊後，自動抓 Session 的 ID 並導向列表頁。
     */
    @GetMapping("/memberOrders")
    public String listMemberOrders(HttpSession session, Model model) {
        // --- 1. 取得登入會員 ID ---
        Integer memId = (Integer) session.getAttribute("memId");

        // 【測試用】還沒登入功能，Session 會是空的，這時強行給它 1001 方便測試
        if (memId == null) {
            memId = 1001; 
        }

        // --- 2. 獲取訂單清單 ---
        List<BookingOrderVO> orders = bookingService.getOrdersByMemberId(memId);

        // --- 3. 補上顯示用的名稱 (讓頁面顯示保母名字而不是只有 ID) ---
        for (BookingOrderVO order : orders) {
            try {
                String name = externalDataService.getSitterInfo(order.getSitterId(), order.getServiceItemId()).getSitterName();
                order.setSitterName(name);
            } catch (Exception e) {
                order.setSitterName("未知保母");
            }
        }

        // --- 4. 資料傳往前端 ---
        model.addAttribute("bookingList", orders); // 注意：這裡名稱要跟 HTML 裡的 th:each 一致
        model.addAttribute("memId", memId);

        // --- 5. 跳轉頁面 ---
        return "backend/booking/booking-member-list"; 
    }


    /**
     * 當表單提交失敗回原頁面時，重新加載顯示用的名稱資料。
     */
    private void reloadMemberPetData(BookingOrderVO order, Model model) {
        try {
            model.addAttribute("memberName", externalDataService.getMemberInfo(order.getMemId()).getName());
            model.addAttribute("petName", externalDataService.getPetInfo(order.getPetId()).getName());
        } catch (Exception ex) {
            model.addAttribute("memberName", "未知使用者");
            model.addAttribute("petName", "未知寵物");
        }
    }
}