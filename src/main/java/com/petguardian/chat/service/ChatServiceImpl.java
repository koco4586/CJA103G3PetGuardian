package com.petguardian.chat.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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
import com.petguardian.chat.model.ChatMessageRepository;
import com.petguardian.chat.model.ChatMessageVO;
import com.petguardian.chat.model.ChatRoomRepository;
import com.petguardian.chat.model.ChatRoomVO;

/**
 * Implementation of {@link ChatService}.
 * Orchestrates message persistence, chatroom resolution, and historical data
 * retrieval.
 */
@Service
public class ChatServiceImpl implements ChatService {

    private final ChatRoomRepository chatroomRepository;
    private final MessageStoreStrategyService messageStoreStrategyService;
    private final ChatMessageRepository messageRepository;
    private final ChatMemberRepository memberRepository;
    private final ChatRoomCreationStrategy chatRoomCreationStrategy;

    public ChatServiceImpl(ChatRoomRepository chatroomRepository,
            MessageStoreStrategyService messageStoreStrategyService,
            ChatMessageRepository messageRepository,
            ChatMemberRepository memberRepository,
            ChatRoomCreationStrategy chatRoomCreationStrategy) {
        this.chatroomRepository = chatroomRepository;
        this.messageStoreStrategyService = messageStoreStrategyService;
        this.messageRepository = messageRepository;
        this.memberRepository = memberRepository;
        this.chatRoomCreationStrategy = chatRoomCreationStrategy;
    }

    @Override
    @Transactional
    public ChatMessageDTO handleIncomingMessage(ChatMessageDTO dto) {
        Integer senderId = dto.getSenderId();
        Integer receiverId = dto.getReceiverId();
        String content = dto.getContent();

        // Chatroom Resolution
        ChatRoomVO chatroom = chatRoomCreationStrategy.findOrCreate(senderId, receiverId, 0);

        // Persistence Strategy Delegation
        ChatMessageVO saved = messageStoreStrategyService.save(chatroom.getChatroomId(), senderId, content,
                dto.getReplyToId());

        // DTO Construction
        ChatMessageDTO responseDto = new ChatMessageDTO();
        responseDto.setMessageId(saved.getMessageId());
        responseDto.setSenderId(dto.getSenderId());
        responseDto.setReceiverId(dto.getReceiverId());
        responseDto.setContent(dto.getContent());
        responseDto.setSenderName(dto.getSenderName());

        // Reply Context Decoration
        if (saved.getReplyToMessageId() != null) {
            responseDto.setReplyToId(saved.getReplyToMessageId());
            messageRepository.findById(saved.getReplyToMessageId()).ifPresent(replyMsg -> {
                responseDto.setReplyToContent(replyMsg.getMessage());
                memberRepository.findById(replyMsg.getMemberId()).ifPresent(replySender -> {
                    responseDto.setReplyToSenderName(replySender.getMemName());
                });
            });
        }

        return responseDto;
    }

    // findOrCreateChatroom removed in favor of ChatRoomCreationStrategy

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessageDTO> getChatHistory(Integer chatroomId, Integer currentUserId, int page, int size) {
        // Access Verification
        ChatRoomVO chatroom = chatroomRepository.findById(chatroomId).orElse(null);
        if (chatroom == null) {
            return Collections.emptyList();
        }

        if (!currentUserId.equals(chatroom.getMemId1()) && !currentUserId.equals(chatroom.getMemId2())) {
            throw new RuntimeException("Access denied");
        }

        Integer partnerId = chatroom.getOtherMemberId(currentUserId);

        // Pagination: Fetch & Sort
        Pageable pageable = PageRequest.of(page, size);
        List<ChatMessageVO> messagesDesc = messageRepository.findLatest(chatroomId, pageable);

        if (messagesDesc.isEmpty()) {
            return Collections.emptyList();
        }

        // Chronological Reordering (ASC)
        List<ChatMessageVO> messages = new ArrayList<>(messagesDesc);
        Collections.reverse(messages);

        // Batch Fetch Preparation
        Set<Integer> memberIds = new HashSet<>();
        Set<Integer> replyMessageIds = new HashSet<>();

        for (ChatMessageVO msg : messages) {
            memberIds.add(msg.getMemberId());
            if (msg.getReplyToMessageId() != null) {
                replyMessageIds.add(msg.getReplyToMessageId());
            }
        }

        // Entity Resolution (Batch)
        Map<Integer, ChatMemberVO> memberMap = memberRepository.findAllById(memberIds).stream()
                .collect(Collectors.toMap(ChatMemberVO::getMemId, Function.identity()));

        // Reply Resolution
        Map<Integer, ChatMessageVO> tempReplyMap = new HashMap<>();
        if (!replyMessageIds.isEmpty()) {
            tempReplyMap = messageRepository.findAllById(replyMessageIds).stream()
                    .collect(Collectors.toMap(ChatMessageVO::getMessageId, Function.identity()));

            // Resolve Reply Senders
            Set<Integer> replySenderIds = new HashSet<>();
            for (ChatMessageVO replyMsg : tempReplyMap.values()) {
                if (!memberMap.containsKey(replyMsg.getMemberId())) {
                    replySenderIds.add(replyMsg.getMemberId());
                }
            }
            if (!replySenderIds.isEmpty()) {
                memberRepository.findAllById(replySenderIds).forEach(m -> memberMap.put(m.getMemId(), m));
            }
        }
        final Map<Integer, ChatMessageVO> replyMessageMap = tempReplyMap;

        // DTO Mapping (O(1))
        return messages.stream().map(msg -> {
            ChatMessageDTO dto = new ChatMessageDTO();
            dto.setMessageId(msg.getMessageId());
            dto.setSenderId(msg.getMemberId());
            dto.setReceiverId(msg.getMemberId().equals(currentUserId) ? partnerId : currentUserId);
            dto.setContent(msg.getMessage());

            ChatMemberVO sender = memberMap.get(msg.getMemberId());
            dto.setSenderName(sender != null ? sender.getMemName() : "Unknown");

            if (msg.getReplyToMessageId() != null) {
                dto.setReplyToId(msg.getReplyToMessageId());
                ChatMessageVO replyMsg = replyMessageMap.get(msg.getReplyToMessageId());
                if (replyMsg != null) {
                    dto.setReplyToContent(replyMsg.getMessage());
                    ChatMemberVO replySender = memberMap.get(replyMsg.getMemberId());
                    if (replySender != null) {
                        dto.setReplyToSenderName(replySender.getMemName());
                    }
                }
            }

            return dto;
        }).collect(Collectors.toList());
    }
}
