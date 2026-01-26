package com.petguardian.chat.service.chatmessage;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.function.Function;

import org.springframework.stereotype.Service;

import com.petguardian.chat.model.ChatMemberDTO;
import com.petguardian.chat.model.ChatMemberRepository;
import com.petguardian.chat.model.ChatRoomDTO;
import com.petguardian.chat.model.ChatRoomRepository;
import com.petguardian.chat.model.ChatRoomEntity;
import com.petguardian.chat.model.ChatMemberEntity;
import com.petguardian.chat.service.mapper.ChatRoomMapper;

/**
 * Service Implementation for Chat View Data Aggregation.
 * 
 * Responsibilities:
 * - Optimized data loading for initial page rendering
 * - DTO projection to decouple View layer from JPA Entities
 * - Efficient multi-entity aggregation (e.g. Member + LastMessage)
 */
@Service
public class ChatPageServiceImpl implements ChatPageService {

    // ============================================================
    // DEPENDENCIES
    // ============================================================

    private final ChatMemberRepository memberRepository;
    private final ChatRoomRepository chatroomRepository;
    private final ChatRoomMapper chatRoomMapper;

    public ChatPageServiceImpl(ChatMemberRepository memberRepository,
            ChatRoomRepository chatroomRepository,
            ChatRoomMapper chatRoomMapper) {
        this.memberRepository = memberRepository;
        this.chatroomRepository = chatroomRepository;
        this.chatRoomMapper = chatRoomMapper;
    }

    // ============================================================
    // PUBLIC API
    // ============================================================

    @Override
    public ChatMemberDTO getMember(Integer memId) {
        return memberRepository.findById(memId)
                .map(ChatMemberDTO::fromEntity)
                .orElse(null);
    }

    /**
     * Retrieves all chatrooms for the current user.
     * <p>
     * Uses batch loading for partner data to avoid N+1 queries.
     * </p>
     * 
     * @param currentUserId The ID of the current user
     * @return List of sorted ChatRoomDTOs
     */
    @Override
    public List<ChatRoomDTO> getMyChatrooms(Integer currentUserId) {
        // 1. Fetch all chatrooms for user
        List<ChatRoomEntity> chatrooms = chatroomRepository.findByMemId1OrMemId2(currentUserId, currentUserId);

        // 2. Batch load partners (Avoid N+1)
        Map<Integer, ChatMemberEntity> memberMap = batchLoadPartners(chatrooms, currentUserId);

        // 3. Map to DTOs and sort by latest activity
        return chatrooms.stream()
                .map(room -> chatRoomMapper.toDto(room, currentUserId, memberMap))
                .sorted((d1, d2) -> {
                    if (d1.getLastMessageTime() == null)
                        return 1;
                    if (d2.getLastMessageTime() == null)
                        return -1;
                    return d2.getLastMessageTime().compareTo(d1.getLastMessageTime());
                })
                .collect(Collectors.toList());
    }

    // ============================================================
    // PRIVATE HELPERS
    // ============================================================

    private Map<Integer, ChatMemberEntity> batchLoadPartners(List<ChatRoomEntity> chatrooms, Integer currentUserId) {
        java.util.Set<Integer> partnerIds = chatrooms.stream()
                .map(r -> r.getOtherMemberId(currentUserId))
                .collect(Collectors.toSet());

        return memberRepository.findAllById(partnerIds).stream()
                .collect(Collectors.toMap(ChatMemberEntity::getMemId, Function.identity()));
    }
}
