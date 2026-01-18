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
 * 
 * Responsibilities:
 * - Provides JSON API for chatroom resolution
 * - Serves lazy-loaded message history via AJAX
 * - Enforces security and validation for data access
 */
@RestController
@RequestMapping("/api/chatrooms")
public class ChatApiController {

    // ============================================================
    // DEPENDENCIES
    // ============================================================
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

    // ============================================================
    // ENDPOINTS
    // ============================================================

    /**
     * Finds or validates a chatroom with a specific partner.
     * Used when a user selects a contact to chat with.
     * 
     * @param partnerId    Target User ID
     * @param chatroomType Room Type (Default: 0 for 1-on-1)
     * @return ChatRoomVO or 404 Not Found
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

        // Normalize participants (Convention: Lower ID first)
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
     * Retrieves paginated message history for a chatroom.
     * 
     * @param chatroomId Target Room ID
     * @param page       Page Index
     * @param size       Page Size
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

        try {
            List<ChatMessageDTO> dtos = chatService.getChatHistory(chatroomId, currentUserId, page, size);
            return ResponseEntity.ok(dtos);
        } catch (RuntimeException e) {
            // Usually indicates Access Denied / Not a member
            return ResponseEntity.status(403).build();
        }
    }
}
