package com.petguardian.chat.service.chatroom;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import com.petguardian.chat.dto.ChatRoomMetadataDTO;

/**
 * Implementation for verifying chatroom membership and access rights.
 */
@Service
public class ChatVerificationServiceImpl implements ChatVerificationService {

    private final ChatRoomMetadataReader metadataReader;

    public ChatVerificationServiceImpl(@Qualifier("metadataReaderProxy") ChatRoomMetadataReader metadataReader) {
        this.metadataReader = metadataReader;
    }

    @Override
    public ChatRoomMetadataDTO verifyMembership(Integer chatroomId, Integer userId) {
        ChatRoomMetadataDTO meta = metadataReader.getRoomMetadata(chatroomId);
        if (meta == null) {
            throw new IllegalArgumentException("Chatroom not found: " + chatroomId);
        }

        if (!isMemberInternal(meta, userId)) {
            throw new SecurityException("Access denied: User " + userId + " is not a member of chatroom " + chatroomId);
        }

        return meta;
    }

    @Override
    public boolean isMember(Integer chatroomId, Integer userId) {
        ChatRoomMetadataDTO meta = metadataReader.getRoomMetadata(chatroomId);
        if (meta == null) {
            return false;
        }
        return isMemberInternal(meta, userId);
    }

    private boolean isMemberInternal(ChatRoomMetadataDTO meta, Integer userId) {
        return meta.getMemberIds().contains(userId);
    }
}
