package com.petguardian.pet.controller;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.petguardian.pet.model.PetVO;
import com.petguardian.pet.model.PetDTO;
import com.petguardian.pet.service.PetService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/pet")
public class PetController {

    @Autowired
    private PetService petService;

    @GetMapping("/index")
    public String index() {
        return "/frontend/index";
    }

    @GetMapping("/img/{petId}")
    @ResponseBody
    public byte[] getImg(@PathVariable Integer petId, HttpServletResponse res) {
        byte[] image = petService.getPetImage(petId);
        if (image != null) {
            res.setContentType("image/jpeg");
            return image;
        }
        return null;
    }

    @GetMapping("/dashboard")
    public String showDashboard(Model model, @RequestParam(defaultValue = "1") Integer whichPage, HttpSession session) {
        Integer memId = (Integer) session.getAttribute("memId");
        if (memId == null) {
            return "redirect:/html/frontend/member/login/login.html";
        }
        Map<String, Object> pageData = petService.getPetsPageData(whichPage, memId);
        model.addAllAttributes(pageData);
        model.addAttribute("whichPage", whichPage);
        return "frontend/dashboard-pets";
    }

    @GetMapping("/listone")
    public String getPetDetail(@RequestParam("petId") Integer petId, Model model) {
        PetDTO pet = petService.getOnePetDTO(petId);
        model.addAttribute("pet", pet);
        return "frontend/pet/petlistonepet";
    }

    @GetMapping("/all")
    public String getAll(@RequestParam(defaultValue = "1") Integer whichPage, Model model, HttpSession session) {
        Integer memId = (Integer) session.getAttribute("memId");
        if (memId == null) {
            return "redirect:/html/frontend/member/login/login.html";
        }
        Map<String, Object> pageData = petService.getPetsPageData(whichPage, memId);
        model.addAllAttributes(pageData);
        model.addAttribute("whichPage", whichPage);
        return "frontend/pet/petlistallpet2_getfromsession";
    }

    @GetMapping("/one")
    public String getOne(@RequestParam(value = "petId", required = false) String petIdStr, Model model,
            HttpSession session) {
        java.util.List<String> errorMsgs = new java.util.LinkedList<>();
        model.addAttribute("errorMsgs", errorMsgs);

        if (petIdStr == null || petIdStr.trim().isEmpty()) {
            errorMsgs.add("請輸入寵物編號");
            return "frontend/pet/petselect";
        }

        Integer petId = null;
        try {
            petId = Integer.valueOf(petIdStr);
        } catch (NumberFormatException e) {
            errorMsgs.add("寵物編號格式不正確");
            return "frontend/pet/petselect";
        }

        PetDTO petDTO = petService.getOnePetDTO(petId);
        if (petDTO == null) {
            errorMsgs.add("查無資料");
            return "frontend/pet/petselect";
        }

        Integer currentMemId = (Integer) session.getAttribute("memId");
        if (currentMemId == null) {
            return "redirect:/html/frontend/member/login/login.html";
        }

        if (!petDTO.getMemId().equals(currentMemId) && !petService.hasOrderRelation(currentMemId, petId)) {
            errorMsgs.add("您無權查看此寵物資料");
            return "frontend/pet/petselect";
        }

        List<Integer> allIds = petService.getAllPetIds(currentMemId);
        int currentIndex = allIds.indexOf(petId);
        int total = allIds.size();
        model.addAttribute("pet", petDTO);
        model.addAttribute("prevId", (currentIndex > 0) ? allIds.get(currentIndex - 1) : null);
        model.addAttribute("nextId", (currentIndex < total - 1) ? allIds.get(currentIndex + 1) : null);
        model.addAttribute("currentIndex", currentIndex);
        model.addAttribute("total", total);
        return "frontend/pet/petlistonepet";
    }

    @PostMapping("/insertBase64")
    @ResponseBody
    public String insertBase64(@ModelAttribute PetVO petVO, @RequestParam("petImageBase64") String petImageBase64,
            HttpSession session) {
        try {
            Integer testMemId = 1001;
            petVO.setMemId(testMemId);
            if (petImageBase64 != null && petImageBase64.contains(",")) {
                String base64Data = petImageBase64.split(",")[1];
                petVO.setPetImage(java.util.Base64.getDecoder().decode(base64Data));
            }
            petService.addPetBase64(petVO);
            return "success";
        } catch (Exception e) {
            e.printStackTrace();
            return "error: " + e.getMessage();
        }
    }

    @PostMapping("/insert")
    public String insert(@ModelAttribute PetVO petVO, @RequestParam MultipartFile petImage, HttpSession session)
            throws Exception {
        Integer memId = (Integer) session.getAttribute("memId");
        if (memId == null) {
            return "redirect:/member/login";
        }
        petVO.setMemId(memId);
        petService.addPet(petVO, petImage);
        return "redirect:/pet/all";
    }

    @GetMapping("/add_page")
    public String showAddPage(HttpSession session) {
        return "frontend/pet/pet1";
    }

    @GetMapping("/confirm")
    public String showConfirmPage() {
        return "frontend/pet/Petconfirm";
    }

    @GetMapping("/getOne_For_Update")
    public String showEditPage(@RequestParam("petId") Integer petId, Model model) {
        PetDTO petDTO = petService.getOnePetDTO(petId);
        model.addAttribute("pet", petDTO);
        return "frontend/pet/petupdate_pet_input";
    }

    @PostMapping("/update")
    @ResponseBody
    public String update(@ModelAttribute PetVO petVO, @RequestParam(value = "petId", required = false) Integer petId,
            @RequestParam(required = false) MultipartFile upFiles,
            @RequestParam("petImageBase64") String petImageBase64, @RequestParam(required = false) String deleteImage,
            HttpSession session) throws Exception {
        if (session == null) {
            return "error: session_expired";
        }

        Integer currentMemId = (Integer) session.getAttribute("memId");
        if (currentMemId == null) {
            return "error: 請先登入";
        }

        if (petVO.getPetId() == null && petId != null) {
            petVO.setPetId(petId);
        }

        if (petVO.getPetId() == null) {
            return "error: petId is missing";
        }

        try {
            PetVO oldPet = petService.getOnePet(petVO.getPetId());
            if (oldPet == null)
                return "error: 找不到該寵物資料";

            petVO.setMemId(oldPet.getMemId());
            petVO.setCreatedTime(oldPet.getCreatedTime());

            if (petImageBase64 != null && petImageBase64.contains(",")) {
                byte[] imageBytes = java.util.Base64.getDecoder().decode(petImageBase64.split(",")[1]);
                petVO.setPetImage(imageBytes);
            } else if (upFiles != null && !upFiles.isEmpty()) {
                petVO.setPetImage(upFiles.getBytes());
            } else {
                petVO.setPetImage(oldPet.getPetImage());
            }

            petService.updatePetBase64(petVO);
            return "success";
        } catch (Exception e) {
            e.printStackTrace();
            return "error: " + e.getMessage();
        }
    }

    @PostMapping("/delete")
    public String deletePet(@RequestParam("petId") Integer petId, HttpSession session,
            RedirectAttributes redirectAttributes) {
        try {
            petService.deletePet(petId);
            return "redirect:/pet/dashboard";
        } catch (Exception e) {
            return "error: 刪除失敗";
        }
    }
}