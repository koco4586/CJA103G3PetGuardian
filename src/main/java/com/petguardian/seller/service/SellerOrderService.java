package com.petguardian.seller.service;

import com.petguardian.orders.model.*;
import com.petguardian.wallet.model.Wallet;
import com.petguardian.wallet.model.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface SellerOrderService {

    List<OrdersVO> getSellerOrders(Integer sellerMemId);
    Optional<OrdersVO> getOrderById(Integer orderId);
    OrdersVO updateOrderStatus(Integer orderId, Integer newStatus);
    List<OrderItemVO> getOrderItems(Integer orderId);

    Map<String, Object> getOrderDetail(Integer sellerId, Integer orderId);

    @Service
    @Transactional
    class WalletServiceImpl implements ProductService.WalletService {

        @Autowired
        private WalletRepository walletRepository;

        @Override
        public Optional<Wallet> getWalletByMemId(Integer memId) {
            return walletRepository.findByMemId(memId);
        }

        @Override
        public void addBalance(Integer memId, Integer amount) {
            Wallet wallet = walletRepository.findByMemId(memId)
                    .orElseThrow(() -> new RuntimeException("錢包不存在"));

            wallet.setBalance(wallet.getBalance() + amount);
            walletRepository.save(wallet);
        }

        @Override
        public void deductBalance(Integer memId, Integer amount) {
            Wallet wallet = walletRepository.findByMemId(memId)
                    .orElseThrow(() -> new RuntimeException("錢包不存在"));

            if (wallet.getBalance() < amount) {
                throw new RuntimeException("餘額不足");
            }

            wallet.setBalance(wallet.getBalance() - amount);
            walletRepository.save(wallet);
        }

        @Override
        public Wallet createWallet(Integer memId) {
            // 檢查是否已存在
            Optional<Wallet> existing = walletRepository.findByMemId(memId);
            if (existing.isPresent()) {
                return existing.get();
            }

            Wallet wallet = new Wallet();
            wallet.setMemId(memId);
            wallet.setBalance(0);
            return walletRepository.save(wallet);
        }
    }
}
