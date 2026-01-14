package com.product.productpic;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "product_pic")
@Getter @Setter
public class ProductPic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_pic_id")
    private Integer productPicId;

    @Column(name = "pro_id", nullable = false)
    private Integer proId;

    @Lob // 對應 SQL 的 BLOB
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "pro_pic")
    private byte[] proPic;
}