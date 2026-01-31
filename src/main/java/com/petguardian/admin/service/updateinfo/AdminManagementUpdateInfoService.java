package com.petguardian.admin.service.updateinfo;

import java.io.File;
import java.io.IOException;
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

	private static final String REALPATH = "/Users/CJA103G3_Workspace/CJA103G3PetGuardian/src/main/resources/static/images/backend/admin/adminupdateinfo/";

	public Admin adminupdateinfo(MultipartFile fileImagePath, AdminManagementUpdateInfoDTO adminManagementUpdateInfoDTO,
			Integer admId) {

		Admin admin1 = null;

		try {

			// 圖片存本機

			File dir = new File(REALPATH);// "記憶體"中創建一個存有路徑的File物件
			if (!dir.exists()) {
				dir.mkdirs();// 創建資料夾

			}

			String fileName = UUID.randomUUID().toString() + "_" + fileImagePath.getOriginalFilename();// 照片檔名（隨機碼＿照片檔名）

			String filePath = REALPATH + fileName;// 檔案上傳的目錄路徑+檔案名稱=組合後的完整檔案路徑

			fileImagePath.transferTo(new File(filePath));// 創建指定路徑檔案，並且把使用者上傳照片內容寫入該檔案
			// 假路徑存資料庫

			Admin admin = adminManagementUpdateInfoRepository.findById(admId).orElse(null);

			admin.setAdmId(admId);
			admin.setAdmImage("/adminupdateinfo/" + fileName);
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
