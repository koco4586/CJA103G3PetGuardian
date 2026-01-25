package com.petguardian.member.controller.management;

import com.petguardian.member.dto.MemberManagementResetPwDTO;
import com.petguardian.member.service.management.MemberManagementResetPwService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/front")
public class MemberManagementResetPwController {

    @Autowired
    private MemberManagementResetPwService memberManagementResetPwService;

    @PutMapping("/resetpw")
    public Map<String,String> resetpw(@RequestBody MemberManagementResetPwDTO memberManagementResetPwDTO, HttpSession session){

        Map<String,String> map = new HashMap<>();

        Integer memId = (Integer)session.getAttribute("memId");

        String result = memberManagementResetPwService.resetpw(memberManagementResetPwDTO,memId);

        map.put("result",result);

        return map;

    }
}
