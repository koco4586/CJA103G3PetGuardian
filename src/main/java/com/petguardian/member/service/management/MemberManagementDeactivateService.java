package com.petguardian.member.service.management;

import com.petguardian.member.model.Member;
import com.petguardian.member.repository.management.MemberManagementDeactivateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MemberManagementDeactivateService {

    @Autowired
    private MemberManagementDeactivateRepository memberManagementDeactivateRepository;

    public String deactivate(Integer memId){

        Member member = memberManagementDeactivateRepository.findById(memId).orElse(null);

        member.setMemStatus(0);

        memberManagementDeactivateRepository.save(member);

        return "停用成功";

    }

}
