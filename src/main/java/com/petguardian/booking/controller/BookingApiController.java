package com.petguardian.booking.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.petguardian.booking.model.BookingOrderVO;
import com.petguardian.booking.service.BookingService;
import com.petguardian.common.service.AuthStrategyService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * 前台預約資料處理控制器
 * 負責：所有 POST 請求和 AJAX 請求，回傳 JSON 或執行動作
 */
@RestController  // 使用 @RestController 自動回傳 JSON
@RequestMapping("/booking")
public class BookingApiController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private AuthStrategyService authStrategyService;

    /**
     * 【切換收藏保姆】
     * 1. 檢查使用者是否登入
     * 2. 如果已收藏該保姆 → 移除收藏
     * 3. 如果未收藏該保姆 → 新增收藏
     * 
     * @param sitterId 保姆 ID
     * @param session HTTP Session（用來取得登入使用者 ID）
     * @return ResponseEntity<String> - "added" 或 "removed"
     */
    @PostMapping("/toggleFavorite/{sitterId}")
    public ResponseEntity<String> toggleFavorite(
            @PathVariable Integer sitterId, 
            HttpSession session) {
        
        // 1. 從 Session 取得登入使用者的 ID
        Integer memId = (Integer) session.getAttribute("memId");
        
        // 2. 檢查是否登入
        if (memId == null) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)  // 401 未授權
                .body("請先登入");
        }

        // 3. 呼叫 Service 執行「切換收藏」邏輯
        //    - 如果已收藏 → 刪除收藏，回傳 false
        //    - 如果未收藏 → 新增收藏，回傳 true
        boolean isFavorited = bookingService.toggleSitterFavorite(memId, sitterId);
        
        // 4. 讓前端知道是新增還是移除
        return ResponseEntity.ok(isFavorited ? "added" : "removed");
    }

    /**
     * 【提交預約表單】
     * 1. 檢查使用者是否登入
     * 2. 將表單資料儲存為新的預約單
     * 3. 回傳新建立的預約單 ID
     * 
     * @param order 預約單資料（從表單綁定）
     * @param request HttpServletRequest（用來取得登入使用者 ID）
     * @return ResponseEntity<?> - 成功回傳訂單 ID，失敗回傳錯誤訊息
     */
    @PostMapping("/submit")
    public ResponseEntity<?> submitBooking(
            @ModelAttribute("order") BookingOrderVO order, 
            HttpServletRequest request) {
        
        try {
            // 1. 取得當前登入使用者 ID
            Integer currentUserId = authStrategyService.getCurrentUserId(request);
            
            // 2. 檢查是否登入
            if (currentUserId == null) {
                return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)  // 401 未授權
                    .body("請先登入");
            }

            // 3. 將登入使用者 ID 設定到預約單
            order.setMemId(currentUserId);
            
            // 4. 呼叫 Service 建立預約（會檢查時段、扣款等）
            BookingOrderVO savedOrder = bookingService.createBooking(order);

            // 5. 回傳成功訊息與訂單 ID（前端可用來導向訂單詳情頁）
            return ResponseEntity.ok(savedOrder.getBookingOrderId());
            
        } catch (Exception e) {
            // 6. 如果建立失敗，回傳錯誤訊息
            return ResponseEntity
                .badRequest()  // 400 錯誤請求
                .body(e.getMessage());
        }
    }

    /**
     * 【取消預約（傳統表單提交）】
     * 1. 根據訂單 ID 和取消原因執行取消
     * 2. 使用 RedirectAttributes 傳遞成功/失敗訊息
     * 3. 重新導向到會員預約管理頁面
     * @param orderId 訂單 ID
     * @param reason 取消原因
     * @param ra RedirectAttributes（用來傳遞 Flash 訊息）
     * @return String - 重新導向路徑
     */
    @PostMapping("/cancel")
    public String cancelBooking(
            @RequestParam Integer orderId, 
            @RequestParam String reason, 
            RedirectAttributes ra) {
        
        try {
            // 1. 呼叫 Service 執行取消預約
            bookingService.cancelBooking(orderId, reason);
            
            // 2. 加入成功訊息（會在重新導向後的頁面顯示）
            ra.addFlashAttribute("successMessage", "訂單已成功取消");
            
        } catch (Exception e) {
            // 3. 如果取消失敗，加入錯誤訊息
            ra.addFlashAttribute("errorMessage", "取消失敗：" + e.getMessage());
        }
        
        // 4. 重新導向到會員預約管理頁面
        return "redirect:/booking/memberOrders";
    }

    /**
     * 【處理退款並取消預約】
     * 1. 執行取消預約邏輯（包含退款審核、更改訂單狀態、釋出保母排程）
     * 2. 回傳操作結果
     * 
     * @param orderId 訂單 ID
     * @return ResponseEntity<String> - 成功回傳 "取消成功"，失敗回傳錯誤訊息
     */
    @PostMapping("/refund/{orderId}")
    public ResponseEntity<String> handleRefund(@PathVariable Integer orderId) {
        
        try {
            // 1. 呼叫 Service 執行取消邏輯
            //    包含：
            //    - 更改訂單狀態為「已取消」
            //    - 建立退款審核記錄
            //    - 釋出保母的時段排程
            bookingService.cancelBooking(orderId, "會員自行取消");
            
            // 2. 回傳成功訊息
            return ResponseEntity.ok("取消成功");
            
        } catch (Exception e) {
            // 3. 如果處理失敗，回傳錯誤訊息
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)  // 400 錯誤請求
                .body("取消失敗：" + e.getMessage());
        }
    }
}