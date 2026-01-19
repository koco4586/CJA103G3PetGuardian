package com.petguardian.shop.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Integer> {

    // 根據會員 ID 查詢錢包
    Optional<Wallet> findByMemId(Integer memId);
}
