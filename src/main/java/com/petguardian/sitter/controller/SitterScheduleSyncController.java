package com.petguardian.sitter.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.petguardian.sitter.service.SitterScheduleSyncService;

@Controller
@RequestMapping("/sitter")
public class SitterScheduleSyncController {

    @Autowired
    private SitterScheduleSyncService syncService;

    /**
     * 資料修復工具：同步所有訂單至排程表
     * URL: /sitter/api/sync-schedule
     * 用途：解決舊訂單在行事曆上未顯示為「橘色」的問題，並清除已取消訂單的佔用
     */
    @GetMapping("/api/sync-schedule")
    @ResponseBody
    public ResponseEntity<?> syncSchedule() {
        try {
            Map<String, Object> result = syncService.syncSchedule();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("message", "同步失敗: " + e.getMessage()));
        }
    }
}
