package com.petguardian.shop.service;

import com.petguardian.shop.model.Wallet;
import java.util.Optional;

public interface WalletService {

    // 根據會員 ID 查詢錢包
    Optional<Wallet> getWalletByMemId(Integer memId);

    // 增加餘額（撥款）
    void addBalance(Integer memId, Integer amount);

    // 扣除餘額（付款）
    void deductBalance(Integer memId, Integer amount);

    // 建立錢包
    Wallet createWallet(Integer memId);
}
