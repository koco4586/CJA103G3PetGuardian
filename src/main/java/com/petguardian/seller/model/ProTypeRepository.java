package com.petguardian.seller.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProTypeRepository extends JpaRepository<ProType, Integer> {
}