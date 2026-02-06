package com.petguardian.seller.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "product_pic")
@Getter
@Setter
public class ProductPic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_pic_id")
    private Integer productPicId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pro_id", nullable = false)
    private Product product;

    @Column(name = "pro_pic", length = 255)
    private String proPic;
}