package com.petguardian.chat.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.petguardian.chat.model.ChatMessageDTO;
import com.petguardian.chat.model.ChatRoomRepository;
import com.petguardian.chat.model.ChatRoomVO;
import com.petguardian.chat.service.AuthStrategyService;
import com.petguardian.chat.service.ChatService;

import jakarta.servlet.http.HttpServletRequest;

/**
 * REST Controller for Chat Resource Management.
 * Provides endpoints for chatroom resolution and message history retrieval.
 */
@RestController
@RequestMapping("/api/chatrooms")
public class ChatApiController {

    private final ChatRoomRepository chatroomRepository;
    private final AuthStrategyService authStrategyService;
    private final ChatService chatService;

    public ChatApiController(ChatRoomRepository chatroomRepository,
            AuthStrategyService authStrategyService,
            ChatService chatService) {
        this.chatroomRepository = chatroomRepository;
        this.authStrategyService = authStrategyService;
        this.chatService = chatService;
    }

    /**
     * Endpoint: Chatroom Resolution.
     * Resolves or validates the existence of a chat session with a specific
     * partner.
     * 
     * @param request   HTTP Request (for Auth)
     * @param partnerId Target User ID
     * @return ChatRoomVO if found, or 404
     */
    @GetMapping
    public ResponseEntity<ChatRoomVO> findChatroom(
            HttpServletRequest request,
            @RequestParam Integer partnerId,
            @RequestParam(required = false, defaultValue = "0") Integer chatroomType) {

        Integer currentUserId = authStrategyService.getCurrentUserId(request);
        if (currentUserId == null) {
            return ResponseEntity.status(401).build();
        }

        // Unified Resolution (normalized: smaller ID first)
        Integer memId1 = Math.min(currentUserId, partnerId);
        Integer memId2 = Math.max(currentUserId, partnerId);

        ChatRoomVO chatroom = chatroomRepository.findByMemId1AndMemId2AndChatroomType(memId1, memId2, chatroomType)
                .orElse(null);

        if (chatroom == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(chatroom);
    }

    /**
     * Endpoint: Message History.
     * Retrieves paginated message history for a subscribed chatroom.
     * 
     * @param request    HTTP Request (for Auth)
     * @param chatroomId Target Chatroom ID
     * @param page       Page index
     * @param size       Page size
     * @return List of Message DTOs
     */
    @GetMapping("/{chatroomId}/messages")
    public ResponseEntity<List<ChatMessageDTO>> getMessages(
            HttpServletRequest request,
            @PathVariable Integer chatroomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        Integer currentUserId = authStrategyService.getCurrentUserId(request);
        if (currentUserId == null) {
            return ResponseEntity.status(401).build();
        }

        // Service Delegation (Includes Security Validation)
        try {
            List<ChatMessageDTO> dtos = chatService.getChatHistory(chatroomId, currentUserId, page, size);
            return ResponseEntity.ok(dtos);
        } catch (RuntimeException e) {
            return ResponseEntity.status(403).build();
        }
    }
}
