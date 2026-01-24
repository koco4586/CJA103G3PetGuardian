package com.petguardian.member.controller.management;

import com.petguardian.member.dto.MemberManagementUpdateDTO;
import com.petguardian.member.model.Member;
import com.petguardian.member.service.management.MemberManagementService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class MemberManagementController {

    @Autowired
    private MemberManagementService memberManagementService;

    @PutMapping("/management")
    public Member update(@RequestParam MultipartFile memImage,
                         @ModelAttribute MemberManagementUpdateDTO memberManagementUpdateDTO,
                         HttpSession session){

        Integer memId = (Integer) session.getAttribute("memId");

        return memberManagementService.update(memImage, memberManagementUpdateDTO,memId);

    }

}
