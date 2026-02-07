package com.petguardian.admin.service.updateinfo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

import com.petguardian.admin.dto.updateinfo.AdminManagementUpdateInfoDTO;
import com.petguardian.admin.model.Admin;
import com.petguardian.admin.repository.updateinfo.AdminManagementUpdateInfoRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class AdminManagementUpdateInfoService {

	@Autowired
	private AdminManagementUpdateInfoRepository adminManagementUpdateInfoRepository;

	@Value("${file.upload.admin.path}")
	private String uploadPath;

	private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");
	private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

	public Admin adminupdateinfo(MultipartFile fileImagePath, AdminManagementUpdateInfoDTO adminManagementUpdateInfoDTO,
			Integer admId) {
		try {
			// 先取得管理員資料
			Admin admin = adminManagementUpdateInfoRepository.findById(admId)
					.orElseThrow(() -> new IllegalArgumentException("找不到管理員 ID: " + admId));

			// 只有在有上傳新圖片時才處理圖片
			if (fileImagePath != null && !fileImagePath.isEmpty()) {
				// 1. 驗證檔案
				validateFile(fileImagePath);

				// 2. 清理原始檔名（防止路徑遍歷）
				String originalFilename = fileImagePath.getOriginalFilename();
				String sanitizedFilename = new File(originalFilename).getName();

				// 3. 驗證副檔名
				String extension = getFileExtension(sanitizedFilename);
				if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
					throw new IllegalArgumentException("不允許的檔案格式,僅支援: " + ALLOWED_EXTENSIONS);
				}

				// 4. 生成安全的檔名（只用 UUID + 副檔名）
				String safeFileName = UUID.randomUUID().toString() + "." + extension;

				// 5. 建立目標目錄
				Path uploadDir = Paths.get(uploadPath).toAbsolutePath().normalize();
				Files.createDirectories(uploadDir);

				// 6. 完整的目標檔案路徑
				Path targetPath = uploadDir.resolve(safeFileName).normalize();

				// 7. 驗證路徑安全性（防止路徑遍歷攻擊）
				if (!targetPath.startsWith(uploadDir)) {
					throw new SecurityException("檢測到路徑遍歷攻擊");
				}

				// 8. 儲存檔案
				fileImagePath.transferTo(targetPath.toFile());

				// 9. 更新圖片路徑
				admin.setAdmImage("/images/backend/adminupdateinfo/" + safeFileName); // 網頁訪問路徑
			}
			// 如果沒有上傳新圖片，保留原本的圖片路徑（不更新 admImage）

			// 更新其他資料
			admin.setAdmName(adminManagementUpdateInfoDTO.getAdmName());
			admin.setAdmAccount(adminManagementUpdateInfoDTO.getAdmAccount());
			admin.setAdmEmail(adminManagementUpdateInfoDTO.getAdmEmail());
			admin.setAdmTel(adminManagementUpdateInfoDTO.getAdmTel());

			return adminManagementUpdateInfoRepository.save(admin);

		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("檔案上傳失敗: " + e.getMessage(), e);
		}
	}

	private void validateFile(MultipartFile file) {
		// 修改：不再檢查 null 或 isEmpty（因為這是可選的）
		if (file.getSize() > MAX_FILE_SIZE) {
			throw new IllegalArgumentException("檔案大小不能超過 10MB");
		}
		String originalFilename = file.getOriginalFilename();
		if (originalFilename == null || originalFilename.trim().isEmpty()) {
			throw new IllegalArgumentException("檔案名稱無效");
		}
	}

	private String getFileExtension(String filename) {
		int lastDot = filename.lastIndexOf('.');
		if (lastDot == -1) {
			throw new IllegalArgumentException("檔案缺少副檔名");
		}
		return filename.substring(lastDot + 1);
	}
}

//package com.petguardian.admin.service.updateinfo;
//
//import java.io.File;
//import java.io.IOException;
//import java.util.UUID;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//import com.petguardian.admin.dto.updateinfo.AdminManagementUpdateInfoDTO;
//import com.petguardian.admin.model.Admin;
//import com.petguardian.admin.repository.updateinfo.AdminManagementUpdateInfoRepository;
//
//@Service
//public class AdminManagementUpdateInfoService {
//
//	@Autowired
//	private AdminManagementUpdateInfoRepository adminManagementUpdateInfoRepository;
//
//	private static final String REALPATH = "/Users/CJA103G3_Workspace/CJA103G3PetGuardian/src/main/resources/static/images/backend/admin/adminupdateinfo/";
//
//	public Admin adminupdateinfo(MultipartFile fileImagePath, AdminManagementUpdateInfoDTO adminManagementUpdateInfoDTO,
//			Integer admId) {
//
//		Admin admin1 = null;
//
//		try {
//
//			// 圖片存本機
//
//			File dir = new File(REALPATH);// "記憶體"中創建一個存有路徑的File物件
//			if (!dir.exists()) {
//				dir.mkdirs();// 創建資料夾
//
//			}
//
//			String fileName = UUID.randomUUID().toString() + "_" + fileImagePath.getOriginalFilename();// 照片檔名（隨機碼＿照片檔名）
//
//			String filePath = REALPATH + fileName;// 檔案上傳的目錄路徑+檔案名稱=組合後的完整檔案路徑
//
//			fileImagePath.transferTo(new File(filePath));// 創建指定路徑檔案，並且把使用者上傳照片內容寫入該檔案
//			// 假路徑存資料庫
//
//			Admin admin = adminManagementUpdateInfoRepository.findById(admId).orElse(null);
//
//			admin.setAdmId(admId);
//			admin.setAdmImage("/adminupdateinfo/" + fileName);
//			admin.setAdmName(adminManagementUpdateInfoDTO.getAdmName());
//			admin.setAdmAccount(adminManagementUpdateInfoDTO.getAdmAccount());
//			admin.setAdmEmail(adminManagementUpdateInfoDTO.getAdmEmail());
//			admin.setAdmTel(adminManagementUpdateInfoDTO.getAdmTel());
//
//			admin1 = adminManagementUpdateInfoRepository.save(admin);
//
//			return admin1;
//		} catch (IOException e) {
//			// 處理錯誤，例如記錄日誌或返回錯誤訊息
//			e.printStackTrace();
//			return admin1;
//		}
//	}
//}
