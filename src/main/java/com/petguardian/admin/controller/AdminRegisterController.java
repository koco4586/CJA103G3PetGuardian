//package com.petguardian.admin.controller;
//
//import com.petguardian.admin.dto.AdminRegisterDTO;
//import com.petguardian.admin.service.AdminRegisterService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.util.HashMap;
//import java.util.Map;
//
//@RestController
//public class AdminRegisterController {
//
//    @Autowired
//    private AdminRegisterService adminRegisterService;
//
//    @PostMapping("/admin/register")
//    public Map<String, String> register(@RequestBody AdminRegisterDTO adminRegisterDTO) {
//
//        Map<String, String> map = new HashMap<>();
//
//        String result = adminRegisterService.register(adminRegisterDTO);
//
//        map.put("result", result);
//
//        return map;
//
//    }
//
//}
