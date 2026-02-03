package com.petguardian.chat.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatMemberRepository extends JpaRepository<ChatMemberEntity, Integer> {

    @Query("SELECT m FROM ChatMemberEntity m WHERE m.memId IN :ids")
    List<ChatMemberEntity> findAllByMemIdIn(@Param("ids") List<Integer> ids);
}
