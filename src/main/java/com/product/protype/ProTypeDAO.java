package com.product.protype;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProTypeDAO extends JpaRepository<ProTypeVO, Integer> {}

