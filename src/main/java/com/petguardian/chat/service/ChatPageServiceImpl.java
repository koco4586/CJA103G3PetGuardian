package com.petguardian.chat.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.petguardian.chat.model.ChatMemberDTO;
import com.petguardian.chat.model.ChatMemberRepository;
import com.petguardian.chat.model.ChatRoomRepository;
import com.petguardian.chat.model.ChatRoomVO;

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

    public ChatPageServiceImpl(ChatMemberRepository memberRepository,
            ChatRoomRepository chatroomRepository) {
        this.memberRepository = memberRepository;
        this.chatroomRepository = chatroomRepository;
    }

    // ============================================================
    // PUBLIC API
    // ============================================================

    @Override
    public List<ChatMemberDTO> getAllMembers() {
        return memberRepository.findAll().stream()
                .map(ChatMemberDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public ChatMemberDTO getMember(Integer memId) {
        return memberRepository.findById(memId)
                .map(ChatMemberDTO::fromEntity)
                .orElse(null);
    }

    /**
     * Aggregates the latest message preview for all valid chatrooms.
     * Uses pre-computed `lastMessagePreview` from ChatRoomVO to avoid expensive
     * join queries.
     * 
     * @param currentUserId context user
     * @return {@code Map<PartnerId, MessagePreview>}
     */
    @Override
    public Map<Integer, String> getLastMessages(Integer currentUserId) {
        Map<Integer, String> resultMap = new HashMap<>();

        List<ChatRoomVO> chatrooms = chatroomRepository.findByMemId1OrMemId2(currentUserId, currentUserId);

        for (ChatRoomVO room : chatrooms) {
            Integer partnerId = room.getOtherMemberId(currentUserId);

            String preview = room.getLastMessagePreview();
            if (preview != null && !preview.isEmpty()) {
                // Truncate for UI consistency if needed (DB column is 200, UI might need less)
                if (preview.length() > 30) {
                    preview = preview.substring(0, 27) + "...";
                }
                resultMap.put(partnerId, preview);
            }
        }

        return resultMap;
    }
}
