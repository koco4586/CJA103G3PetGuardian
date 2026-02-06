package com.petguardian.backend.controller;

import com.petguardian.admin.model.Admin;
import com.petguardian.admin.repository.login.AdminLoginRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminCurrentController {

    @Autowired
    private AdminLoginRepository adminLoginRepository;

    @GetMapping("/current-admin")
    public Map<String, Object> getCurrentAdmin(HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        Integer admId = (Integer) session.getAttribute("admId");

        if (admId == null) {
            return response;
        }

        Admin admin = adminLoginRepository.findById(admId).orElse(null);

        if (admin != null) {
            response.put("adminId", admin.getAdmId());
            response.put("name", admin.getAdmName());
        }

        return response;
    }
}