package com.petguardian.chat.service;

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
import com.petguardian.chat.model.ChatMemberVO;
import com.petguardian.chat.model.ChatMessageDTO;
import com.petguardian.chat.model.ChatMessageVO;
import com.petguardian.chat.model.ChatRoomRepository;
import com.petguardian.chat.model.ChatRoomVO;

/**
 * Service Implementation for Core Chat Functionality.
 * 
 * Responsibilities:
 * - Orchestrates the message processing flow (Validation -> Persistence ->
 * Notification)
 * - Manages chat history retrieval with pagination
 * - Handles complex DTO assembly (resolving member names, reply contexts)
 * 
 * Architecture Note:
 * persistence is delegated to {@link MessageStrategyService} to support future
 * Redis Write-Behind strategy without modifying this core service (DIP).
 */
@Service
public class ChatServiceImpl implements ChatService {

    // ============================================================
    // DEPENDENCIES
    // ============================================================
    private final ChatRoomRepository chatroomRepository;
    private final MessageStrategyService messageStrategyService;
    private final ChatMemberRepository memberRepository;
    private final ChatRoomCreationStrategy chatRoomCreationStrategy;

    public ChatServiceImpl(ChatRoomRepository chatroomRepository,
            MessageStrategyService messageStrategyService,
            ChatMemberRepository memberRepository,
            ChatRoomCreationStrategy chatRoomCreationStrategy) {
        this.chatroomRepository = chatroomRepository;
        this.messageStrategyService = messageStrategyService;
        this.memberRepository = memberRepository;
        this.chatRoomCreationStrategy = chatRoomCreationStrategy;
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
        ChatRoomVO chatroom = chatRoomCreationStrategy.findOrCreate(senderId, receiverId, 0);

        // Delegate persistence to strategy (supports Sync MySQL or Async Redis)
        ChatMessageVO saved = messageStrategyService.save(
                chatroom.getChatroomId(), senderId, dto.getContent(), dto.getReplyToId());

        return buildResponseDto(saved, dto.getSenderName(), receiverId);
    }

    /**
     * Retrieves paginated chat history for a specific room.
     * Validates access rights and enriches messages with sender/reply details.
     */
    @Override
    @Transactional(readOnly = true)
    public List<ChatMessageDTO> getChatHistory(Integer chatroomId, Integer currentUserId, int page, int size) {
        // Access Control: Ensure user is a member of the chatroom
        ChatRoomVO chatroom = chatroomRepository.findById(chatroomId).orElse(null);
        if (chatroom == null) {
            return Collections.emptyList();
        }
        if (!currentUserId.equals(chatroom.getMemId1()) && !currentUserId.equals(chatroom.getMemId2())) {
            throw new RuntimeException("Access denied");
        }

        // Fetch paginated messages (Strategy handles DB/Cache retrieval)
        Pageable pageable = PageRequest.of(page, size);
        List<ChatMessageVO> messages = fetchMessagesAsc(chatroomId, pageable);

        if (messages.isEmpty()) {
            return Collections.emptyList();
        }

        // Batch resolve dependencies to avoid N+1 queries
        Integer partnerId = chatroom.getOtherMemberId(currentUserId);
        Map<Integer, ChatMemberVO> memberMap = resolveMemberMap(messages);
        Map<String, ChatMessageVO> replyMap = resolveReplyMap(messages, memberMap);

        // Map Entities to DTOs
        return messages.stream()
                .map(msg -> toDto(msg, memberMap, replyMap, currentUserId, partnerId))
                .collect(Collectors.toList());
    }

    // ============================================================
    // PRIVATE HELPERS
    // ============================================================

    /**
     * Constructs the response DTO for a newly saved message.
     */
    private ChatMessageDTO buildResponseDto(ChatMessageVO saved, String senderName, Integer receiverId) {
        ChatMessageDTO responseDto = new ChatMessageDTO();
        responseDto.setMessageId(saved.getMessageId());
        responseDto.setSenderId(saved.getMemberId());
        responseDto.setReceiverId(receiverId); // Explicitly set for frontend hydration
        responseDto.setContent(saved.getMessage());
        responseDto.setSenderName(senderName);

        // Enrich with reply context if applicable
        decorateReplyContext(responseDto, saved.getReplyToMessageId());

        return responseDto;
    }

    private List<ChatMessageVO> fetchMessagesAsc(Integer chatroomId, Pageable pageable) {
        // Fetch latest DESC (efficient for pagination) then reverse to ASC for display
        List<ChatMessageVO> desc = messageStrategyService.findLatestMessages(chatroomId, pageable);
        List<ChatMessageVO> asc = new ArrayList<>(desc);
        Collections.reverse(asc);
        return asc;
    }

    /**
     * Bulk resolves member information for all participants in the message list.
     */
    private Map<Integer, ChatMemberVO> resolveMemberMap(List<ChatMessageVO> messages) {
        Set<Integer> memberIds = messages.stream()
                .map(ChatMessageVO::getMemberId)
                .collect(Collectors.toSet());

        return memberRepository.findAllById(memberIds).stream()
                .collect(Collectors.toMap(ChatMemberVO::getMemId, Function.identity()));
    }

    /**
     * Resolves referenced reply messages and ensures their authors are also loaded.
     */
    private Map<String, ChatMessageVO> resolveReplyMap(List<ChatMessageVO> messages,
            Map<Integer, ChatMemberVO> memberMap) {
        Set<String> replyIds = messages.stream()
                .map(ChatMessageVO::getReplyToMessageId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        if (replyIds.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, ChatMessageVO> replyMap = messageStrategyService.findAllById(replyIds).stream()
                .collect(Collectors.toMap(ChatMessageVO::getMessageId, Function.identity()));

        // identify sender IDs from replies that haven't been loaded yet (edge case)
        Set<Integer> missingSenderIds = replyMap.values().stream()
                .map(ChatMessageVO::getMemberId)
                .filter(id -> !memberMap.containsKey(id))
                .collect(Collectors.toSet());

        if (!missingSenderIds.isEmpty()) {
            memberRepository.findAllById(missingSenderIds)
                    .forEach(m -> memberMap.put(m.getMemId(), m));
        }

        return replyMap;
    }

    /**
     * Converts a message entity to DTO with full context (sender names, reply
     * content).
     */
    private ChatMessageDTO toDto(ChatMessageVO msg, Map<Integer, ChatMemberVO> memberMap,
            Map<String, ChatMessageVO> replyMap, Integer currentUserId, Integer partnerId) {
        ChatMessageDTO dto = new ChatMessageDTO();
        dto.setMessageId(msg.getMessageId());
        dto.setSenderId(msg.getMemberId());
        dto.setReceiverId(msg.getMemberId().equals(currentUserId) ? partnerId : currentUserId);
        dto.setContent(msg.getMessage());

        ChatMemberVO sender = memberMap.get(msg.getMemberId());
        dto.setSenderName(sender != null ? sender.getMemName() : "Unknown");

        // Reply decoration
        if (msg.getReplyToMessageId() != null) {
            dto.setReplyToId(msg.getReplyToMessageId());
            ChatMessageVO replyMsg = replyMap.get(msg.getReplyToMessageId());

            if (replyMsg != null) {
                dto.setReplyToContent(replyMsg.getMessage());
                ChatMemberVO replySender = memberMap.get(replyMsg.getMemberId());
                if (replySender != null) {
                    dto.setReplyToSenderName(replySender.getMemName());
                }
            }
        }

        return dto;
    }

    private void decorateReplyContext(ChatMessageDTO dto, String replyToMessageId) {
        if (replyToMessageId == null) {
            return;
        }
        dto.setReplyToId(replyToMessageId);

        // Single lookup for individual message processing (rare case compared to batch)
        messageStrategyService.findById(replyToMessageId).ifPresent(replyMsg -> {
            dto.setReplyToContent(replyMsg.getMessage());
            memberRepository.findById(replyMsg.getMemberId()).ifPresent(replySender -> {
                dto.setReplyToSenderName(replySender.getMemName());
            });
        });
    }
}
