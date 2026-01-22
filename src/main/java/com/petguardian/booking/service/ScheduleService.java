package com.petguardian.booking.service;

import org.springframework.stereotype.Service;

@Service
public class ScheduleService {
	public String updateStatusString(String currentStatus, int start, int end, char target) {
        // 先將 24 位元的字串轉為字元陣列，方便修改特定位置
        char[] statusArray = currentStatus.toCharArray();
        
        // 使用方法參數中定義的 start 和 end
        for (int i = start; i < end; i++) {
            // 確保 index 在 0-23 之間，避免陣列越界
            if (i >= 0 && i < 24) {
                statusArray[i] = target; // 使用參數 target
            }
        }
        
        // 將修改後的陣列轉回字串
        return new String(statusArray);
    }
}
