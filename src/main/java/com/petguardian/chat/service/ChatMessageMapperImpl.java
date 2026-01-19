package com.petguardian.chat.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.petguardian.chat.model.ChatMemberRepository;
import com.petguardian.chat.model.ChatMemberVO;
import com.petguardian.chat.model.ChatMessageDTO;
import com.petguardian.chat.model.ChatMessageVO;

@Service
public class ChatMessageMapperImpl implements ChatMessageMapper {

    private final ChatMemberRepository memberRepository;
    private final MessageStrategyService messageStrategyService;

    public ChatMessageMapperImpl(ChatMemberRepository memberRepository, MessageStrategyService messageStrategyService) {
        this.memberRepository = memberRepository;
        this.messageStrategyService = messageStrategyService;
    }

    @Override
    public ChatMessageDTO toDto(ChatMessageVO entity, ChatMemberVO sender, String replyContent, String replySenderName,
            Integer currentUserId, Integer partnerId) {
        ChatMessageDTO dto = new ChatMessageDTO();
        dto.setMessageId(entity.getMessageId());
        dto.setSenderId(entity.getMemberId());
        dto.setReceiverId(entity.getMemberId().equals(currentUserId) ? partnerId : currentUserId);
        dto.setContent(entity.getMessage());
        dto.setSenderName(sender != null ? sender.getMemName() : "Unknown");
        dto.setChatroomId(entity.getChatroomId());

        if (entity.getReplyToMessageId() != null) {
            dto.setReplyToId(entity.getReplyToMessageId());
            dto.setReplyToContent(replyContent);
            dto.setReplyToSenderName(replySenderName);
        }

        return dto;
    }

    @Override
    public List<ChatMessageDTO> toDtoList(List<ChatMessageVO> entities, Integer currentUserId, Integer partnerId) {
        if (entities.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Integer, ChatMemberVO> memberMap = resolveMemberMap(entities);
        Map<String, ChatMessageVO> replyMap = resolveReplyMap(entities, memberMap);

        return entities.stream()
                .map(msg -> {
                    ChatMemberVO sender = memberMap.get(msg.getMemberId());
                    String replyContent = null;
                    String replySenderName = null;

                    if (msg.getReplyToMessageId() != null) {
                        ChatMessageVO replyMsg = replyMap.get(msg.getReplyToMessageId());
                        if (replyMsg != null) {
                            replyContent = replyMsg.getMessage();
                            ChatMemberVO replySender = memberMap.get(replyMsg.getMemberId());
                            if (replySender != null) {
                                replySenderName = replySender.getMemName();
                            }
                        }
                    }

                    return toDto(msg, sender, replyContent, replySenderName, currentUserId, partnerId);
                })
                .collect(Collectors.toList());
    }

    @Override
    public void decorateReplyContext(ChatMessageDTO dto, String replyToMessageId) {
        if (replyToMessageId == null) {
            return;
        }
        dto.setReplyToId(replyToMessageId);

        messageStrategyService.findById(replyToMessageId).ifPresent(replyMsg -> {
            dto.setReplyToContent(replyMsg.getMessage());
            memberRepository.findById(replyMsg.getMemberId()).ifPresent(replySender -> {
                dto.setReplyToSenderName(replySender.getMemName());
            });
        });
    }

    private Map<Integer, ChatMemberVO> resolveMemberMap(List<ChatMessageVO> messages) {
        Set<Integer> memberIds = messages.stream()
                .map(ChatMessageVO::getMemberId)
                .collect(Collectors.toSet());

        return memberRepository.findAllById(memberIds).stream()
                .collect(Collectors.toMap(ChatMemberVO::getMemId, Function.identity()));
    }

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
}
