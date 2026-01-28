package com.petguardian.chat.service.status;

import com.petguardian.chat.service.chatroom.ChatRoomMetadataReader;
import com.petguardian.chat.service.chatroom.ChatRoomMetadataWriter;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation for managing chat read/unread status.
 */
@Service
public class ChatReadStatusServiceImpl implements ChatReadStatusService {

    private final ChatRoomMetadataReader metadataReader;
    private final ChatRoomMetadataWriter metadataWriter;

    public ChatReadStatusServiceImpl(
            @Qualifier("metadataReaderProxy") ChatRoomMetadataReader metadataReader,
            @Qualifier("metadataWriterProxy") ChatRoomMetadataWriter metadataWriter) {
        this.metadataReader = metadataReader;
        this.metadataWriter = metadataWriter;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasUnreadMessages(Integer userId) {
        List<com.petguardian.chat.dto.ChatRoomMetadataDTO> rooms = metadataReader.getUserChatrooms(userId);
        if (rooms == null || rooms.isEmpty())
            return false;

        return rooms.stream().anyMatch(meta -> {
            if (meta.getLastMessageAt() == null)
                return false;

            java.time.LocalDateTime myLastRead = userId.equals(meta.getMemberIds().get(0))
                    ? meta.getMem1LastReadAt()
                    : meta.getMem2LastReadAt();

            return myLastRead == null || meta.getLastMessageAt().isAfter(myLastRead);
        });
    }

    @Override
    @Transactional
    public void markRoomAsRead(Integer chatroomId, Integer userId) {
        metadataWriter.updateLastReadAtCacheOnly(chatroomId, userId, java.time.LocalDateTime.now());
    }
}
