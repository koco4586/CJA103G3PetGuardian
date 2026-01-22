package com.petguardian.chat.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.petguardian.chat.model.ChatMessageDTO;
import com.petguardian.chat.model.ChatMessageEntity;
import com.petguardian.chat.model.ChatRoomRepository;
import com.petguardian.chat.model.ChatRoomEntity;

/**
 * Service Implementation for Core Chat Functionality.
 * 
 * Responsibilities:
 * - Orchestrates the message processing flow (Validation -> Persistence ->
 * Notification)
 * - Manages chat history retrieval with pagination
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

    // Extracted Services (P1 Refactoring)
    private final ChatMessageMapper messageMapper;
    private final ChatReadStatusService readStatusService;
    private final ChatVerificationService verificationService;

    public ChatServiceImpl(
            ChatRoomRepository chatroomRepository,
            MessageStrategyService messageStrategyService,
            ChatRoomCreationStrategy chatRoomCreationStrategy,
            ChatMessageMapper messageMapper,
            ChatReadStatusService readStatusService,
            ChatVerificationService verificationService) {
        this.chatroomRepository = chatroomRepository;
        this.messageStrategyService = messageStrategyService;
        this.chatRoomCreationStrategy = chatRoomCreationStrategy;
        this.messageMapper = messageMapper;
        this.readStatusService = readStatusService;
        this.verificationService = verificationService;
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

        // Delegate persistence to strategy (supports Sync MySQL or Async Redis)
        ChatMessageEntity saved = messageStrategyService.save(
                chatroom.getChatroomId(), senderId, dto.getContent(), dto.getReplyToId());

        // Update chatroom metadata (last message, sender's read status)
        updateChatroomAfterMessage(chatroom, senderId, dto.getContent());

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

        // Delegate DTO conversion to mapper
        Integer partnerId = chatroom.getOtherMemberId(currentUserId);
        List<ChatMessageDTO> dtos = messageMapper.toDtoList(messages, currentUserId, partnerId);

        // Mark last sent message as read if partner has read (only for first page)
        if (page == 0) {
            LocalDateTime partnerLastReadAt = currentUserId.equals(chatroom.getMemId1())
                    ? chatroom.getMem2LastReadAt()
                    : chatroom.getMem1LastReadAt();
            if (partnerLastReadAt != null) {
                for (int i = dtos.size() - 1; i >= 0; i--) {
                    if (currentUserId.equals(dtos.get(i).getSenderId())) {
                        dtos.get(i).setIsRead(true);
                        break;
                    }
                }
            }
        }

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
     * Delegates to ChatReadStatusService.
     */
    @Override
    @Transactional
    public void markRoomAsRead(Integer chatroomId, Integer userId) {
        readStatusService.markRoomAsRead(chatroomId, userId);
    }

    /**
     * Finds an existing chatroom between two users with a specific type.
     * Does NOT create a new room if not found.
     */
    @Override
    @Transactional(readOnly = true)
    public ChatRoomEntity findChatroom(Integer currentUserId, Integer partnerId, Integer chatroomType) {
        Integer memId1 = Math.min(currentUserId, partnerId);
        Integer memId2 = Math.max(currentUserId, partnerId);

        return chatroomRepository.findByMemId1AndMemId2AndChatroomType(memId1, memId2, chatroomType)
                .orElse(null);
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

    private void updateChatroomAfterMessage(ChatRoomEntity chatroom, Integer senderId, String content) {
        chatroom.setLastMessageAt(LocalDateTime.now());
        String preview = content.length() > 200 ? content.substring(0, 200) : content;
        chatroom.setLastMessagePreview(preview);

        // Update sender's read status
        chatroom.updateLastReadAt(senderId);

        chatroomRepository.save(chatroom);
    }

    private ChatMessageDTO buildResponseDto(ChatMessageEntity saved, String senderName, Integer receiverId) {
        ChatMessageDTO responseDto = new ChatMessageDTO();
        responseDto.setMessageId(saved.getMessageId());
        responseDto.setSenderId(saved.getMemberId());
        responseDto.setReceiverId(receiverId);
        responseDto.setContent(saved.getMessage());
        responseDto.setSenderName(senderName);
        responseDto.setChatroomId(saved.getChatroomId());

        // Delegate reply context decoration to mapper
        messageMapper.decorateReplyContext(responseDto, saved.getReplyToMessageId());

        return responseDto;
    }

    private List<ChatMessageEntity> fetchMessagesAsc(Integer chatroomId, Pageable pageable) {
        List<ChatMessageEntity> desc = messageStrategyService.findLatestMessages(chatroomId, pageable);
        List<ChatMessageEntity> asc = new ArrayList<>(desc);
        Collections.reverse(asc);
        return asc;
    }
}
