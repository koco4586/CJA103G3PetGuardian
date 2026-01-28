package com.petguardian.chat.service.chatroom;

/**
 * Unified Interface for Chat Room and Member Metadata.
 * Decouples business logic from specific storage (MySQL/Redis).
 * Optimized for Write-Behind strategies.
 * Inherits from segregated Reader and Writer interfaces.
 */
public interface ChatRoomMetadataService extends ChatRoomMetadataReader, ChatRoomMetadataWriter {
}
