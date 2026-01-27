package com.petguardian.chat.service.chatroom;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.petguardian.chat.dto.ChatRoomMetadataDTO;
import com.petguardian.chat.dto.MemberProfileDTO;
import com.petguardian.chat.model.ChatRoomRepository;
import com.petguardian.chat.service.chatmessage.resilience.ResilienceChatFailureHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * JPA implementation for Metadata Writing.
 */
@Slf4j
@Service("jpaMetadataWriter")
@RequiredArgsConstructor
public class JpaMetadataWriter implements ChatRoomMetadataWriter {

    private final ChatRoomRepository chatRoomRepository;
    private final ResilienceChatFailureHandler failureHandler;

    @Override
    @Transactional
    public void syncRoomMetadata(Integer chatroomId, String preview, LocalDateTime time, Integer senderId) {
        chatRoomRepository.updateRoomMetadataAtomic(chatroomId, preview, time, senderId);
    }

    @Override
    public void updateCacheOnly(Integer chatroomId, String preview, LocalDateTime time, Integer senderId) {
        // No-Op for JPA
    }

    @Override
    public void addUserToRoom(Integer userId, Integer chatroomId) {
        // Handled via other association logic in JPA
    }

    @Override
    @Transactional
    public void updateLastReadAt(Integer chatroomId, Integer userId, LocalDateTime time) {
        chatRoomRepository.updateMem1LastReadAt(chatroomId, userId, time);
        chatRoomRepository.updateMem2LastReadAt(chatroomId, userId, time);
    }

    @Override
    public void updateLastReadAtCacheOnly(Integer chatroomId, Integer userId, LocalDateTime time) {
        // No-Op for JPA
    }

    @Override
    public void saveRoomMetadata(ChatRoomMetadataDTO meta) {
        // Read-Through save not directly needed for JPA as it's the source
    }

    @Override
    public void saveMemberProfile(MemberProfileDTO profile) {
        // Source of Truth
    }

    @Override
    public void saveUserRoomList(Integer userId, List<Integer> roomIds) {
        // Source of Truth
    }

    @Override
    public boolean isHealthy() {
        return true;
    }

    @Override
    public boolean isConnectionException(Throwable e) {
        return failureHandler.isConnectionException(e);
    }

    @Override
    public boolean isDataIntegrityViolation(Throwable e) {
        return failureHandler.isDataIntegrityViolation(e);
    }
}
