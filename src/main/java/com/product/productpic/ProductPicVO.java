package com.product.productpic;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "product_pic")
@Getter
@Setter
public class ProductPicVO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_pic_id")
    private Integer productPicId;

    @Column(name = "pro_id")
    private Integer proId;

    @Lob
    @Column(name = "pro_pic")
    private byte[] proPic;
}
