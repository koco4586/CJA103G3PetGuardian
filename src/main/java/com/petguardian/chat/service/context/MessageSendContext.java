package com.petguardian.chat.service.context;

import com.petguardian.chat.dto.MemberProfileDTO;
import com.petguardian.chat.model.ChatMessageEntity;
import com.petguardian.chat.model.ChatRoomEntity;
import lombok.Builder;
import lombok.Getter;

/**
 * Pre-loaded Context for Message Sending.
 * Eagerly loads all required data to prevent N+1 queries during processing.
 */
@Getter
@Builder
public class MessageSendContext {
    private final ChatRoomEntity chatroom;
    private final MemberProfileDTO senderProfile;
    private final ChatMessageEntity replyMessage;
    private final MemberProfileDTO replySenderProfile;
    private final String messageId;

    public MessageCreationContext toCreationContext(String content, String replyToId) {
        return new MessageCreationContext(
                this.messageId,
                this.chatroom.getChatroomId(),
                this.senderProfile.getMemberId(),
                content,
                replyToId);
    }
}
