package com.petguardian.chat.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.HashSet;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.petguardian.chat.dto.ChatRoomMetadataDTO;
import com.petguardian.chat.dto.MemberProfileDTO;
import com.petguardian.chat.dto.ChatMessageDTO;
import com.petguardian.chat.model.ChatMessageEntity;
import com.petguardian.chat.model.ChatRoomEntity;
import com.petguardian.chat.service.chatroom.ChatRoomService;
import com.petguardian.chat.service.mapper.ChatMessageMapper;
import com.petguardian.chat.service.status.ChatStatusService;
import com.petguardian.chat.service.chatmessage.report.ChatReportService;
import com.petguardian.chat.service.context.MessageSendContext;
import com.petguardian.chat.service.context.MessageCreationContext;

import com.petguardian.chat.service.chatmessage.ChatMessageService;
import com.petguardian.chat.service.chatmessage.ChatMessageRetrievalManager;

import io.hypersistence.tsid.TSID;
import lombok.RequiredArgsConstructor;

/**
 * Service Implementation for Core Chat Functionality.
 * Responsibilities:
 * Orchestrates the message processing flow (Validation → Persistence →
 * Notification)
 * Manages chat history retrieval with pagination
 * Architecture Note:
 * Chatroom operations delegated to {@link ChatRoomService} (Facade)
 * Message operations delegated to {@link ChatMessageService} (Facade)
 * Status/Notification delegated to {@link ChatStatusService} (Facade)
 * DTO mapping delegated to {@link ChatMessageMapper}
 */
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    // =========================================================================
    // DEPENDENCIES (Facades)
    // =========================================================================

    private final ChatRoomService chatRoomService; // Chatroom Facade
    private final ChatMessageService chatMessageService; // Message Facade
    private final ChatMessageRetrievalManager retrievalManager; // Retrieval Manager (for Search)
    private final ChatStatusService statusService; // Status + Notification Facade
    private final ChatMessageMapper messageMapper; // Mapper
    private final TSID.Factory tsidFactory; // ID Generator
    private final ChatReportService reportService; // Report Service

    // ============================================================
    // PUBLIC API
    // ============================================================

    /**
     * Processes an incoming message from a user.
     * Uses pre-loading pattern to eliminate N+1 queries.
     */
    @Override
    @Transactional
    public ChatMessageDTO handleIncomingMessage(ChatMessageDTO dto) {
        Integer senderId = dto.getSenderId();
        Integer receiverId = dto.getReceiverId();

        // 1. Fail Fast Validation (Strategy: Prevent Poison Messages)
        if (senderId == null) {
            throw new IllegalArgumentException("Sender ID cannot be null");
        }
        if (dto.getContent() == null || dto.getContent().length() > 2000) {
            throw new IllegalArgumentException("Message content must be between 1 and 2000 characters");
        }
        if (dto.getChatroomId() == null && receiverId == null) {
            throw new IllegalArgumentException("Target (ChatroomId or ReceiverId) must be provided");
        }

        // 2. Pre-load all required context in one pass (Zero SQL pattern)
        MessageSendContext ctx = preloadContext(dto);

        // 3. Build slim persistence context
        MessageCreationContext creationCtx = ctx.toCreationContext(dto.getContent(), dto.getReplyToId());

        // 4. Delegate persistence to Facade (Encapsulated High Availability Logic)
        ChatMessageEntity saved = chatMessageService.save(creationCtx);

        // 5. Build response using pre-loaded context (no additional SQL)
        return buildResponseDtoFromContext(saved, ctx, receiverId);
    }

    /**
     * Retrieves paginated chat history for a specific room.
     * Validates access rights and enriches messages with sender/reply details.
     */
    @Override
    @Transactional(readOnly = true)
    public List<ChatMessageDTO> getChatHistory(Integer chatroomId, Integer currentUserId, int page, int size) {
        // Access Control via ChatRoomService facade
        ChatRoomMetadataDTO chatroom = chatRoomService.verifyMembership(chatroomId, currentUserId);

        // Fetch paginated messages via Facade
        Pageable pageable = PageRequest.of(page, size);
        List<ChatMessageEntity> messages = fetchMessagesAsc(chatroomId, pageable);

        if (messages.isEmpty()) {
            return Collections.emptyList();
        }

        // Prepare Batch Data
        Map<String, ChatMessageEntity> replyMap = resolveReplyMap(messages);
        Map<Integer, MemberProfileDTO> memberMap = resolveMemberMap(messages, replyMap);
        Map<String, Integer> reportStatusMap = resolveReportStatusMap(currentUserId, messages);

        // Delegate DTO conversion to mapper
        Integer partnerId = chatroom.getMemberIds().stream()
                .filter(id -> !id.equals(currentUserId))
                .findFirst()
                .orElse(null);
        List<ChatMessageDTO> dtos = messageMapper.toDtoList(messages, currentUserId, partnerId, memberMap, replyMap,
                reportStatusMap);

        // Mark last sent message as read if partner has read (only for first page)
        markLatestSelfMessageAsRead(dtos, chatroom, currentUserId, page);

        return dtos;
    }

    /**
     * Checks if the user has any unread messages.
     * Delegates to ChatStatusService facade.
     */
    @Override
    @Transactional(readOnly = true)
    public boolean hasUnreadMessages(Integer userId) {
        return statusService.hasUnreadMessages(userId);
    }

    /**
     * Marks the chatroom as read for the specific user.
     * Delegates to ChatStatusService which handles both persistence and WebSocket
     * broadcast.
     */
    @Override
    @Transactional
    public void markRoomAsRead(Integer chatroomId, Integer userId) {
        statusService.markRoomAsRead(chatroomId, userId);
    }

    // ============================================================
    // PRIVATE HELPERS
    // ============================================================

    /**
     * Pre-loads all required context for message sending.
     * Batch-fetches sender, reply, and reply sender profiles in one call.
     */
    private MessageSendContext preloadContext(ChatMessageDTO dto) {
        Integer senderId = dto.getSenderId();
        Integer receiverId = dto.getReceiverId();

        // 1. Resolve Chatroom
        ChatRoomEntity chatroom = chatRoomService.resolveOrCreateChatroom(
                dto.getChatroomId(), senderId, receiverId);

        // 2. Generate TSID (distributed-safe ID)
        String messageId = tsidFactory.generate().toString();

        // 3. Resolve Reply Message (if exists)
        ChatMessageEntity replyMessage = null;
        if (dto.getReplyToId() != null) {
            replyMessage = chatMessageService.findById(dto.getReplyToId()).orElse(null);
        }

        // 4. Batch Fetch Member Profiles (sender + reply sender)
        List<Integer> memberIds = new ArrayList<>();
        memberIds.add(senderId);
        if (replyMessage != null) {
            memberIds.add(replyMessage.getMemberId());
        }
        Map<Integer, MemberProfileDTO> profiles = chatRoomService.getMemberProfiles(memberIds);

        // 5. Build Context
        return MessageSendContext.builder()
                .chatroom(chatroom)
                .messageId(messageId)
                .senderProfile(profiles.get(senderId))
                .replyMessage(replyMessage)
                .replySenderProfile(replyMessage != null ? profiles.get(replyMessage.getMemberId()) : null)
                .build();
    }

    /**
     * Builds response DTO using pre-loaded context (0 SQL).
     */
    private ChatMessageDTO buildResponseDtoFromContext(ChatMessageEntity saved, MessageSendContext ctx,
            Integer receiverId) {
        MemberProfileDTO sender = ctx.getSenderProfile();
        String replyContent = ctx.getReplyMessage() != null ? ctx.getReplyMessage().getMessage() : null;
        String replySenderName = ctx.getReplySenderProfile() != null ? ctx.getReplySenderProfile().getMemberName()
                : null;

        return messageMapper.toDto(saved, sender, replyContent, replySenderName, saved.getMemberId(), receiverId, 0);
    }

    private List<ChatMessageEntity> fetchMessagesAsc(Integer chatroomId, Pageable pageable) {
        List<ChatMessageEntity> desc = chatMessageService.fetchHistory(chatroomId, pageable);
        List<ChatMessageEntity> asc = new ArrayList<>(desc);
        Collections.reverse(asc);
        return asc;
    }

    private void markLatestSelfMessageAsRead(List<ChatMessageDTO> dtos, ChatRoomMetadataDTO chatroom,
            Integer currentUserId,
            int page) {
        if (page != 0) {
            return;
        }

        // Correct Partner Identification: If I am member 0, partner is member 1.
        LocalDateTime partnerLastReadAt = currentUserId.equals(chatroom.getMemberIds().get(0))
                ? chatroom.getMem2LastReadAt() // Partner is Member 2
                : chatroom.getMem1LastReadAt(); // Partner is Member 1

        if (partnerLastReadAt == null) {
            return;
        }

        // Mark all messages as read if sent by current user and chatTime <=
        // partnerLastReadAt
        for (ChatMessageDTO msg : dtos) {
            if (currentUserId.equals(msg.getSenderId())) {
                if (msg.getChatTime() != null && !msg.getChatTime().isAfter(partnerLastReadAt)) {
                    msg.setIsRead(true);
                }
            }
        }
    }

    // ============================================================
    // DATA AGGREGATION HELPERS
    // ============================================================

    private Map<Integer, MemberProfileDTO> resolveMemberMap(List<ChatMessageEntity> messages,
            Map<String, ChatMessageEntity> replyMap) {
        Set<Integer> memberIds = new HashSet<>();

        // 1. Collect IDs from current batch
        messages.forEach(msg -> memberIds.add(msg.getMemberId()));

        // 2. Collect IDs from referenced replies
        replyMap.values().forEach(replyMsg -> memberIds.add(replyMsg.getMemberId()));

        if (memberIds.isEmpty()) {
            return Collections.emptyMap();
        }

        // Use ChatRoomService facade for batch member profile retrieval
        return chatRoomService.getMemberProfiles(new ArrayList<>(memberIds));
    }

    private Map<String, ChatMessageEntity> resolveReplyMap(List<ChatMessageEntity> messages) {
        Set<String> replyIds = messages.stream()
                .map(ChatMessageEntity::getReplyToMessageId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        if (replyIds.isEmpty()) {
            return Collections.emptyMap();
        }

        // Try to find reply messages in the current batch first
        Map<String, ChatMessageEntity> foundInBatch = messages.stream()
                .filter(msg -> replyIds.contains(msg.getMessageId()))
                .collect(Collectors.toMap(ChatMessageEntity::getMessageId, Function.identity()));

        Set<String> missingIds = replyIds.stream()
                .filter(id -> !foundInBatch.containsKey(id))
                .collect(Collectors.toSet());

        if (missingIds.isEmpty()) {
            return foundInBatch;
        }

        // Only query the facade (which might hit MySQL) for missing IDs
        Map<String, ChatMessageEntity> results = new HashMap<>(foundInBatch);
        results.putAll(chatMessageService.findAllById(missingIds).stream()
                .collect(Collectors.toMap(ChatMessageEntity::getMessageId, Function.identity())));

        return results;
    }

    private Map<String, Integer> resolveReportStatusMap(Integer currentUserId, List<ChatMessageEntity> messages) {
        // Resolve report status for the current user to display in the UI
        if (messages.isEmpty()) {
            return Collections.emptyMap();
        }

        List<String> messageIds = messages.stream()
                .map(ChatMessageEntity::getMessageId)
                .collect(Collectors.toList());

        // Use Strategy for efficient Batch Retrieval (Cache-Aside Zero SQL)
        return reportService.getBatchStatus(currentUserId, messageIds);
    }
    // ============================================================
    // SEARCH & JUMP HELPERS
    // ============================================================

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessageDTO> searchChatHistory(Integer chatroomId, String keyword, Integer requesterId) {
        // 1. Validate membership AND get metadata
        ChatRoomMetadataDTO chatroom = chatRoomService.verifyMembership(chatroomId, requesterId);

        // 2. Search messages (RetrievalManager -> Repository)
        List<ChatMessageEntity> entities = retrievalManager.searchMessage(chatroomId, keyword);

        if (entities.isEmpty()) {
            return Collections.emptyList();
        }

        // 4. Convert to DTOs
        // Using Mapper for consistency.
        // We need 'partnerId' for the mapper.
        // Reusing 'chatroom' metadata from step 1

        Integer partnerId = chatroom.getMemberIds().stream()
                .filter(id -> !id.equals(requesterId))
                .findFirst()
                .orElse(null);

        // We also need replyMap for proper DTO structure if we want consistency
        // But for search results, reply context might be optional or we can fetch it.
        // Let's resolve context to be safe.
        Map<String, ChatMessageEntity> replyMap = resolveReplyMap(entities); // Reuse private helper
        Map<Integer, MemberProfileDTO> memberMap = resolveMemberMap(entities, replyMap); // Reuse private helper
        Map<String, Integer> reportStatusMap = Collections.emptyMap();

        return messageMapper.toDtoList(entities, requesterId, partnerId, memberMap, replyMap, reportStatusMap);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Integer> getMessagePosition(Integer chatroomId, String messageId, Integer pageSize) {
        // 1. Calculate page index
        int page = retrievalManager.getMessagePage(chatroomId, messageId, pageSize);

        // Return as map
        Map<String, Integer> result = new HashMap<>();
        result.put("page", page);
        return result;
    }
}
