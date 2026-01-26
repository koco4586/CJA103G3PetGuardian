package com.petguardian.pet.service;

import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.web.multipart.MultipartFile;
import com.petguardian.pet.model.PetDTO;
import com.petguardian.pet.model.PetRepository;
import com.petguardian.pet.model.PetVO;

@Service
public class PetServiceImpl implements PetService { // 🔴 加上 implements

    @Autowired
    private PetRepository repository;

    @Override
    public List<PetDTO> getPetsByMemId(Integer memId) {
        // 呼叫 Repo 剛剛補好的方法
        List<PetVO> pets = repository.findByMemId(memId);
        
        // 使用你現有的 convertToDTO 工具進行轉換
        return pets.stream()
                   .map(this::convertToDTO)
                   .collect(Collectors.toList());
    } // <---
    // --- 轉換工具 (內部使用，不一定要在 Interface 定義) ---
    private PetDTO convertToDTO(PetVO vo) {
        if (vo == null) return null;
        PetDTO dto = new PetDTO();
        dto.setPetId(vo.getPetId());
        dto.setMemId(vo.getMemId());
        dto.setPetName(vo.getPetName());
        dto.setPetAge(vo.getPetAge());
        dto.setPetDescription(vo.getPetDescription());
        dto.setTypeId(vo.getTypeId());
        dto.setTypeName(resolveTypeName(vo.getTypeId()));
        dto.setSizeName(vo.getSizeId() != null && vo.getSizeId() == 1 ? "小型" : (vo.getSizeId() == 2 ? "中型" : "大型"));
        dto.setSizeId(vo.getSizeId());
        dto.setPetGender(vo.getPetGender());
        dto.setPetGenderText(resolveGenderText(vo.getPetGender()));
        
        if (vo.getPetImage() != null) {
            String base64 = java.util.Base64.getEncoder().encodeToString(vo.getPetImage());
            dto.setBase64Image("data:image/jpeg;base64," + base64);
        }
        
        return dto;
    }
    
    private String resolveTypeName(Integer typeId) {
        if (typeId == null) return "未知";

        switch (typeId) {
            case 1:
                return "貓";
            case 2:
                return "狗";
            default:
                return "其他";
        }
    }
    
    private String resolveGenderText(Integer gender) {
        if (gender == null) return "未知";
        return gender == 1 ? "公" : "母";
    }
    
    @Override
    public List<Integer> getAllPetIds(Integer currentMemId) {
        // 這裡建議修改成：只取得 (1) 我自己的寵物 (2) 我有訂單關係的寵物 ID
        // 這樣在使用「上一筆/下一筆」導覽時，才不會跳到別人的寵物去
        return repository.getAll().stream()
                         .filter(p -> p.getMemId().equals(currentMemId) || hasOrderRelation(currentMemId, p.getPetId()))
                         .map(PetVO::getPetId)
                         .collect(Collectors.toList());
    }
    public boolean hasOrderRelation(Integer currentMemId, Integer petId) {
        // 1. 呼叫你 Repository 裡的方法 (findByPrimaryKey)
        Optional<PetVO> petOpt = repository.findByPrimaryKey(petId);
        
        if (petOpt.isEmpty()) {
            return false; // 找不到這隻寵物
        }
        
        PetVO pet = petOpt.get();

        // 2. 判斷：如果是主人，直接回傳 true (主人能看自己的)
        if (pet.getMemId().equals(currentMemId)) {
            return true; 
        }

        // 3. 判斷：如果是保姆，檢查是否有訂單關係
        // 🔴 這裡需要去「訂單表」查。邏輯是：是否有一個訂單，保姆是 currentMemId 且寵物是 petId
        // 因為我現在沒有你的 OrderRepository，我先幫你寫下判斷邏輯的註解
        /*
        boolean hasOrder = orderRepo.checkIfSitterHasPetOrder(currentMemId, petId);
        if (hasOrder) return true;
        */

        return false; // 都不是就沒權限
    }

    @Override
    public byte[] getPetImage(Integer petId) {
        return repository.findByPrimaryKey(petId)
                         .map(PetVO::getPetImage)
                         .orElse(null);
    }
    
    @Override
    public byte[] getPetOriginalImage(Integer petId) {
        return repository.findByPrimaryKey(petId)
                         .map(PetVO::getPetImageOriginal) // 抓原圖欄位
                         .orElse(null);
    }//商城所需，記得打開

    @Override
    public List<PetDTO> findPetsByNameDTO(String petName) {
        List<PetVO> voList = repository.getByName(petName);
        return voList.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public PetDTO getOnePetDTO(Integer petId) {
        return repository.findByPrimaryKey(petId)
                         .map(this::convertToDTO)
                         .orElse(null);
    }

    @Override
    public Map<String, Object> getPetsPageData(Integer whichPage, 
    		Integer memId) {
        List<PetVO> voList = repository.findByMemId(memId);
        List<PetDTO> dtoList = voList.stream().map(this::convertToDTO).collect(Collectors.toList());

        int rowsPerPage = 3; // 你設定一頁 3 筆
        int rowNumber = dtoList.size();
        int pageNumber = (int) Math.ceil((double) rowNumber / rowsPerPage);
        
        // 計算切片的開始與結束 index
        int fromIndex = (whichPage - 1) * rowsPerPage;
        int toIndex = Math.min(fromIndex + rowsPerPage, rowNumber);

        
        // 關鍵修正：只取出那一頁要顯示的資料
        List<PetDTO> pagedlist = (fromIndex < rowNumber) 
                                 ? dtoList.subList(fromIndex, toIndex) 
                                 : List.of();

        // 注意：這裡傳出的 Key 是 "petlist"
        return Map.of(
            "petlist", pagedlist, 
            "rowNumber", rowNumber, 
            "pageNumber", pageNumber,
            "rowsPerPage", rowsPerPage,
            "whichPage", whichPage
        );
    }

    @Override
    public void deletePet(Integer petId) {
        repository.delete(petId);
    }

    @Override
    public void addPet(PetVO petVO, MultipartFile petImage) throws Exception {
        if (petImage != null && !petImage.isEmpty()) {
            petVO.setPetImage(petImage.getBytes());
        }
        repository.insert(petVO);
    }

    @Override
    public void addPetFromBase64(String base64Str, String originalBase64, String name, String type, String sex, String age, String size, String desc, Integer memId) {
    	
    	String base64 = base64Str.split(",")[1];
        byte[] image = Base64.getDecoder().decode(base64);
        PetVO pet = new PetVO();
        pet.setMemId(memId);
        pet.setPetName(name);
        pet.setPetImage(image);
        pet.setTypeId("狗".equals(type) ? 2 : 1);
        pet.setPetGender("母".equals(sex) ? 2 : 1);
        pet.setPetAge(age == null || age.isEmpty() ? 0 : Integer.parseInt(age));
        pet.setSizeId(Integer.parseInt(size));
        pet.setPetDescription(desc);
        
        if (base64Str != null && base64Str.contains(",")) {
            try {
                String pureBase64 = base64Str.split(",")[1];
                byte[] imageBytes = Base64.getDecoder().decode(pureBase64);
                pet.setPetImage(imageBytes);
            } catch (Exception e) {
                System.err.println("合成圖解碼失敗: " + e.getMessage());
            }
        }

        // 2. 處理「原始圖」 (判斷並解碼)
        if (originalBase64 != null && originalBase64.contains(",")) {
            try {
                String pureOriginalBase64 = originalBase64.split(",")[1];
                byte[] originalBytes = Base64.getDecoder().decode(pureOriginalBase64);
                pet.setPetImageOriginal(originalBytes);
                System.out.println("Service: 原圖已成功存入 pet 物件");
            } catch (Exception e) {
                System.err.println("原圖解碼失敗: " + e.getMessage());
            }
        } else {
            System.out.println("Service: 未收到原圖 Base64 資料");
        }
        
        // 3. 執行資料庫儲存
        repository.insert(pet);
    }

    @Override
    public void updatePetWithCanvas(PetVO petVO, String base64Data, String originalBase64, String deleteImage) throws Exception {
        if ("true".equals(deleteImage)) {
            petVO.setPetImage(null);
            petVO.setPetImageOriginal(null);
        } else if (base64Data != null && base64Data.contains(",")) {
        	petVO.setPetImage(Base64.getDecoder().decode(base64Data.split(",")[1]));
        	if (originalBase64 != null && originalBase64.contains(",")) {
                petVO.setPetImageOriginal(Base64.getDecoder().decode(originalBase64.split(",")[1]));
            }
        } else {
            // 沒換圖，從資料庫撈出舊的兩張圖塞回去，避免變成空白
            repository.findByPrimaryKey(petVO.getPetId()).ifPresent(oldPet -> {
                petVO.setPetImage(oldPet.getPetImage());
                petVO.setPetImageOriginal(oldPet.getPetImageOriginal());
            });
            
        }
        repository.update(petVO);
    }
}
