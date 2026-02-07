package com.petguardian.pet.service;

import java.time.format.DateTimeFormatter;

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

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

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
        if (vo == null)
            return null;
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

        String desc = vo.getPetDescription();
        dto.setPetDescription((desc == null || desc.trim().isEmpty()) ? "" : desc.trim());

        // 📅 時間格式化 (新時代用法)
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        if (vo.getCreatedTime() != null) {
            dto.setCreatedTimeText(vo.getCreatedTime().format(dtf));
        }
        if (vo.getUpdatedAt() != null) {
            dto.setUpdatedAtText(vo.getUpdatedAt().format(dtf));
        }

        // 🔥 設定是否有圖片
        dto.setHasImage(vo.getPetImage() != null && vo.getPetImage().length > 0);

        return dto;
    }

    private String resolveTypeName(Integer typeId) {
        if (typeId == null)
            return "未知";

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
        if (gender == null)
            return "未知";
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
         * boolean hasOrder = orderRepo.checkIfSitterHasPetOrder(currentMemId, petId);
         * if (hasOrder) return true;
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

        if (whichPage == null || whichPage < 1) {
            whichPage = 1;
        }

        int rowsPerPage = 9; // 你設定一頁 3 筆
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
                "whichPage", whichPage);
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
    public void updatePet(PetVO petVO, MultipartFile petImage, String deleteImage) throws Exception {
        // 情況 1：使用者勾選了「刪除圖片」
        if ("true".equals(deleteImage)) {
            petVO.setPetImage(null);
        }
        // 情況 2：使用者上傳了新圖片
        else if (petImage != null && !petImage.isEmpty()) {
            petVO.setPetImage(petImage.getBytes());
        }
        // 情況 3：使用者沒傳新圖，也沒刪除圖 -> 從資料庫撈出舊圖補回，防止變空白
        else {
            repository.findByPrimaryKey(petVO.getPetId()).ifPresent(oldPet -> {
                petVO.setPetImage(oldPet.getPetImage());
            });
        }
        repository.update(petVO);
    }

    @Override
    public void addPetBase64(PetVO petVO) {
        // 直接呼叫你寫好的 jdbcTemplate.update(INSERT, ...)
        repository.insert(petVO);
    }

    @Override
    public void updatePetBase64(PetVO petVO) {
        // 直接呼叫你寫好的 jdbcTemplate.update(UPDATE, ...)
        repository.update(petVO);
    }

    public PetVO getOnePet(Integer petId) {
        // 呼叫 Repository 的 findByPrimaryKey，並處理 Optional
        return repository.findByPrimaryKey(petId).orElse(null);
    }

}
