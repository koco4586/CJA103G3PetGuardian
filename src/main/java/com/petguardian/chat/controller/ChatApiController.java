package com.petguardian.chat.controller;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.petguardian.chat.model.ChatMessageDTO;
import com.petguardian.chat.model.ChatRoomDTO;
import com.petguardian.chat.model.ChatRoomEntity;
import com.petguardian.chat.service.AuthStrategyService;
import com.petguardian.chat.service.ChatRoomMapper;
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
    private final AuthStrategyService authStrategyService;
    private final ChatService chatService;
    private final ChatRoomMapper chatRoomMapper;

    public ChatApiController(AuthStrategyService authStrategyService,
            ChatService chatService,
            ChatRoomMapper chatRoomMapper) {
        this.authStrategyService = authStrategyService;
        this.chatService = chatService;
        this.chatRoomMapper = chatRoomMapper;
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
     * @return ChatRoomEntity or 404 Not Found
     */
    @GetMapping
    public ResponseEntity<ChatRoomDTO> findChatroom(
            HttpServletRequest request,
            @RequestParam Integer partnerId,
            @RequestParam(required = false, defaultValue = "0") Integer chatroomType) {

        Integer currentUserId = authStrategyService.getCurrentUserId(request);
        if (currentUserId == null) {
            return ResponseEntity.status(401).build();
        }

        ChatRoomEntity chatroom = chatService.findChatroom(currentUserId, partnerId, chatroomType);

        if (chatroom == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(chatRoomMapper.toDto(chatroom, currentUserId));
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
        } catch (SecurityException e) {
            return ResponseEntity.status(403).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Checks global unread status for the current user.
     * Used for the global header "Red Dot".
     */
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Boolean>> checkUnreadStatus(HttpServletRequest request) {
        Integer currentUserId = authStrategyService.getCurrentUserId(request);
        if (currentUserId == null) {
            return ResponseEntity.status(401).build();
        }

        boolean hasUnread = chatService.hasUnreadMessages(currentUserId);
        return ResponseEntity.ok(Collections.singletonMap("hasUnread", hasUnread));
    }

    /**
     * Marks a room as read.
     * Called when opening a chat or focusing on the window.
     * Returns the UPDATED global unread status.
     */
    @PostMapping("/{chatroomId}/read")
    public ResponseEntity<Map<String, Boolean>> markAsRead(
            HttpServletRequest request,
            @PathVariable Integer chatroomId) {

        Integer currentUserId = authStrategyService.getCurrentUserId(request);
        if (currentUserId == null) {
            return ResponseEntity.status(401).build();
        }

        chatService.markRoomAsRead(chatroomId, currentUserId);

        // Return new global status
        boolean hasUnread = chatService.hasUnreadMessages(currentUserId);
        return ResponseEntity.ok(Collections.singletonMap("hasUnread", hasUnread));
    }
}
