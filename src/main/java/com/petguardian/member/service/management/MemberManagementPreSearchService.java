package com.petguardian.member.service.management;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.petguardian.member.dto.MemberManagementPreSearchDTO;
import com.petguardian.member.model.Member;
import com.petguardian.member.repository.management.MemberManagementPreSearchRepository;

@Service
public class MemberManagementPreSearchService {

	@Autowired
	private MemberManagementPreSearchRepository memberManagementPreSearchRepository;

	public MemberManagementPreSearchDTO presearchmember(Integer memId) {

		Member member = memberManagementPreSearchRepository.findById(memId).orElse(null);

		MemberManagementPreSearchDTO memberManagementPreSearchDTO = new MemberManagementPreSearchDTO();

		memberManagementPreSearchDTO.setMemImage(member.getMemImage());

		memberManagementPreSearchDTO.setMemName(member.getMemName());

		memberManagementPreSearchDTO.setMemAcc(member.getMemAcc());

		memberManagementPreSearchDTO.setMemUid(member.getMemUid());

		memberManagementPreSearchDTO.setMemBth(member.getMemBth());

		memberManagementPreSearchDTO.setMemSex(member.getMemSex());

		memberManagementPreSearchDTO.setMemEmail(member.getMemEmail());

		memberManagementPreSearchDTO.setMemTel(member.getMemTel());

		memberManagementPreSearchDTO.setMemAdd(member.getMemAdd());

		memberManagementPreSearchDTO.setMemAccountNumber(member.getMemAccountNumber());

		return memberManagementPreSearchDTO;

	}

}
