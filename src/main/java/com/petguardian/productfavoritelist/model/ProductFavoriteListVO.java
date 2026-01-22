package com.petguardian.productfavoritelist.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "product_favorite_list")
@Setter @Getter
@IdClass(ProductFavoriteListId.class)
public class ProductFavoriteListVO implements Serializable {

    @Id
    @Column(name = "mem_id", nullable = false)
    private Integer memId;

    @Id
    @Column(name = "pro_id", nullable = false)
    private Integer proId;

    @Column(name = "fav_time", nullable = false, insertable = false, updatable = false)
    private LocalDateTime favTime;
}
