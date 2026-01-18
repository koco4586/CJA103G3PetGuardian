package com.petguardian.chat.model;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRoomRepository extends JpaRepository<ChatRoomVO, Integer> {

    /**
     * Find chatroom by member IDs (direction 1)
     */
    Optional<ChatRoomVO> findByMemId1AndMemId2(Integer memId1, Integer memId2);

    /**
     * Find chatroom by member IDs (direction 2)
     */
    Optional<ChatRoomVO> findByMemId2AndMemId1(Integer memId2, Integer memId1);

    /**
     * Find all chatrooms where the user is either member 1 or member 2.
     */
    List<ChatRoomVO> findByMemId1OrMemId2(Integer memId1, Integer memId2);
}
