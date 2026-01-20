package com.petguardian.chat.model;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ChatRoomRepository extends JpaRepository<ChatRoomVO, Integer> {

    /**
     * Find chatroom by member IDs and Type (Unique Key)
     */
    Optional<ChatRoomVO> findByMemId1AndMemId2AndChatroomType(Integer memId1, Integer memId2, Integer chatroomType);

    /**
     * Find all chatrooms where the user is either member 1 or member 2.
     */
    List<ChatRoomVO> findByMemId1OrMemId2(Integer memId1, Integer memId2);

    /**
     * Counts unread chatrooms for a specific user.
     * Logic: Room is unread if user is mem1 and lastMessageAt > mem1LastReadAt (or
     * null),
     * OR user is mem2 and lastMessageAt > mem2LastReadAt (or null).
     */
    @Query("SELECT COUNT(c) FROM ChatRoomVO c WHERE " +
            "(c.memId1 = :userId AND c.lastMessageAt IS NOT NULL AND (c.mem1LastReadAt IS NULL OR c.lastMessageAt > c.mem1LastReadAt)) OR "
            +
            "(c.memId2 = :userId AND c.lastMessageAt IS NOT NULL AND (c.mem2LastReadAt IS NULL OR c.lastMessageAt > c.mem2LastReadAt))")
    int countUnreadRooms(Integer userId);

}
