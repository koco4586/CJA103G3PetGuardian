package com.petguardian.wallet.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "wallet")
@Getter
@Setter
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wallet_id")
    private Integer walletId;

    @Column(name = "mem_id", nullable = false, unique = true)
    private Integer memId;

    @Column(name = "balance", nullable = false)
    private Integer balance = 0;
}