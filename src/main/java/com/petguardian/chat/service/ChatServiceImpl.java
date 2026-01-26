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

import com.petguardian.chat.model.ChatMemberRepository;
import com.petguardian.chat.model.ChatMemberEntity;
import com.petguardian.chat.model.ChatMessageDTO;
import com.petguardian.chat.model.ChatMessageEntity;
import com.petguardian.chat.model.ChatRoomDTO;
import com.petguardian.chat.model.ChatRoomEntity;
import com.petguardian.chat.model.ChatRoomRepository;
import com.petguardian.chat.service.chatmessage.MessageStrategyService;
import com.petguardian.chat.service.chatroom.ChatRoomCreationStrategy;
import com.petguardian.chat.service.chatroom.ChatVerificationService;
import com.petguardian.chat.service.mapper.ChatMessageMapper;
import com.petguardian.chat.service.mapper.ChatRoomMapper;
import com.petguardian.chat.service.status.ChatReadStatusService;
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
    private final ChatRoomRepository chatroomRepository;
    private final MessageStrategyService messageStrategyService;
    private final ChatRoomCreationStrategy chatRoomCreationStrategy;
    private final ChatMessageMapper messageMapper;
    private final ChatRoomMapper chatRoomMapper;
    private final ChatReadStatusService readStatusService;
    private final ChatVerificationService verificationService;
    private final ChatMemberRepository memberRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final TSID.Factory tsidFactory;

    public ChatServiceImpl(
            ChatRoomRepository chatroomRepository,
            ChatMemberRepository memberRepository,
            MessageStrategyService messageStrategyService,
            ChatRoomCreationStrategy chatRoomCreationStrategy,
            ChatMessageMapper messageMapper,
            ChatRoomMapper chatRoomMapper,
            ChatReadStatusService readStatusService,
            ChatVerificationService verificationService,
            SimpMessagingTemplate messagingTemplate,
            TSID.Factory tsidFactory) {
        this.chatroomRepository = chatroomRepository;
        this.memberRepository = memberRepository;
        this.messageStrategyService = messageStrategyService;
        this.chatRoomCreationStrategy = chatRoomCreationStrategy;
        this.messageMapper = messageMapper;
        this.chatRoomMapper = chatRoomMapper;
        this.readStatusService = readStatusService;
        this.verificationService = verificationService;
        this.messagingTemplate = messagingTemplate;
        this.tsidFactory = tsidFactory;
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

        // Ensure valid chatroom exists before saving message
        ChatRoomEntity chatroom = resolveChatroom(dto.getChatroomId(), senderId, receiverId);

        // Generate distributed-safe ID (TSID)
        String messageId = tsidFactory.generate().toString();

        // Build MessageCreationContext
        MessageCreationContext context = new MessageCreationContext(
                messageId,
                chatroom.getChatroomId(),
                senderId,
                dto.getContent(),
                dto.getReplyToId());

        // Delegate persistence to strategy (supports Sync MySQL or Async Redis)
        ChatMessageEntity saved = messageStrategyService.save(context);

        // Update chatroom metadata via strategy (abstracted write path)
        messageStrategyService.updateRoomMetadata(chatroom.getChatroomId(), senderId, dto.getContent());

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
        ChatRoomEntity chatroom = verificationService.verifyMembership(chatroomId, currentUserId);

        // Fetch paginated messages
        Pageable pageable = PageRequest.of(page, size);
        List<ChatMessageEntity> messages = fetchMessagesAsc(chatroomId, pageable);

        if (messages.isEmpty()) {
            return Collections.emptyList();
        }

        // Prepare Batch Data
        Map<String, ChatMessageEntity> replyMap = resolveReplyMap(messages);
        Map<Integer, ChatMemberEntity> memberMap = resolveMemberMap(messages, replyMap);

        // Delegate DTO conversion to mapper
        Integer partnerId = chatroom.getOtherMemberId(currentUserId);
        List<ChatMessageDTO> dtos = messageMapper.toDtoList(messages, currentUserId, partnerId, memberMap, replyMap);

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

        String partnerName = memberRepository.findById(partnerId)
                .map(ChatMemberEntity::getMemName)
                .orElse("Unknown User");

        return chatRoomMapper.toDto(entity, currentUserId, partnerName);
    }

    // ============================================================
    // PRIVATE HELPERS
    // ============================================================

    private ChatRoomEntity resolveChatroom(Integer chatroomId, Integer senderId, Integer receiverId) {
        if (chatroomId != null) {
            ChatRoomEntity chatroom = chatroomRepository.findById(chatroomId).orElse(null);
            if (chatroom != null) {
                return chatroom;
            }
        }
        // Fallback: Legacy/New Chat Flow - Default to Type 0
        return chatRoomCreationStrategy.findOrCreate(senderId, receiverId, 0);
    }

    private ChatMessageDTO buildResponseDto(ChatMessageEntity saved, String senderName, Integer receiverId) {
        // Resolve Sender Entity
        ChatMemberEntity sender = memberRepository.findById(saved.getMemberId()).orElse(null);

        // Resolve Reply Context
        String replyContent = null;
        String replySenderName = null;
        if (saved.getReplyToMessageId() != null) {
            ChatMessageEntity replyMsg = messageStrategyService.findById(saved.getReplyToMessageId()).orElse(null);
            if (replyMsg != null) {
                replyContent = replyMsg.getMessage();
                replySenderName = memberRepository.findById(replyMsg.getMemberId())
                        .map(ChatMemberEntity::getMemName)
                        .orElse(null);
            }
        }

        return messageMapper.toDto(saved, sender, replyContent, replySenderName, saved.getMemberId(), receiverId);
    }

    private List<ChatMessageEntity> fetchMessagesAsc(Integer chatroomId, Pageable pageable) {
        List<ChatMessageEntity> desc = messageStrategyService.findLatestMessages(chatroomId, pageable);
        List<ChatMessageEntity> asc = new ArrayList<>(desc);
        Collections.reverse(asc);
        return asc;
    }

    private void markLatestSelfMessageAsRead(List<ChatMessageDTO> dtos, ChatRoomEntity chatroom,
            Integer currentUserId,
            int page) {
        if (page != 0) {
            return;
        }

        LocalDateTime partnerLastReadAt = currentUserId.equals(chatroom.getMemId1())
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

    private Map<Integer, ChatMemberEntity> resolveMemberMap(List<ChatMessageEntity> messages,
            Map<String, ChatMessageEntity> replyMap) {
        Set<Integer> memberIds = new java.util.HashSet<>();

        // 1. Collect IDs from current batch
        messages.forEach(msg -> memberIds.add(msg.getMemberId()));

        // 2. Collect IDs from referenced replies
        replyMap.values().forEach(replyMsg -> memberIds.add(replyMsg.getMemberId()));

        if (memberIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return memberRepository.findAllById(memberIds).stream()
                .collect(Collectors.toMap(ChatMemberEntity::getMemId, Function.identity()));
    }

    private Map<String, ChatMessageEntity> resolveReplyMap(List<ChatMessageEntity> messages) {
        Set<String> replyIds = messages.stream()
                .map(ChatMessageEntity::getReplyToMessageId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        if (replyIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return messageStrategyService.findAllById(replyIds).stream()
                .collect(Collectors.toMap(ChatMessageEntity::getMessageId, Function.identity()));
    }
}
