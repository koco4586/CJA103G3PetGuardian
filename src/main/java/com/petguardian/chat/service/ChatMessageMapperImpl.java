package com.petguardian.chat.service;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.petguardian.chat.model.ChatMemberRepository;
import com.petguardian.chat.model.ChatMemberEntity;
import com.petguardian.chat.model.ChatMessageDTO;
import com.petguardian.chat.model.ChatMessageEntity;

@Service
public class ChatMessageMapperImpl implements ChatMessageMapper {

    // ============================================================
    // DEPENDENCIES
    // ============================================================
    private final ChatMemberRepository memberRepository;
    private final MessageStrategyService messageStrategyService;

    public ChatMessageMapperImpl(ChatMemberRepository memberRepository, MessageStrategyService messageStrategyService) {
        this.memberRepository = memberRepository;
        this.messageStrategyService = messageStrategyService;
    }

    // ============================================================
    // PUBLIC OPERATIONS
    // ============================================================

    @Override
    public ChatMessageDTO toDto(ChatMessageEntity chatMessageEntity, ChatMemberEntity sender, String replyContent,
            String replySenderName,
            Integer currentUserId, Integer partnerId) {
        ChatMessageDTO chatMessageDTO = new ChatMessageDTO();
        chatMessageDTO.setMessageId(chatMessageEntity.getMessageId());
        chatMessageDTO.setSenderId(chatMessageEntity.getMemberId());
        chatMessageDTO.setReceiverId(chatMessageEntity.getMemberId().equals(currentUserId) ? partnerId : currentUserId);
        chatMessageDTO.setContent(chatMessageEntity.getMessage());
        chatMessageDTO.setSenderName(sender != null ? sender.getMemName() : "Unknown");
        chatMessageDTO.setChatroomId(chatMessageEntity.getChatroomId());

        if (chatMessageEntity.getReplyToMessageId() != null) {
            chatMessageDTO.setReplyToId(chatMessageEntity.getReplyToMessageId());
            chatMessageDTO.setReplyToContent(replyContent);
            chatMessageDTO.setReplyToSenderName(replySenderName);
        }

        return chatMessageDTO;
    }

    /**
     * Bulk converts messages to DTOs.
     * 
     * Workflow:
     * 1. PREPARE: Pre-fetch all related data (Replies, Senders) to avoid N+1.
     * 2. MAP: Stream through entities and combine with pre-fetched data.
     */
    @Override
    public List<ChatMessageDTO> toDtoList(List<ChatMessageEntity> chatMessageEntities, Integer currentUserId,
            Integer partnerId) {
        if (chatMessageEntities.isEmpty()) {
            return Collections.emptyList();
        }

        // 1. Data Preparation (Load dependencies upfront)
        Map<String, ChatMessageEntity> replyMap = resolveReplyMap(chatMessageEntities);
        Map<Integer, ChatMemberEntity> memberMap = resolveMemberMap(chatMessageEntities, replyMap);

        // 2. Transformation (Pure mapping logic)
        return chatMessageEntities.stream()
                .map(msg -> mapToDto(msg, memberMap, replyMap, currentUserId, partnerId))
                .collect(Collectors.toList());
    }

    @Override
    public void decorateReplyContext(ChatMessageDTO chatMessageDTO, String replyToMessageId) {
        if (replyToMessageId == null) {
            return;
        }

        chatMessageDTO.setReplyToId(replyToMessageId);

        ChatMessageEntity replyMsg = messageStrategyService.findById(replyToMessageId).orElse(null);
        if (replyMsg == null) {
            return;
        }

        chatMessageDTO.setReplyToContent(replyMsg.getMessage());

        ChatMemberEntity replySender = memberRepository.findById(replyMsg.getMemberId()).orElse(null);
        if (replySender != null) {
            chatMessageDTO.setReplyToSenderName(replySender.getMemName());
        }
    }

    // ============================================================
    // INTERNAL HELPERS - MAPPING LOGIC
    // ============================================================

    /**
     * Maps a single entity to DTO using pre-resolved dependency maps.
     * 
     * Encapsulates the complexity of:
     * - Resolving the sender
     * - Resolving the parent message (if it is a reply)
     * - Resolving the parent message's sender
     */
    private ChatMessageDTO mapToDto(ChatMessageEntity msg,
            Map<Integer, ChatMemberEntity> memberMap,
            Map<String, ChatMessageEntity> replyMap,
            Integer currentUserId,
            Integer partnerId) {
        // Resolve Sender
        ChatMemberEntity sender = memberMap.get(msg.getMemberId());

        // Resolve Reply Context
        String replyContent = null;
        String replySenderName = null;

        if (msg.getReplyToMessageId() != null) {
            ChatMessageEntity replyMsg = replyMap.get(msg.getReplyToMessageId());
            if (replyMsg != null) {
                replyContent = replyMsg.getMessage();
                ChatMemberEntity replySender = memberMap.get(replyMsg.getMemberId());
                replySenderName = (replySender != null) ? replySender.getMemName() : null;
            }
        }

        // Delegate to base mapper
        return toDto(msg, sender, replyContent, replySenderName, currentUserId, partnerId);
    }

    // ============================================================
    // INTERNAL HELPERS - DATA RESOLUTION
    // ============================================================

    /**
     * Resolves all unique members involved in this batch of messages.
     * Includes both original senders AND senders of the parent messages (if
     * replies).
     */
    private Map<Integer, ChatMemberEntity> resolveMemberMap(List<ChatMessageEntity> messages,
            Map<String, ChatMessageEntity> replyMap) {
        Set<Integer> memberIds = new HashSet<>();

        // 1. Collect IDs from current batch
        messages.forEach(chatMessageEntity -> memberIds.add(chatMessageEntity.getMemberId()));

        // 2. Collect IDs from referenced replies (replacing side-effect logic)
        replyMap.values().forEach(replyMsg -> memberIds.add(replyMsg.getMemberId()));

        if (memberIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return memberRepository.findAllById(memberIds).stream()
                .collect(Collectors.toMap(ChatMemberEntity::getMemId, Function.identity()));
    }

    /**
     * Resolves all parent messages referenced by the current batch.
     */
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
