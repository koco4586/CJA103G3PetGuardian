package com.petguardian.member.controller.management;

import com.petguardian.member.service.management.MemberManagementDeactivateService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class MemberManagementDeactivateController {

    @Autowired
    private MemberManagementDeactivateService memberManagementDeactivateService;

    @PutMapping("/deactivate")
    public Map<String,String> deactivate(HttpSession session){

        Map<String,String> map = new HashMap<>();

        Integer memId = (Integer) session.getAttribute("memId");

        String result = memberManagementDeactivateService.deactivate(memId);

        map.put("result",result);

        return map;

    }

}
