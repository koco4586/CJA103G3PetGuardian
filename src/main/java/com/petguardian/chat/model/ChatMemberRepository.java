package com.petguardian.chat.model;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMemberRepository extends JpaRepository<ChatMemberVO, Integer> {
}
