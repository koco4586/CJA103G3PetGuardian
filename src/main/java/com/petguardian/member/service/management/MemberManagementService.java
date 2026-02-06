package com.petguardian.member.service.management;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;
import com.petguardian.member.dto.MemberManagementUpdateDTO;
import com.petguardian.member.model.Member;
import com.petguardian.member.repository.management.MemberManagementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class MemberManagementService {

	@Autowired
	private MemberManagementRepository memberManagementRepository;

	@Value("${file.upload.path}")
	private String uploadPath;

	private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");
	private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

	public Member update(MultipartFile fileImagePath, MemberManagementUpdateDTO memberManagementUpdateDTO,
			Integer memId) {

		try {
			// 查詢會員
			Member member = memberManagementRepository.findById(memId)
					.orElseThrow(() -> new IllegalArgumentException("找不到會員 ID: " + memId));

			// 👇 只有在有上傳圖片時才處理圖片
			if (fileImagePath != null && !fileImagePath.isEmpty()) {
				// 1. 驗證檔案
				validateFile(fileImagePath);

				// 2. 清理原始檔名（防止路徑遍歷）
				String originalFilename = fileImagePath.getOriginalFilename();
				String sanitizedFilename = new File(originalFilename).getName();

				// 3. 驗證副檔名
				String extension = getFileExtension(sanitizedFilename);
				if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
					throw new IllegalArgumentException("不允許的檔案格式，僅支援: " + ALLOWED_EXTENSIONS);
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
				member.setMemImage("/images/member/" + safeFileName); // 網頁訪問路徑
			}
			// 如果沒有上傳圖片，保持原有的圖片路徑不變

			// 更新其他資料
			member.setMemName(memberManagementUpdateDTO.getMemName());
			member.setMemAcc(memberManagementUpdateDTO.getMemAcc());
			member.setMemUid(memberManagementUpdateDTO.getMemUid());
			member.setMemBth(memberManagementUpdateDTO.getMemBth());
			member.setMemSex(memberManagementUpdateDTO.getMemSex());
			member.setMemEmail(memberManagementUpdateDTO.getMemEmail());
			member.setMemTel(memberManagementUpdateDTO.getMemTel());
			member.setMemAdd(memberManagementUpdateDTO.getMemAdd());
			member.setMemAccountNumber(memberManagementUpdateDTO.getMemAccountNumber());

			return memberManagementRepository.save(member);

		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("檔案上傳失敗: " + e.getMessage(), e);
		}
	}

	private void validateFile(MultipartFile file) {
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

//package com.petguardian.member.service.management;
//
//import java.io.File;
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.Set;
//import java.util.UUID;
//import com.petguardian.member.dto.MemberManagementUpdateDTO;
//import com.petguardian.member.model.Member;
//import com.petguardian.member.repository.management.MemberManagementRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//@Service
//public class MemberManagementService {
//
//	@Autowired
//	private MemberManagementRepository memberManagementRepository;
//
//	@Value("${file.upload.path}")
//	private String uploadPath;
//
//	private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");
//	private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
//
//	public Member update(MultipartFile fileImagePath, MemberManagementUpdateDTO memberManagementUpdateDTO,
//			Integer memId) {
//
//		try {
//			// 1. 驗證檔案
//			validateFile(fileImagePath);
//
//			// 2. 清理原始檔名（防止路徑遍歷）
//			String originalFilename = fileImagePath.getOriginalFilename();
//			String sanitizedFilename = new File(originalFilename).getName();
//
//			// 3. 驗證副檔名
//			String extension = getFileExtension(sanitizedFilename);
//			if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
//				throw new IllegalArgumentException("不允許的檔案格式，僅支援: " + ALLOWED_EXTENSIONS);
//			}
//
//			// 4. 生成安全的檔名（只用 UUID + 副檔名）
//			String safeFileName = UUID.randomUUID().toString() + "." + extension;
//
//			// 5. 建立目標目錄
//			Path uploadDir = Paths.get(uploadPath).toAbsolutePath().normalize();
//			Files.createDirectories(uploadDir);
//
//			// 6. 完整的目標檔案路徑
//			Path targetPath = uploadDir.resolve(safeFileName).normalize();
//
//			// 7. 驗證路徑安全性（防止路徑遍歷攻擊）
//			if (!targetPath.startsWith(uploadDir)) {
//				throw new SecurityException("檢測到路徑遍歷攻擊");
//			}
//
//			// 8. 儲存檔案
//			fileImagePath.transferTo(targetPath.toFile());
//
//			// 9. 更新資料庫（存相對於 static 的路徑）
//			Member member = memberManagementRepository.findById(memId)
//					.orElseThrow(() -> new IllegalArgumentException("找不到會員 ID: " + memId));
//
//			member.setMemId(memId);
//			member.setMemImage("/images/member/" + safeFileName); // 網頁訪問路徑
//			member.setMemName(memberManagementUpdateDTO.getMemName());
//			member.setMemAcc(memberManagementUpdateDTO.getMemAcc());
//			member.setMemUid(memberManagementUpdateDTO.getMemUid());
//			member.setMemBth(memberManagementUpdateDTO.getMemBth());
//			member.setMemSex(memberManagementUpdateDTO.getMemSex());
//			member.setMemEmail(memberManagementUpdateDTO.getMemEmail());
//			member.setMemTel(memberManagementUpdateDTO.getMemTel());
//			member.setMemAdd(memberManagementUpdateDTO.getMemAdd());
//			member.setMemAccountNumber(memberManagementUpdateDTO.getMemAccountNumber());
//
//			return memberManagementRepository.save(member);
//
//		} catch (IOException e) {
//			e.printStackTrace();
//			throw new RuntimeException("檔案上傳失敗: " + e.getMessage(), e);
//		}
//	}
//
//	private void validateFile(MultipartFile file) {
//		if (file == null || file.isEmpty()) {
//			throw new IllegalArgumentException("檔案不能為空");
//		}
//
//		if (file.getSize() > MAX_FILE_SIZE) {
//			throw new IllegalArgumentException("檔案大小不能超過 10MB");
//		}
//
//		String originalFilename = file.getOriginalFilename();
//		if (originalFilename == null || originalFilename.trim().isEmpty()) {
//			throw new IllegalArgumentException("檔案名稱無效");
//		}
//	}
//
//	private String getFileExtension(String filename) {
//		int lastDot = filename.lastIndexOf('.');
//		if (lastDot == -1) {
//			throw new IllegalArgumentException("檔案缺少副檔名");
//		}
//		return filename.substring(lastDot + 1);
//	}
//}
//
////package com.petguardian.member.service.management;
////
////import java.io.File;
////import java.io.IOException;
////import java.util.UUID;
////
////import com.petguardian.member.dto.MemberManagementSelectDTO;
////import com.petguardian.member.dto.MemberManagementUpdateDTO;
////import com.petguardian.member.model.Member;
////import com.petguardian.member.repository.management.MemberManagementRepository;
////import org.springframework.beans.factory.annotation.Autowired;
////import org.springframework.stereotype.Service;
////import org.springframework.web.multipart.MultipartFile;
////
////@Service
////public class MemberManagementService {
////
////    @Autowired
////    private MemberManagementRepository memberManagementRepository;
////
////    private static final String REALPATH = "/Users/CJA103G3_Workspace/CJA103G3PetGuardian/src/main/resources/static/images/member/";
////
////    public Member update(MultipartFile fileImagePath,
////            MemberManagementUpdateDTO memberManagementUpdateDTO,
////            Integer memId) {
////
////        Member member1 = null;
////
////        try {
////
////            // 圖片存本機
////
////            File dir = new File(REALPATH);// "記憶體"中創建一個存有路徑的File物件
////            if (!dir.exists()) {
////                dir.mkdirs();// 創建資料夾
////            }
////
////            String fileName = UUID.randomUUID().toString() + "_" + fileImagePath.getOriginalFilename();// 照片檔名（隨機碼＿照片檔名）
////
////            String filePath = REALPATH + fileName;// 檔案上傳的目錄路徑+檔案名稱=組合後的完整檔案路徑
////
////            fileImagePath.transferTo(new File(filePath));// 創建指定路徑檔案，並且把使用者上傳照片內容寫入該檔案
////            // 假路徑存資料庫
////
////            Member member = memberManagementRepository.findById(memId).orElse(null);
////
////            member.setMemId(memId);
////            member.setMemImage("/member/" + fileName);
////            member.setMemName(memberManagementUpdateDTO.getMemName());
////            member.setMemAcc(memberManagementUpdateDTO.getMemAcc());
////            member.setMemUid(memberManagementUpdateDTO.getMemUid());
////            member.setMemBth(memberManagementUpdateDTO.getMemBth());
////            member.setMemSex(memberManagementUpdateDTO.getMemSex());
////            member.setMemEmail(memberManagementUpdateDTO.getMemEmail());
////            member.setMemTel(memberManagementUpdateDTO.getMemTel());
////            member.setMemAdd(memberManagementUpdateDTO.getMemAdd());
////            member.setMemAccountNumber(memberManagementUpdateDTO.getMemAccountNumber());
////
////            member1 = memberManagementRepository.save(member);
////
////            return member1;
////
////        } catch (IOException e) {
////            // 處理錯誤，例如記錄日誌或返回錯誤訊息
////            e.printStackTrace();
////            return member1;
////        }
////
////    }
////
////}
