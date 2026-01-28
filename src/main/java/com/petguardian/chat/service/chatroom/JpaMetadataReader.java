package com.petguardian.chat.service.chatroom;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.petguardian.chat.dto.ChatRoomMetadataDTO;
import com.petguardian.chat.dto.MemberProfileDTO;
import com.petguardian.chat.model.ChatRoomEntity;
import com.petguardian.chat.model.ChatRoomRepository;
import com.petguardian.member.model.Member;
import com.petguardian.member.repository.management.MemberManagementRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * JPA implementation for Metadata Reading.
 */
@Slf4j
@Service("jpaMetadataReader")
@RequiredArgsConstructor
public class JpaMetadataReader implements ChatRoomMetadataReader {

    private final ChatRoomRepository chatRoomRepository;
    private final MemberManagementRepository memberRepository;

    @Override
    @Transactional(readOnly = true)
    public ChatRoomMetadataDTO getRoomMetadata(Integer chatroomId) {
        return chatRoomRepository.findById(chatroomId)
                .map(this::mapToRoomDto)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatRoomMetadataDTO> getUserChatrooms(Integer userId) {
        return chatRoomRepository.findByMemId1OrMemId2(userId, userId).stream()
                .map(this::mapToRoomDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public MemberProfileDTO getMemberProfile(Integer memberId) {
        return memberRepository.findById(memberId)
                .map(this::mapToMemberDto)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Integer, MemberProfileDTO> getMemberProfiles(List<Integer> memberIds) {
        return memberRepository.findAllById(memberIds).stream()
                .map(this::mapToMemberDto)
                .collect(Collectors.toMap(MemberProfileDTO::getMemberId, p -> p));
    }

    @Override
    public Optional<ChatRoomMetadataDTO> findRoomByMembers(Integer memId1, Integer memId2) {
        Integer id1 = Math.min(memId1, memId2);
        Integer id2 = Math.max(memId1, memId2);
        return chatRoomRepository.findByMemId1AndMemId2AndChatroomType(id1, id2, 0)
                .map(this::mapToRoomDto);
    }

    private ChatRoomMetadataDTO mapToRoomDto(ChatRoomEntity entity) {
        return ChatRoomMetadataDTO.builder()
                .chatroomId(entity.getChatroomId())
                .chatroomName(entity.getChatroomName())
                .memberIds(Arrays.asList(entity.getMemId1(), entity.getMemId2()))
                .lastMessagePreview(entity.getLastMessagePreview())
                .lastMessageAt(entity.getLastMessageAt())
                .chatroomType(entity.getChatroomType())
                .chatroomStatus(entity.getChatroomStatus())
                .mem1LastReadAt(entity.getMem1LastReadAt())
                .mem2LastReadAt(entity.getMem2LastReadAt())
                .build();
    }

    private MemberProfileDTO mapToMemberDto(Member entity) {
        return MemberProfileDTO.builder()
                .memberId(entity.getMemId())
                .memberName(entity.getMemName())
                .memberImage(entity.getMemImage())
                .build();
    }
}
