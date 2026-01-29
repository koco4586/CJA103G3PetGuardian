package com.petguardian.chat.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.petguardian.chat.dto.ChatRoomMetadataDTO;
import com.petguardian.chat.dto.MemberProfileDTO;
import com.petguardian.chat.dto.ChatMessageDTO;
import com.petguardian.chat.dto.ChatRoomDTO;
import com.petguardian.chat.model.ChatMessageEntity;
import com.petguardian.chat.model.ChatRoomEntity;
import com.petguardian.chat.service.chatmessage.MessageReaderStrategyService;
import com.petguardian.chat.service.chatmessage.MessageWriterStrategyService;
import org.springframework.beans.factory.annotation.Qualifier;
import com.petguardian.chat.service.chatroom.ChatRoomCreationStrategy;
import com.petguardian.chat.service.chatroom.ChatRoomMetadataReader;
import com.petguardian.chat.service.chatroom.ChatVerificationService;
import com.petguardian.chat.service.mapper.ChatMessageMapper;
import com.petguardian.chat.service.mapper.ChatRoomMapper;
import com.petguardian.chat.service.status.ChatReadStatusService;
import com.petguardian.chat.service.chatmessage.report.ReportStrategyService;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.petguardian.chat.service.chatmessage.MessageCreationContext;

import io.hypersistence.tsid.TSID;

/**
 * Service Implementation for Core Chat Functionality.
 * 
 * Responsibilities:
 * - Orchestrates the message processing flow (Validation -> Persistence ->
 * Notification)
 * - Manages chat history retrieval with pagination
 * - Handles real-time WebSocket notifications
 * 
 * Architecture Note:
 * - DTO mapping is delegated to {@link ChatMessageMapper}
 * - Read status management is delegated to {@link ChatReadStatusService}
 * - Membership verification is delegated to {@link ChatVerificationService}
 * - Persistence is delegated to {@link MessageStrategyService}
 */
@Service
public class ChatServiceImpl implements ChatService {
    // ============================================================
    // DEPENDENCIES
    // ============================================================
    private final ChatRoomMetadataReader metadataReader;
    private final MessageReaderStrategyService messageReader;
    private final MessageWriterStrategyService messageWriter;
    private final ChatRoomCreationStrategy chatRoomCreationStrategy;
    private final ChatMessageMapper messageMapper;
    private final ChatRoomMapper chatRoomMapper;
    private final ChatReadStatusService readStatusService;
    private final ChatVerificationService verificationService;
    private final SimpMessagingTemplate messagingTemplate;
    private final TSID.Factory tsidFactory;
    private final ReportStrategyService reportStrategy;

    public ChatServiceImpl(
            @Qualifier("metadataReaderProxy") ChatRoomMetadataReader metadataReader,
            @Qualifier("messageReaderProxy") MessageReaderStrategyService messageReader,
            @Qualifier("messageWriterProxy") MessageWriterStrategyService messageWriter,
            ChatRoomCreationStrategy chatRoomCreationStrategy,
            ChatMessageMapper messageMapper,
            ChatRoomMapper chatRoomMapper,
            ChatReadStatusService readStatusService,
            ChatVerificationService verificationService,
            SimpMessagingTemplate messagingTemplate,
            TSID.Factory tsidFactory,
            ReportStrategyService reportStrategy) {
        this.metadataReader = metadataReader;
        this.messageReader = messageReader;
        this.messageWriter = messageWriter;
        this.chatRoomCreationStrategy = chatRoomCreationStrategy;
        this.messageMapper = messageMapper;
        this.chatRoomMapper = chatRoomMapper;
        this.readStatusService = readStatusService;
        this.verificationService = verificationService;
        this.messagingTemplate = messagingTemplate;
        this.tsidFactory = tsidFactory;
        this.reportStrategy = reportStrategy;
    }

    // ============================================================
    // PUBLIC API
    // ============================================================

    /**
     * Processes an incoming message from a user.
     * Ensures chatroom existence, persists the message via strategy,
     * and constructs the response DTO with full context.
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

        // Ensure valid chatroom exists before saving message
        ChatRoomEntity chatroom = resolveChatroom(dto.getChatroomId(), senderId, receiverId);

        // Generate distributed-safe ID (TSID)
        TSID tsid = tsidFactory.generate();
        String messageId = tsid.toString();

        // Build MessageCreationContext
        MessageCreationContext context = new MessageCreationContext(
                messageId,
                chatroom.getChatroomId(),
                senderId,
                dto.getContent(),
                dto.getReplyToId());

        // Delegate persistence to strategy (supports Sync MySQL or Async Redis)
        // Note: Metadata sync is now handled entirely by the strategy implementation.
        ChatMessageEntity saved = messageWriter.save(context);

        // Build response DTO
        return buildResponseDto(saved, dto.getSenderName(), receiverId);
    }

    /**
     * Retrieves paginated chat history for a specific room.
     * Validates access rights and enriches messages with sender/reply details.
     */
    @Override
    @Transactional(readOnly = true)
    public List<ChatMessageDTO> getChatHistory(Integer chatroomId, Integer currentUserId, int page, int size) {
        // Access Control via dedicated service
        ChatRoomMetadataDTO chatroom = verificationService.verifyMembership(chatroomId, currentUserId);

        // Fetch paginated messages
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
     * Delegates to ChatReadStatusService.
     */
    @Override
    @Transactional(readOnly = true)
    public boolean hasUnreadMessages(Integer userId) {
        return readStatusService.hasUnreadMessages(userId);
    }

    /**
     * Marks the chatroom as read for the specific user.
     * Updates persistence layer via {@link ChatReadStatusService} and broadcasts
     * a read receipt via WebSocket.
     */
    @Override
    @Transactional
    public void markRoomAsRead(Integer chatroomId, Integer userId) {
        readStatusService.markRoomAsRead(chatroomId, userId);

        // Broadcast Read Receipt
        ChatMessageDTO readReceipt = new ChatMessageDTO();
        readReceipt.setChatroomId(chatroomId);
        readReceipt.setIsRead(true);
        messagingTemplate.convertAndSend("/topic/chatroom." + chatroomId + ".read", readReceipt);
    }

    /**
     * Finds or creates a chatroom between two users.
     * Delegates entirely to the creation strategy to ensure consistency.
     */
    @Override
    @Transactional
    public ChatRoomDTO findOrCreateChatroom(Integer currentUserId, Integer partnerId, Integer chatroomType) {
        ChatRoomEntity entity = chatRoomCreationStrategy.findOrCreate(currentUserId, partnerId, chatroomType);

        String partnerName = metadataReader.getMemberProfile(partnerId).getMemberName();

        return chatRoomMapper.toDto(entity, currentUserId, partnerName);
    }

    // ============================================================
    // PRIVATE HELPERS
    // ============================================================

    private ChatRoomEntity resolveChatroom(Integer chatroomId, Integer senderId, Integer receiverId) {
        if (chatroomId != null) {
            ChatRoomMetadataDTO meta = metadataReader.getRoomMetadata(chatroomId);
            if (meta != null) {
                ChatRoomEntity entity = new ChatRoomEntity();
                entity.setChatroomId(meta.getChatroomId());
                entity.setMemId1(meta.getMemberIds().get(0));
                entity.setMemId2(meta.getMemberIds().get(1));
                return entity;
            }
        }
        // Fallback: Legacy/New Chat Flow - Default to Type 0
        return chatRoomCreationStrategy.findOrCreate(senderId, receiverId, 0);
    }

    private ChatMessageDTO buildResponseDto(ChatMessageEntity saved, String senderName, Integer receiverId) {
        // Resolve Sender Entity
        MemberProfileDTO sender = metadataReader.getMemberProfile(saved.getMemberId());

        // Resolve Reply Context
        String replyContent = null;
        String replySenderName = null;
        if (saved.getReplyToMessageId() != null) {
            ChatMessageEntity replyMsg = messageReader.findById(saved.getReplyToMessageId()).orElse(null);
            if (replyMsg != null) {
                replyContent = replyMsg.getMessage();
                replySenderName = metadataReader.getMemberProfile(replyMsg.getMemberId()).getMemberName();
            }
        }

        return messageMapper.toDto(saved, sender, replyContent, replySenderName, saved.getMemberId(), receiverId, 0); // New
                                                                                                                      // messages
                                                                                                                      // are
                                                                                                                      // not
                                                                                                                      // reported
                                                                                                                      // yet
    }

    private List<ChatMessageEntity> fetchMessagesAsc(Integer chatroomId, Pageable pageable) {
        List<ChatMessageEntity> desc = messageReader.findLatestMessages(chatroomId, pageable);
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

        LocalDateTime partnerLastReadAt = currentUserId.equals(chatroom.getMemberIds().get(0))
                ? chatroom.getMem2LastReadAt()
                : chatroom.getMem1LastReadAt();

        if (partnerLastReadAt == null) {
            return;
        }

        for (int i = dtos.size() - 1; i >= 0; i--) {
            ChatMessageDTO msg = dtos.get(i);
            if (currentUserId.equals(msg.getSenderId())) {
                if (msg.getChatTime() != null && !msg.getChatTime().isAfter(partnerLastReadAt)) {
                    msg.setIsRead(true);
                }
                break;
            }
        }
    }

    // ============================================================
    // DATA AGGREGATION HELPERS
    // ============================================================

    private Map<Integer, MemberProfileDTO> resolveMemberMap(List<ChatMessageEntity> messages,
            Map<String, ChatMessageEntity> replyMap) {
        java.util.Set<Integer> memberIds = new java.util.HashSet<>();

        // 1. Collect IDs from current batch
        messages.forEach(msg -> memberIds.add(msg.getMemberId()));

        // 2. Collect IDs from referenced replies
        replyMap.values().forEach(replyMsg -> memberIds.add(replyMsg.getMemberId()));

        if (memberIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return metadataReader.getMemberProfiles(new ArrayList<>(memberIds));
    }

    private Map<String, ChatMessageEntity> resolveReplyMap(List<ChatMessageEntity> messages) {
        Set<String> replyIds = messages.stream()
                .map(ChatMessageEntity::getReplyToMessageId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        if (replyIds.isEmpty()) {
            return Collections.emptyMap();
        }

        // Optimization: Try to find reply messages in the current batch first
        Map<String, ChatMessageEntity> foundInBatch = messages.stream()
                .filter(msg -> replyIds.contains(msg.getMessageId()))
                .collect(Collectors.toMap(ChatMessageEntity::getMessageId, Function.identity()));

        Set<String> missingIds = replyIds.stream()
                .filter(id -> !foundInBatch.containsKey(id))
                .collect(Collectors.toSet());

        if (missingIds.isEmpty()) {
            return foundInBatch;
        }

        // Only query the strategy (which might hit MySQL) for missing IDs
        Map<String, ChatMessageEntity> results = new java.util.HashMap<>(foundInBatch);
        results.putAll(messageReader.findAllById(missingIds).stream()
                .collect(Collectors.toMap(ChatMessageEntity::getMessageId, Function.identity())));

        return results;
    }

    private Map<String, Integer> resolveReportStatusMap(Integer currentUserId, List<ChatMessageEntity> messages) {
        // Resolve report status for the current user to display in the UI (e.g.,
        // "Reported" flag).
        // Delegates to ReportStrategyService for efficient Batch Retrieval.
        // Schema: Cache-Aside (Redis Batch Get -> MySQL Fallback).

        if (messages.isEmpty()) {
            return Collections.emptyMap();
        }

        List<String> messageIds = messages.stream()
                .map(ChatMessageEntity::getMessageId)
                .collect(Collectors.toList());

        // Use Strategy for efficient Batch Retrieval (Cache-Aside Zero SQL)
        return reportStrategy.getBatchStatus(currentUserId, messageIds);
    }
}
