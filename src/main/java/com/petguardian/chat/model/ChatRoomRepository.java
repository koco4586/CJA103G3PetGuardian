package com.petguardian.chat.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatRoomRepository extends JpaRepository<ChatRoomEntity, Integer> {

        List<ChatRoomEntity> findByMemId1OrMemId2(Integer memId1, Integer memId2);

        Optional<ChatRoomEntity> findByMemId1AndMemId2AndChatroomType(Integer memId1, Integer memId2,
                        Integer chatroomType);

        @Modifying
        @Query("UPDATE ChatRoomEntity c SET c.mem1LastReadAt = :time WHERE c.chatroomId = :id AND c.memId1 = :userId")
        void updateMem1LastReadAt(@Param("id") Integer id, @Param("userId") Integer userId,
                        @Param("time") LocalDateTime time);

        @Modifying
        @Query("UPDATE ChatRoomEntity c SET c.mem2LastReadAt = :time WHERE c.chatroomId = :id AND c.memId2 = :userId")
        void updateMem2LastReadAt(@Param("id") Integer id, @Param("userId") Integer userId,
                        @Param("time") LocalDateTime time);

        /**
         * Blind Update for Room Metadata.
         * Updates preview, timestamp, and sender's read status in a single SQL.
         */
        @Modifying
        @Query("UPDATE ChatRoomEntity c SET " +
                        "c.lastMessagePreview = :preview, " +
                        "c.lastMessageAt = :time, " +
                        "c.mem1LastReadAt = CASE WHEN c.memId1 = :senderId THEN :time ELSE c.mem1LastReadAt END, " +
                        "c.mem2LastReadAt = CASE WHEN c.memId2 = :senderId THEN :time ELSE c.mem2LastReadAt END " +
                        "WHERE c.chatroomId = :id")
        void updateRoomMetadataAtomic(@Param("id") Integer id, @Param("preview") String preview,
                        @Param("time") LocalDateTime time, @Param("senderId") Integer senderId);
}
