package com.petguardian.member.service.management;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import com.petguardian.member.dto.MemberManagementUpdateDTO;
import com.petguardian.member.model.Member;
import com.petguardian.member.repository.management.MemberManagementRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class MemberManagementService {

	@Autowired
	private MemberManagementRepository memberManagementRepository;

	private static final String REALPATH = "src/main/resources/static/images/member/";

	public Member update(MultipartFile fileImagePath, MemberManagementUpdateDTO memberManagementUpdateDTO,
			Integer memId) {

		Member member1 = null;

		try {

			// 圖片存本機
			String fakePath = null;
			
			if (fileImagePath != null) {
			Path uploadDir = Paths.get(REALPATH).toAbsolutePath(); //Path是一個類別， get() 方法會將字串路徑轉換成 Path 物件，toAbsolutePath()會將相對路徑轉換為絕對路徑
			
			Files.createDirectories(uploadDir); //會創建目錄，包括所有不存在的父目錄

			String fileName = UUID.randomUUID().toString() + "_" + fileImagePath.getOriginalFilename();// 照片檔名（隨機碼＿照片檔名）

			Path targetPath = uploadDir.resolve(fileName); //將fileName拼接在uploadDir後面,檔案上傳的目錄路徑+檔案名稱=組合後的完整檔案路徑

			fileImagePath.transferTo(targetPath.toFile());// transferTo():創建指定路徑檔案，並且把使用者上傳照片內容寫入該檔案,targetPath.toFile():將 Path 物件轉換為 File 物件
			
			fakePath = "/member/" + fileName;
			
			}
			// 假路徑存資料庫
			Member member = memberManagementRepository.findById(memId).orElse(null);

			member.setMemId(memId);
			member.setMemImage(fakePath);
			member.setMemName(memberManagementUpdateDTO.getMemName());
			member.setMemAcc(memberManagementUpdateDTO.getMemAcc());
			member.setMemUid(memberManagementUpdateDTO.getMemUid());
			member.setMemBth(memberManagementUpdateDTO.getMemBth());
			member.setMemSex(memberManagementUpdateDTO.getMemSex());
			member.setMemEmail(memberManagementUpdateDTO.getMemEmail());
			member.setMemTel(memberManagementUpdateDTO.getMemTel());
			member.setMemAdd(memberManagementUpdateDTO.getMemAdd());
			member.setMemAccountNumber(memberManagementUpdateDTO.getMemAccountNumber());

			member1 = memberManagementRepository.save(member);

			return member1;

		} catch (IOException e) {
			// 處理錯誤，例如記錄日誌或返回錯誤訊息
			e.printStackTrace();
			return member1;
		}

	}

}
