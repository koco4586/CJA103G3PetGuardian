package com.petguardian.chat.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.petguardian.chat.model.ChatMemberRepository;
import com.petguardian.chat.model.ChatMemberVO;
import com.petguardian.chat.model.ChatMessageRepository;
import com.petguardian.chat.model.ChatMessageVO;
import com.petguardian.chat.model.ChatRoomRepository;
import com.petguardian.chat.model.ChatRoomVO;

/**
 * Implementation of {@link ChatPageService}.
 * Optimizes initial data loading for the chat UI.
 */
@Service
public class ChatPageServiceImpl implements ChatPageService {

    private final ChatMemberRepository memberRepository;
    private final ChatRoomRepository chatroomRepository;
    private final ChatMessageRepository messageRepository;

    public ChatPageServiceImpl(ChatMemberRepository memberRepository,
            ChatRoomRepository chatroomRepository,
            ChatMessageRepository messageRepository) {
        this.memberRepository = memberRepository;
        this.chatroomRepository = chatroomRepository;
        this.messageRepository = messageRepository;
    }

    @Override
    public List<ChatMemberVO> getAllMembers() {
        return memberRepository.findAll();
    }

    @Override
    public ChatMemberVO getMember(Integer memId) {
        return memberRepository.findById(memId).orElse(null);
    }

    @Override
    public Map<Integer, String> getLastMessages(Integer currentUserId) {
        Map<Integer, String> resultMap = new HashMap<>();

        // Optimization: Query rooms first to restrict message lookup scope
        List<ChatRoomVO> chatrooms = chatroomRepository.findByMemId1OrMemId2(currentUserId, currentUserId);

        for (ChatRoomVO room : chatrooms) {
            // Identify Partner
            Integer partnerId = room.getOtherMemberId(currentUserId);

            // Fetch Head Message (Limit 1)
            List<ChatMessageVO> messages = messageRepository.findLatest(
                    room.getChatroomId(),
                    PageRequest.of(0, 1));

            if (!messages.isEmpty()) {
                String content = messages.get(0).getMessage();
                // View Truncation Logic
                if (content.length() > 30) {
                    content = content.substring(0, 27) + "...";
                }
                resultMap.put(partnerId, content);
            }
        }

        return resultMap;
    }
}
