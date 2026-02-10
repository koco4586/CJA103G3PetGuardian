package com.petguardian.member.controller.management;

import com.petguardian.member.dto.MemberManagementUpdateDTO;
import com.petguardian.member.model.Member;
import com.petguardian.member.service.management.MemberManagementService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/front")
public class MemberManagementController {

    @Autowired
    private MemberManagementService memberManagementService;

    @PutMapping("/management")
    public Member update(@RequestParam(required = false) MultipartFile memImage,// 添加 required
                         @ModelAttribute @Valid MemberManagementUpdateDTO memberManagementUpdateDTO,
                         HttpSession session){

        Integer memId = (Integer) session.getAttribute("memId");

        return memberManagementService.update(memImage, memberManagementUpdateDTO,memId);

    }

}
