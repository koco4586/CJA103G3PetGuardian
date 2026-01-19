package com.petguardian.productfavoritelist.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ProductFavoriteListId implements Serializable {

    private Integer memId;
    private Integer proId;
}
