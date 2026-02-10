package com.petguardian.admin.service.updateinfo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.petguardian.admin.dto.updateinfo.AdminManagementUpdateInfoDTO;
import com.petguardian.admin.model.Admin;
import com.petguardian.admin.repository.updateinfo.AdminManagementUpdateInfoRepository;

@Service
public class AdminManagementUpdateInfoService {

	@Autowired
	private AdminManagementUpdateInfoRepository adminManagementUpdateInfoRepository;

	private static final String REALPATH = "src/main/resources/static/images/backend/admin/";

	public Admin adminupdateinfo(MultipartFile fileImagePath, AdminManagementUpdateInfoDTO adminManagementUpdateInfoDTO,
			Integer admId) {

		Admin admin1 = null;

		try {

			// 圖片存本機

			Path uploadDir = Paths.get(REALPATH).toAbsolutePath(); // Path是一個類別， get() 方法會將字串路徑轉換成 Path
																	// 物件，toAbsolutePath()會將相對路徑轉換為絕對路徑

			Files.createDirectories(uploadDir); // 會創建目錄，包括所有不存在的父目錄

			String fileName = UUID.randomUUID().toString() + "_" + fileImagePath.getOriginalFilename();// 照片檔名（隨機碼＿照片檔名）

			Path targetPath = uploadDir.resolve(fileName); // 將fileName拼接在uploadDir後面,檔案上傳的目錄路徑+檔案名稱=組合後的完整檔案路徑

			fileImagePath.transferTo(targetPath.toFile());// transferTo():創建指定路徑檔案，並且把使用者上傳照片內容寫入該檔案,targetPath.toFile():將
															// Path 物件轉換為 File 物件

			// 假路徑存資料庫

			Admin admin = adminManagementUpdateInfoRepository.findById(admId).orElse(null);

			admin.setAdmId(admId);
			admin.setAdmImage("/admin/" + fileName);
			admin.setAdmName(adminManagementUpdateInfoDTO.getAdmName());
			admin.setAdmAccount(adminManagementUpdateInfoDTO.getAdmAccount());
			admin.setAdmEmail(adminManagementUpdateInfoDTO.getAdmEmail());
			admin.setAdmTel(adminManagementUpdateInfoDTO.getAdmTel());

			admin1 = adminManagementUpdateInfoRepository.save(admin);

			return admin1;
		} catch (IOException e) {
			// 處理錯誤，例如記錄日誌或返回錯誤訊息
			e.printStackTrace();
			return admin1;
		}
	}
}
