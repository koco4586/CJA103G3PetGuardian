package com.product.protype;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "pro_type")
@Getter @Setter
public class ProType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pro_type_id")
    private Integer proTypeId;

    @Column(name = "pro_type_name", nullable = false, length = 20)
    private String proTypeName;
}