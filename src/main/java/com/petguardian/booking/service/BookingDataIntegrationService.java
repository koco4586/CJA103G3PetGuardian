package com.petguardian.booking.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.petguardian.member.model.Member;
import com.petguardian.member.repository.login.MemberLoginRepository;
import com.petguardian.pet.model.PetRepository;
import com.petguardian.pet.model.PetVO;
import com.petguardian.petsitter.model.PetSitterServiceRepository;
import com.petguardian.petsitter.model.PetSitterServiceVO;
import com.petguardian.wallet.model.Wallet;
import com.petguardian.wallet.model.WalletRepository;

@Service
public class BookingDataIntegrationService {

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private MemberLoginRepository memberRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private PetSitterServiceRepository serviceRepository;
    
    //寵物驗證
    public PetVO validateAndGetPet(Integer petId, Integer memId) {
        PetVO pet = petRepository.findByPrimaryKey(petId)
                .orElseThrow(() -> new IllegalArgumentException("預約失敗：找不到對應的寵物資料 (ID: " + petId + ")"));
        if (!pet.getMemId().equals(memId)) {
            throw new IllegalArgumentException("預約失敗：這隻寵物不屬於您。");
        }
        return pet;
    }

    
     //退款操作
    @Transactional
    public void processRefund(Integer memId, int amount) {
        Wallet wallet = walletRepository.findByMemId(memId)
                .orElseThrow(() -> new RuntimeException("找不到錢包，無法退款"));
        wallet.setBalance(wallet.getBalance() + amount);
        walletRepository.save(wallet);
    }

    //撥款操作 (保母端)
    @Transactional
    public void processPayout(Integer sitterMemId, int amount) {
        Wallet wallet = walletRepository.findByMemId(sitterMemId)
                .orElseThrow(() -> new RuntimeException("找不到保母錢包，無法撥款"));
        wallet.setBalance(wallet.getBalance() + amount);
        walletRepository.save(wallet);
    }
    
 // 批次查詢服務項目
    public java.util.Map<Integer, String> getServiceNamesMap(java.util.Set<Integer> serviceItemIds) {
        if (serviceItemIds == null || serviceItemIds.isEmpty()) return java.util.Collections.emptyMap();

        return serviceRepository.findByServiceItemIdIn(serviceItemIds).stream()
            .collect(java.util.stream.Collectors.toMap(
                PetSitterServiceVO::getServiceItemId,
                s -> s.getServiceItem() != null ? s.getServiceItem().getServiceType() : "一般服務",
                (existing, replacement) -> existing // 避免重複 Key 
            ));
    }

    //服務定價
    public PetSitterServiceVO getSitterServiceInfo(Integer sitterId, Integer serviceItemId) {
        return serviceRepository.findBySitterIdAndServiceItemId(sitterId, serviceItemId)
        		.orElseThrow(() -> new RuntimeException("該保母不提供此項服務或保母不存在"));
    }

    //會員資料
    public Member getMemberInfo(Integer memId) {
        return memberRepository.findById(memId).orElse(null);
    }
    //查詢寵物
    public PetVO getPetInfo(Integer petId) {
        return petRepository.findByPrimaryKey(petId).orElse(null);
    }
}